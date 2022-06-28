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
import static org.objectweb.asm.Opcodes.*;

import net.rsmogura.picoson.abi.JsonObjectDescriptor;
import net.rsmogura.picoson.abi.Names;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * Generates or modifies the static class initializer to support
 * proper configure JSON metadata and supporting objects.
 */
public class InitializerGenerator extends MethodVisitor {
  /** Context will basic data and utils. */
  private final TransformationContext transformationContext;

  /**
   * Initialized generator with given visitor as the parent.
   */
  public InitializerGenerator(TransformationContext ctx, MethodVisitor mv) {
    super(ASM8, mv);
    this.transformationContext = ctx;
  }

  @Override
  public void visitCode() {
    super.visitCode();
    generateInitializer();
  }

  /**
   * Creates initializer for class. This step involves. <br />
   * 1. Creating descriptors (right now it's in JavaC tree plugin, but will be moved here). <br />
   * 2. Initialization of JsonSupport class, and storing it in synthetic field
   *    {@link net.rsmogura.picoson.abi.Names#SUPPORT_CLASS_HOLDER} (filed created in other place). <br/>
   * 3. Initialization of descriptor holder {@link net.rsmogura.picoson.abi.Names#DESCRIPTOR_HOLDER}.
   */
  protected void generateInitializer() {
    mv.visitTypeInsn(NEW, transformationContext.supportClassInternalName);
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL,
        transformationContext.supportClassInternalName, "<init>",
        VOID_METHOD_DESCRIPTOR, false);
    // Store class in static field (this field has to be added somewhere else)
    mv.visitFieldInsn(
        PUTSTATIC,
        transformationContext.jsonClassInternalName,
        SUPPORT_CLASS_HOLDER,
        JSON_SUPPORT_DESCRIPTOR);
    mv.visitMethodInsn(
      INVOKESTATIC,
      transformationContext.jsonClassInternalName,
      DESCRIPTOR_INITIALIZER,
      Type.getMethodDescriptor(Type.getType(JsonObjectDescriptor.class)), // TODO Shared
      false);
    mv.visitFieldInsn(PUTSTATIC,
      transformationContext.jsonClassInternalName,
      DESCRIPTOR_HOLDER,
      JSON_OBJECT_DESCRIPTOR);
  }

  /**
   * Adds JsonSupport holder field to class.
   */
  public static void addJsonSupportFiled(ClassVisitor cv) {
    cv.visitField(ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC,
        SUPPORT_CLASS_HOLDER, JSON_SUPPORT_DESCRIPTOR, null, null);
  }
}
