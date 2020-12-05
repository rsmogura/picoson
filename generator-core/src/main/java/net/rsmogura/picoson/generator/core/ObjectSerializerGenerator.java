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
import static net.rsmogura.picoson.generator.core.BinaryNames.BOOL_RETURNING_METHOD;
import static net.rsmogura.picoson.generator.core.BinaryNames.JSON_OBJECT_DESCRIPTOR;
import static net.rsmogura.picoson.generator.core.BinaryNames.JSON_OBJECT_DESCRIPTOR_GET_PROPERTIES_DESC;
import static net.rsmogura.picoson.generator.core.BinaryNames.JSON_OBJECT_DESCRIPTOR_NAME;
import static net.rsmogura.picoson.generator.core.BinaryNames.JSON_PROPERTY_DESCRIPTOR_NAME;
import static net.rsmogura.picoson.generator.core.BinaryNames.JSON_WRITER_NAME;
import static net.rsmogura.picoson.generator.core.BinaryNames.JSON_WRITER_RETURNING_METHOD;
import static net.rsmogura.picoson.generator.core.BinaryNames.OBJECT_RETURNING_METHOD;
import static net.rsmogura.picoson.generator.core.BinaryNames.WRITE_PROPERTY_DESCRIPTOR;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP_X1;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Type.getInternalName;
import static org.objectweb.asm.Type.getMethodDescriptor;
import static org.objectweb.asm.Type.getType;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.util.Elements;
import net.rsmogura.picoson.abi.Names;
import net.rsmogura.picoson.generator.core.analyze.PropertiesCollector;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ObjectSerializerGenerator extends AbstractMethodGenerator {
  public ObjectSerializerGenerator(MethodVisitor mv, Type owner,
      Elements elements,
      PropertiesCollector propertiesCollector) {
    super(mv, owner, elements, propertiesCollector);
  }

  public void generate() {
    final int iteratorLocal = 2;
    final Label propertiesLoopLabel = new Label();
    final Label propertiesLoopEnd = new Label();

    generateBeginObject();
    generatePropertiesDescriptorIterator(iteratorLocal);

    // While iterator.hasNext
    mv.visitLabel(propertiesLoopLabel);
    mv.visitVarInsn(ALOAD, iteratorLocal);
    mv.visitMethodInsn(INVOKEINTERFACE, getInternalName(Iterator.class),
        "hasNext", BOOL_RETURNING_METHOD, true);
    mv.visitJumpInsn(IFEQ, propertiesLoopEnd);

    // #jsonWriteProp(iterator.next())
    mv.visitVarInsn(ALOAD, 0);
    mv.visitVarInsn(ALOAD, iteratorLocal);
    mv.visitMethodInsn(INVOKEINTERFACE, getInternalName(Iterator.class),
        "next", OBJECT_RETURNING_METHOD, true);
    mv.visitTypeInsn(CHECKCAST, JSON_PROPERTY_DESCRIPTOR_NAME);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitMethodInsn(INVOKEVIRTUAL, owner.getInternalName(),
        WRITE_PROPERTY_NAME, WRITE_PROPERTY_DESCRIPTOR, false);
    mv.visitInsn(POP); // pop boolean result

    mv.visitJumpInsn(GOTO, propertiesLoopLabel);
    mv.visitLabel(propertiesLoopEnd);

    generateEndObject();
    mv.visitInsn(RETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  protected void generatePropertiesDescriptorIterator(int iteratorLocal) {
    mv.visitFieldInsn(GETSTATIC, owner.getInternalName(),
        Names.DESCRIPTOR_HOLDER, JSON_OBJECT_DESCRIPTOR);
    mv.visitMethodInsn(INVOKEVIRTUAL, JSON_OBJECT_DESCRIPTOR_NAME,
        "getProperties", JSON_OBJECT_DESCRIPTOR_GET_PROPERTIES_DESC, false);
    mv.visitMethodInsn(INVOKEINTERFACE, getInternalName(Collection.class),
        "iterator", getMethodDescriptor(getType(Iterator.class)), true);
    mv.visitVarInsn(ASTORE, iteratorLocal);
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
