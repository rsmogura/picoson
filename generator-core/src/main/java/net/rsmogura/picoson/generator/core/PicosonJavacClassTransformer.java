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

import static net.rsmogura.picoson.abi.Names.READ_PROPERTY_NAME;
import static net.rsmogura.picoson.generator.core.BinaryNames.READ_PROPERTY_DESCRIPTOR;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;

import javax.lang.model.util.Elements;
import net.rsmogura.picoson.abi.JsonObjectDescriptor;
import net.rsmogura.picoson.abi.Names;
import net.rsmogura.picoson.generator.core.analyze.PropertiesCollector;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * Class transformer supporting JavaC ATP.
 * <br />
 * Even using JavaC API ATP has some limitations, this transformer
 * can help overcome this limitations.
 */
public class PicosonJavacClassTransformer extends ClassVisitor {
  // TODO Only list of properties needed, not the whole collector class
  private final PropertiesCollector propertiesCollector;
  private final Elements elements;

  private String thizClass;

  public PicosonJavacClassTransformer(int api, ClassVisitor cv,
      PropertiesCollector propertiesCollector, Elements elements) {
    super(api, cv);
    this.propertiesCollector = propertiesCollector;
    this.elements = elements;
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
    if (Names.DESCRIPTOR_INITIALIZER.equals(name)) {
      // Remove method, will be re-generated later
      return null;
    } else if (Names.READ_PROPERTY_NAME.equals(name)) {
      return null;
    } else {
      return super.visitMethod(access, name, descriptor, signature, exceptions);
    }
  }

  @Override
  public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    return super.visitAnnotation(desc, visible);
  }

  @Override
  public void visitEnd() {
    final Type thizClassType = Type.getObjectType(thizClass);

    final MethodVisitor initDescriptorMv = super.visitMethod(
        ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC,
        Names.DESCRIPTOR_INITIALIZER,
        Type.getMethodDescriptor(Type.getType(JsonObjectDescriptor.class)),
        null,
        null
    );

    final MethodVisitor propertyRead = cv.visitMethod(ACC_PROTECTED, READ_PROPERTY_NAME,
        READ_PROPERTY_DESCRIPTOR, null, null);

    new JsonDescriptorsGenerator(
        initDescriptorMv, thizClassType, propertiesCollector).generate();

    new PropertyReaderGenerator(propertyRead, thizClassType, elements, propertiesCollector)
        .generate();

    super.visitEnd();
  }
}
