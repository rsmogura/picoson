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

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;

import net.rsmogura.picoson.abi.JsonObjectDescriptorBuilder;
import net.rsmogura.picoson.abi.JsonPropertyDescriptor;
import net.rsmogura.picoson.generator.core.analyze.FieldProperty;
import net.rsmogura.picoson.generator.core.analyze.PropertiesCollector;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * Builds method responsible for creating object & properties descriptors.
 */
public class JsonDescriptorsGenerator {

  private static final String JSON_DESCRIPTOR_BUILDER_NAME =
      Type.getInternalName(JsonObjectDescriptorBuilder.class);

  private static final String JSON_PROPERTY_DESCRIPTOR_NAME =
      Type.getInternalName(JsonPropertyDescriptor.class);

  private static final String JSON_DESCRIPTOR_BUILDER_SET_DESCRIBED_CLASS;
  private static final String JSON_DESCRIPTOR_BUILDER_ADD_DESCRIPTOR;
  private static final String JSON_DESCRIPTOR_BUILDER_BUILD_DESCRIPTOR;

  private static final String NEW_JSON_PROPERTY_DESCRIPTOR;

  static {
    try {
      JSON_DESCRIPTOR_BUILDER_SET_DESCRIBED_CLASS =
          Type.getMethodDescriptor(
              JsonObjectDescriptorBuilder.class.getMethod("setJsonClass", Class.class));

      JSON_DESCRIPTOR_BUILDER_ADD_DESCRIPTOR =
          Type.getMethodDescriptor(
              JsonObjectDescriptorBuilder.class.getMethod(
                  "addPropertyDescriptor", JsonPropertyDescriptor.class));
      
      JSON_DESCRIPTOR_BUILDER_BUILD_DESCRIPTOR =
          Type.getMethodDescriptor(
              JsonObjectDescriptorBuilder.class.getDeclaredMethod("build"));

      NEW_JSON_PROPERTY_DESCRIPTOR =
          Type.getConstructorDescriptor(JsonPropertyDescriptor.class.getConstructor(
            String.class, String.class, int.class,
              Class.class, int.class, Class.class));
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private final MethodVisitor mv;
  private final Type owner;
  private final PropertiesCollector propertiesCollector;

  /**
   * Constructor.
   *
   * @param owner the owner of this method (used in generated code to build JSON descriptor)
   */
  public JsonDescriptorsGenerator(MethodVisitor mv, Type owner,
      PropertiesCollector propertiesCollector) {
    this.mv = mv;
    this.owner = owner;
    this.propertiesCollector = propertiesCollector;
  }

  /**
   * Begins generating the code.
   */
  public void generate() {
    mv.visitCode();

    createAndStoreBuilder();
    addPropertiesToBuilder();
    buildDescriptorFromBuilder();

    mv.visitInsn(ARETURN);
    mv.visitMaxs(0, 1);
    mv.visitEnd();
  }

  private void addPropertiesToBuilder() {
    for (FieldProperty fp : propertiesCollector.getJsonProperties().values()) {
      // Put builder on stack for adding descriptor
      mv.visitVarInsn(ALOAD, 1);

      mv.visitTypeInsn(NEW, JSON_PROPERTY_DESCRIPTOR_NAME);
      mv.visitInsn(DUP);

      // new JsonPropertyDescriptor(String jsonPropertyName, String internalPropertyName,
      //      int readPropertyIndex, Class<?> readerClass, int writePropertyIndex,
      //      Class<?> writerClass)
      mv.visitLdcInsn(fp.getPropertyName());
      mv.visitLdcInsn(fp.getFieldElement().getSimpleName().toString());
      mv.visitLdcInsn(fp.getReadIndex());
      mv.visitLdcInsn(owner);
      mv.visitLdcInsn(fp.getWriteIndex());
      mv.visitLdcInsn(owner);
      mv.visitMethodInsn(INVOKESPECIAL,
          JSON_PROPERTY_DESCRIPTOR_NAME, "<init>",
          NEW_JSON_PROPERTY_DESCRIPTOR, false);

      // JsonObjectDescriptorBuilder.addPropertyDescriptor
      // stack: aload 1, new descriptor
      mv.visitMethodInsn(INVOKEVIRTUAL,
          JSON_DESCRIPTOR_BUILDER_NAME, "addPropertyDescriptor",
          JSON_DESCRIPTOR_BUILDER_ADD_DESCRIPTOR, false);
    }
  }

  private void buildDescriptorFromBuilder() {
    mv.visitIntInsn(ALOAD, 1);
    mv.visitMethodInsn(INVOKEVIRTUAL,
        JSON_DESCRIPTOR_BUILDER_NAME, "build",
        JSON_DESCRIPTOR_BUILDER_BUILD_DESCRIPTOR, false);
  }

  private void createAndStoreBuilder() {
    mv.visitTypeInsn(NEW, JSON_DESCRIPTOR_BUILDER_NAME);
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL,
        JSON_DESCRIPTOR_BUILDER_NAME, "<init>",
        BinaryNames.VOID_METHOD_DESCRIPTOR, false);

    mv.visitInsn(DUP);
    mv.visitLdcInsn(owner);
    mv.visitMethodInsn(INVOKEVIRTUAL,
        JSON_DESCRIPTOR_BUILDER_NAME, "setJsonClass",
        JSON_DESCRIPTOR_BUILDER_SET_DESCRIBED_CLASS, false);

    mv.visitVarInsn(ASTORE, 1);
  }
}
