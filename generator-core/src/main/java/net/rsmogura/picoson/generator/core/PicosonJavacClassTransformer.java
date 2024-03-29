/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.rsmogura.picoson.generator.core;

import static net.rsmogura.picoson.abi.Names.*;
import static net.rsmogura.picoson.generator.core.BinaryNames.*;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.RETURN;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileManager;

import net.rsmogura.picoson.JsonReader;
import net.rsmogura.picoson.JsonSupport;
import net.rsmogura.picoson.abi.JsonObjectDescriptor;
import net.rsmogura.picoson.abi.Names;
import net.rsmogura.picoson.generator.core.analyze.PropertiesCollector;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;

/**
 * Class transformer supporting JavaC ATP.
 * <br />
 * Even using JavaC API ATP has some limitations, this transformer
 * can help overcome this limitations.
 */
public class PicosonJavacClassTransformer extends ClassVisitor {

  private final TypeElement jsonClass;
  // TODO Only list of properties needed, not the whole collector class
  private final PropertiesCollector propertiesCollector;
  private final JavaFileManager fileManager;
  private final Elements elements;
  private final Types typeUtils;
  private final GeneratorUtils generatorUtils;
  private final TransformationContext transformationContext;

  private String thizClass;

  /**
   * Indicates if initializer has to be generated from scratch,
   * not modified only (in cases where static initializer was not present
   * in class).
   */
  private boolean needGenerateInitializer = true;

  /** Does this class has {@link net.rsmogura.picoson.annotations.Json} annotation. */
  private boolean isJsonAnnotated;

  public PicosonJavacClassTransformer(int api, ClassVisitor cv,
      TypeElement jsonClass, PropertiesCollector propertiesCollector,
      JavaFileManager fileManager,
      Elements elements,
      Types typeUtils) {
    super(api, cv);
    this.jsonClass = jsonClass;
    this.propertiesCollector = propertiesCollector;
    this.fileManager = fileManager;
    this.elements = elements;
    this.typeUtils = typeUtils;

    this.generatorUtils = new GeneratorUtils(elements, typeUtils);
    this.transformationContext = new TransformationContext(
        elements, typeUtils, propertiesCollector, jsonClass);
  }

  @Override
  public void visit(int version, int access, String name, String signature, String superName,
      String[] interfaces) {
    thizClass = name;

    super.visit(version, access, name, signature, superName, interfaces);
  }

  @Override
  public FieldVisitor visitField(int access, String name, String desc,
                                 String signature, Object value) {
    if (Names.DESCRIPTOR_HOLDER.equals(name)) {
      // Override synthetic
      access = access | ACC_SYNTHETIC;
    }

    FieldVisitor fieldVisitor = super.visitField(access, name, desc, signature, value);
    return fieldVisitor;
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
      String[] exceptions) {
    // TODO Can be removed later.
    //      This part removes methods generated by JavaC API as part of
    //      migration to byte-code
    if (isJsonAnnotated && Names.DESCRIPTOR_INITIALIZER.equals(name)) {
      System.out.println("Removing json descriptor ------------------");
      // Remove method, will be re-generated later
      return null;
    } else if (isJsonAnnotated && Names.READ_PROPERTY_NAME.equals(name)) {
      return null;
    } else if (isJsonAnnotated && Names.INSTANCE_SERIALIZE_PUBLIC_METHOD.equals(name)) {
      return null;
    } else if (isJsonAnnotated && JSON_SUPPORT_OBTAIN_PUB_METHOD.equals(name)) {
      return null;
    } else if (isJsonAnnotated && "<clinit>".equals(name)) {
      // This block is used to conditionaly extend static initializer
      // Static initializer can already be in the transformed code
      // If it's there than we will add new instruction to it,
      // if not, it has be generated with instructions.
      needGenerateInitializer = false;

      return createInitializerGenerator(
          super.visitMethod(access, name, descriptor, signature, exceptions));
    } else {
      return super.visitMethod(access, name, descriptor, signature, exceptions);
    }
  }

  @Override
  public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    // Start with fast check, if the annotation can be considered as JSON
    final AnnotationVisitor parentVisitor = super.visitAnnotation(desc, visible);
    if (JSON_ANNOTATION.equals(desc)) {
      this.isJsonAnnotated = true;
    }

    return parentVisitor;
  }

  @Override
  public void visitEnd() {
    if (isJsonAnnotated) {
      final Type thizClassType = Type.getObjectType(thizClass);

      final MethodVisitor initDescriptorMv =
          super.visitMethod(
              ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC,
              Names.DESCRIPTOR_INITIALIZER,
              Type.getMethodDescriptor(Type.getType(JsonObjectDescriptor.class)),
              null,
              null);

      super.visitField(ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC,
        DESCRIPTOR_HOLDER, JSON_OBJECT_DESCRIPTOR, null, null);

      new JsonDescriptorsGenerator(initDescriptorMv, thizClassType, propertiesCollector).generate();

      generatePropertyReader(thizClassType);
      generatePropertyWriter(thizClassType);
      generateObjectReader(thizClassType);
      generateObjectWriter(thizClassType);
      generateSupportCode();

      if (needGenerateInitializer) {
        generateStaticInitializer();
      }
    }

    super.visitEnd();
  }

  protected void generateObjectReader(final Type thizClassType) {
    final MethodVisitor objectDeserializerMv = cv.visitMethod(
      ACC_STATIC /* | ACC_SYNTHETIC */ | ACC_PUBLIC,
      GENERATED_DESERIALIZE_METHOD_NAME,
      Type.getMethodDescriptor(
        Type.getObjectType(transformationContext.jsonClassInternalName),
        Type.getType(JsonReader.class)
      ),
      null,
      null
    );

    new ObjectDeserializerGenerator(objectDeserializerMv,
      Type.getObjectType(this.thizClass), this.elements, this.typeUtils, this.propertiesCollector).generate();

    objectDeserializerMv.visitMaxs(-1, -1);
    objectDeserializerMv.visitEnd();
  }

  protected void generatePropertyReader(final Type thizClassType) {
    final MethodVisitor propertyRead =
        cv.visitMethod(ACC_PROTECTED, READ_PROPERTY_NAME, READ_PROPERTY_DESCRIPTOR, null, null);
    new PropertyReaderGenerator(propertyRead, thizClassType, elements, typeUtils,
        propertiesCollector).generate();
  }

  protected void generateObjectWriter(final Type thizClassType) {
    final MethodVisitor objectSerializerMv = cv.visitMethod(
        ACC_PROTECTED, INSTANCE_SERIALIZE_METHOD_NAME,
        INSTANCE_SERIALIZE_METHOD_DESC, null, null);
    new ObjectSerializerGenerator(objectSerializerMv, thizClassType, elements, typeUtils,
        propertiesCollector).generate();

    // TODO Temporary - public entry methods should be controlled by other annotations
    final MethodVisitor jsonWriteMv = cv.visitMethod(
        ACC_PUBLIC, INSTANCE_SERIALIZE_PUBLIC_METHOD,
        INSTANCE_SERIALIZE_METHOD_DESC, null, null);
    jsonWriteMv.visitVarInsn(ALOAD, 0);
    jsonWriteMv.visitVarInsn(ALOAD, 1);
    jsonWriteMv.visitMethodInsn(INVOKEVIRTUAL, thizClassType.getInternalName(),
        INSTANCE_SERIALIZE_METHOD_NAME, INSTANCE_SERIALIZE_METHOD_DESC, false);
    jsonWriteMv.visitInsn(RETURN);
    jsonWriteMv.visitMaxs(0, 0);
    jsonWriteMv.visitEnd();
  }

  protected void generatePropertyWriter(final Type thizClassType) {
    final MethodVisitor propertyWrite =
        cv.visitMethod(ACC_PROTECTED, WRITE_PROPERTY_NAME, WRITE_PROPERTY_DESCRIPTOR, null, null);
    new PropertyWriterGenerator(propertyWrite, thizClassType, elements, typeUtils, propertiesCollector)
        .generate();

  }

  protected JsonSupportClassGenerator generateSupportClass() {
    final JsonSupportClassGenerator supportClassGenerator = new JsonSupportClassGenerator(
        transformationContext, fileManager);
    supportClassGenerator.generate();

    return supportClassGenerator;
  }

  protected void generateStaticInitializer() {
    // TODO May generate few static initializers, we have to extend existing one if present
    MethodVisitor mv = cv.visitMethod(ACC_STATIC,
        "<clinit>", VOID_METHOD_DESCRIPTOR, null, null);

    final InitializerGenerator initMv = createInitializerGenerator(mv);
    // Visit code generates required bytecode
    initMv.visitCode();
    // Finish initializer code
    initMv.visitInsn(RETURN);
    initMv.visitMaxs(-1, -1);
    initMv.visitEnd();
  }

  protected void generateSupportCode() {
    InitializerGenerator.addJsonSupportFiled(cv);
    generateSupportClass();

    SignatureWriter sw = new SignatureWriter();
    final SignatureVisitor returnSignature = sw.visitReturnType();
    // TODO Keep JsonSupport name in binary names, not to generate this name every time
    returnSignature.visitClassType(Type.getInternalName(JsonSupport.class));
    final SignatureVisitor typeArgVisitor = returnSignature.visitTypeArgument('=');
    typeArgVisitor.visitClassType(thizClass);
    typeArgVisitor.visitEnd();
    returnSignature.visitEnd();

    final String signature = sw.toString();
    final MethodVisitor mv = cv
        .visitMethod(ACC_PUBLIC | ACC_STATIC, JSON_SUPPORT_OBTAIN_PUB_METHOD,
            JSON_SUPPORT_METHOD_DESCRIPTOR, signature, null);
    mv.visitCode();
    mv.visitFieldInsn(GETSTATIC, thizClass, SUPPORT_CLASS_HOLDER, JSON_SUPPORT_DESCRIPTOR);
    mv.visitInsn(ARETURN);
    mv.visitMaxs(-1, -1);
    mv.visitEnd();
  }

  // Test supporting methods

  /**
   * (Extracted for test mocking). Creates instance of {@link InitializerGenerator}.
   */
  protected InitializerGenerator createInitializerGenerator(MethodVisitor parentVisitor) {
    return new InitializerGenerator(this.transformationContext, parentVisitor);
  }
}
