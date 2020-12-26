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

import static net.rsmogura.picoson.abi.Names.SUPPORT_CLASS_POSTFIX;
import static net.rsmogura.picoson.abi.Names.SUPPORT_CLASS_READ_IMPL;
import static net.rsmogura.picoson.abi.Names.SUPPORT_CLASS_WRITE_IMPL;
import static net.rsmogura.picoson.generator.core.BinaryNames.VOID_METHOD_DESCRIPTOR;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;
import static org.objectweb.asm.Type.VOID_TYPE;
import static org.objectweb.asm.Type.getMethodDescriptor;
import static org.objectweb.asm.Type.getType;

import java.io.IOException;
import java.io.OutputStream;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;
import net.rsmogura.picoson.JsonReader;
import net.rsmogura.picoson.JsonSupport;
import net.rsmogura.picoson.JsonWriter;
import net.rsmogura.picoson.abi.Names;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;

public class JsonSupportClassGenerator {
  private final ClassWriter out;

  private final JavaFileManager fileManager;

  private final TransformationContext ctx;

  public JsonSupportClassGenerator(TransformationContext ctx, JavaFileManager fileManager) {
    this.fileManager = fileManager;
    this.ctx = ctx;

    this.out = new ClassWriter(COMPUTE_MAXS | COMPUTE_FRAMES);
  }

  public void generate() {
    out.visit(V1_8, ACC_PUBLIC, ctx.supportClassInternalName,
        generateClassSignature(), Type.getInternalName(JsonSupport.class), null);
    generateConstructor();
    generateReadMethod();
    generateWriteMethod();
    out.visitEnd();

    try{
      storeClass();
    } catch (IOException ioe) {
      throw new PicosonGeneratorException(ioe);
    }
  }

  protected void generateConstructor() {
    final MethodVisitor mv = out.visitMethod(ACC_PUBLIC,
        "<init>", VOID_METHOD_DESCRIPTOR,null, null);
    mv.visitVarInsn(ALOAD, 0);
    // TODO JsonSupport as constant
    mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(JsonSupport.class),
        "<init>", VOID_METHOD_DESCRIPTOR, false);
    mv.visitInsn(RETURN);
    mv.visitMaxs(-1, -1);
    mv.visitEnd();
  }

  protected void generateReadMethod() {
    //TODO getMethodDescriptor as constant
    //TODO reader method to call #jsonRead (but first need to migrate it to ASM)
    MethodVisitor mv = out.visitMethod(ACC_PUBLIC, SUPPORT_CLASS_READ_IMPL,
        getMethodDescriptor(getType(Object.class), getType(JsonReader.class)), null, null);
    mv.visitVarInsn(ALOAD, 1);

    // TODO (!) For now call public deserialize method
    mv.visitMethodInsn(INVOKESTATIC, ctx.jsonClassInternalName,
        Names.GENERATED_DESERIALIZE_METHOD_NAME,
        getMethodDescriptor(getType("L" + ctx.jsonClassInternalName + ";"), getType(JsonReader.class)),
        false);
    mv.visitInsn(ARETURN);
    mv.visitMaxs(-1, -1);
    mv.visitEnd();
  }

  protected void generateWriteMethod() {
    //TODO getMethodDescriptor as constant

    MethodVisitor mv = out.visitMethod(ACC_PUBLIC, SUPPORT_CLASS_WRITE_IMPL,
        getMethodDescriptor(VOID_TYPE, getType(Object.class), getType(JsonWriter.class)), null, null);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitTypeInsn(CHECKCAST, ctx.jsonClassInternalName);
    mv.visitVarInsn(ALOAD, 2);

    // TODO (!) For now call public deserialize method
    mv.visitMethodInsn(INVOKEVIRTUAL, ctx.jsonClassInternalName,
        Names.INSTANCE_SERIALIZE_METHOD_NAME,
        getMethodDescriptor(VOID_TYPE, getType(JsonWriter.class)),
        false);
    mv.visitInsn(RETURN);
    mv.visitMaxs(-1, -1);
    mv.visitEnd();
  }

  protected String generateClassSignature() {
    final SignatureWriter signatureWriter = new SignatureWriter();
    final SignatureVisitor superSignature = signatureWriter.visitSuperclass();
    superSignature.visitClassType(Type.getInternalName(JsonSupport.class));
    final SignatureVisitor typeArg = signatureWriter.visitTypeArgument('=');
    typeArg.visitClassType(ctx.jsonClassInternalName);
    typeArg.visitEnd();
    superSignature.visitEnd();
    return signatureWriter.toString();
  }

  protected void storeClass() throws IOException {
    final JavaFileObject outClass = fileManager
        .getJavaFileForOutput(StandardLocation.CLASS_OUTPUT,
            ctx.elements.getBinaryName(ctx.jsonClass).toString() + SUPPORT_CLASS_POSTFIX,
            Kind.CLASS, null);
    try (OutputStream outStream = outClass.openOutputStream()) {
      outStream.write(this.out.toByteArray());
    }
  }
}
