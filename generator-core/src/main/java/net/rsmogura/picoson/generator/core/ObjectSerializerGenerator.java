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

import static net.rsmogura.picoson.abi.Names.WRITE_PROPERTY_NAME;
import static net.rsmogura.picoson.generator.core.BinaryNames.JSON_OBJECT_DESCRIPTOR;
import static net.rsmogura.picoson.generator.core.BinaryNames.JSON_OBJECT_DESCRIPTOR_GET_PROPERTIES_DESC;
import static net.rsmogura.picoson.generator.core.BinaryNames.JSON_OBJECT_DESCRIPTOR_NAME;
import static net.rsmogura.picoson.generator.core.BinaryNames.JSON_WRITER_NAME;
import static net.rsmogura.picoson.generator.core.BinaryNames.JSON_WRITER_RETURNING_METHOD;
import static net.rsmogura.picoson.generator.core.BinaryNames.WRITE_PROPERTY_DESCRIPTOR;
import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IF_ICMPGE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.RETURN;

import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import net.rsmogura.picoson.abi.Names;
import net.rsmogura.picoson.generator.core.analyze.PropertiesCollector;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ObjectSerializerGenerator extends AbstractMethodGenerator {
  public ObjectSerializerGenerator(MethodVisitor mv, Type owner,
      Elements elements,
      Types typeUtils,
      PropertiesCollector propertiesCollector) {
    super(mv, owner, elements, typeUtils, propertiesCollector);
  }

  public void generate() {
    final int descriptorsArrayLocal = 2;
    final int counterLocal = 3;
    final int lengthLocal = 4;

    final Label propertiesLoopStart = new Label();
    final Label propertiesLoopEnd = new Label();

    generateBeginObject();
    generatePropertiesDescriptorIterator(descriptorsArrayLocal);

    // Store array length in local variable
    mv.visitVarInsn(ALOAD, descriptorsArrayLocal);
    mv.visitInsn(Opcodes.ARRAYLENGTH);
    mv.visitVarInsn(ISTORE, lengthLocal);

    // Initialize counter to 0
    mv.visitLdcInsn(0);
    mv.visitVarInsn(ISTORE, counterLocal);

    // Begin (int i=0; i < length; i++)
    // Continuatino condition check
    mv.visitLabel(propertiesLoopStart);
    mv.visitVarInsn(ILOAD, counterLocal);
    mv.visitVarInsn(ILOAD, lengthLocal);
    mv.visitJumpInsn(IF_ICMPGE, propertiesLoopEnd);

    // Prepare invocation for this.#jsonWriteProp
    mv.visitVarInsn(ALOAD, 0);

    // Put on stack current property descriptor descriptors[i]
    mv.visitVarInsn(ALOAD, descriptorsArrayLocal);
    mv.visitVarInsn(ILOAD, counterLocal);
    mv.visitInsn(AALOAD);

    // Load writer
    mv.visitVarInsn(ALOAD, 1);
    mv.visitMethodInsn(INVOKEVIRTUAL, owner.getInternalName(),
        WRITE_PROPERTY_NAME, WRITE_PROPERTY_DESCRIPTOR, false);
    mv.visitInsn(POP); // Pop boolean result, as it's not used

    // Increment index in properties descriptor array
    mv.visitIincInsn(counterLocal, 1);
    mv.visitJumpInsn(GOTO, propertiesLoopStart);
    mv.visitLabel(propertiesLoopEnd);

    // Finish object
    generateEndObject();

    // Leave method & visitor
    mv.visitInsn(RETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  protected void generatePropertiesDescriptorIterator(int descriptorsArrayLocal) {
    mv.visitFieldInsn(GETSTATIC, owner.getInternalName(),
        Names.DESCRIPTOR_HOLDER, JSON_OBJECT_DESCRIPTOR);
    mv.visitMethodInsn(INVOKEVIRTUAL, JSON_OBJECT_DESCRIPTOR_NAME,
        "getProperties", JSON_OBJECT_DESCRIPTOR_GET_PROPERTIES_DESC, false);
    mv.visitVarInsn(ASTORE, descriptorsArrayLocal);
  }

  protected void generateBeginObject() {
    mv.visitVarInsn(ALOAD, 1);
    mv.visitMethodInsn(INVOKEVIRTUAL, JSON_WRITER_NAME,
        "beginObject", JSON_WRITER_RETURNING_METHOD, false);
    mv.visitInsn(POP);
  }

  protected void generateEndObject() {
    mv.visitVarInsn(ALOAD, 1);
    mv.visitMethodInsn(INVOKEVIRTUAL, JSON_WRITER_NAME,
        "endObject", JSON_WRITER_RETURNING_METHOD, false);
    mv.visitInsn(POP);
  }
}
