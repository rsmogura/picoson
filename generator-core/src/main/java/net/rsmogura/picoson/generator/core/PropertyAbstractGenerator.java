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

import static javax.lang.model.type.TypeKind.DECLARED;
import static net.rsmogura.picoson.generator.core.BinaryNames.GET_READ_INDEX_DESCRIPTOR;
import static net.rsmogura.picoson.generator.core.BinaryNames.JSON_PROPERTY_DESCRIPTOR_NAME;
import static net.rsmogura.picoson.generator.core.BinaryNames.JSON_READER_NAME;
import static net.rsmogura.picoson.generator.core.BinaryNames.STRING_RETURNING_METHOD;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.IF_ICMPNE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.PUTFIELD;

import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import net.rsmogura.picoson.abi.Names;
import net.rsmogura.picoson.generator.core.analyze.FieldProperty;
import net.rsmogura.picoson.generator.core.analyze.PropertiesCollector;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public abstract class PropertyAbstractGenerator extends AbstractMethodGenerator {
  protected static final int PARAM_THIS = 0;
  protected static final int PARAM_DESC = 1;
  protected static final int PARAM_READER_WRITER = 2;
  protected static final int PARAM_DESCRIPTOR_IDX = 3;

  public PropertyAbstractGenerator(MethodVisitor mv, Type owner,
      Elements elements,
      PropertiesCollector propertiesCollector) {
    super(mv, owner, elements, propertiesCollector);
  }

  public void generate() {
    // Read index of property, and store it as local, this has been
    // determined as having big performance impact for reading large objects
    mv.visitVarInsn(ALOAD, PARAM_DESC);
    mv.visitMethodInsn(INVOKEVIRTUAL, JSON_PROPERTY_DESCRIPTOR_NAME,
        "getReadPropertyIndex", GET_READ_INDEX_DESCRIPTOR, false);
    mv.visitVarInsn(ISTORE, PARAM_DESCRIPTOR_IDX);

    // TODO This is if-else-if block, which is terrible slow, for large number of properties
    //      This should be changed to BST.
    for (FieldProperty fp : propertiesCollector.getJsonProperties().values()) {
      final Label elseBlock = new Label();
      mv.visitVarInsn(ILOAD, PARAM_DESCRIPTOR_IDX);
      mv.visitLdcInsn(fp.getReadIndex());
      mv.visitJumpInsn(IF_ICMPNE, elseBlock);

      // Normal block
      handleProperty(fp);
      mv.visitLdcInsn(true);
      mv.visitInsn(Opcodes.IRETURN);

      // Begin else block. Because we have return in every true-block
      // no need to generate skip all label, to jump to the end of if-else
      // tree.
      mv.visitLabel(elseBlock);
    }
    mv.visitLabel(new Label());
    mv.visitLdcInsn(false);
    mv.visitInsn(Opcodes.IRETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  protected void handleProperty(FieldProperty fieldProperty) {
    final VariableElement fieldElement = fieldProperty.getFieldElement();
    final TypeMirror propertyType = fieldElement.asType();
    final TypeKind typeKind = propertyType.getKind();

    if (typeKind == DECLARED) {
      final DeclaredType declaredType = (DeclaredType) propertyType;
      handleReferenceProperty(fieldProperty, declaredType);
    } else if (typeKind.isPrimitive()) {
      handlePrimitiveProperty(fieldProperty, propertyType);
    }
  }

  protected abstract void handlePrimitiveProperty(FieldProperty fieldProperty,
      TypeMirror propertyType);

  protected void handleReferenceProperty(FieldProperty fieldProperty,
      DeclaredType declaredType) {
    final TypeElement typeElement = (TypeElement) declaredType.asElement();
    final Name binaryName = elements.getBinaryName(typeElement);

    if (binaryName.contentEquals(String.class.getName())) {
      preHandleReferenceProperty(fieldProperty, declaredType);
      handleString(fieldProperty, declaredType);
      postHandleReferenceProperty(fieldProperty, declaredType);
    }
  }

  protected abstract void preHandleReferenceProperty(FieldProperty fieldProperty,
      DeclaredType declaredType);
  protected abstract void postHandleReferenceProperty(FieldProperty fieldProperty,
      DeclaredType declaredType);

  protected abstract void handleString(FieldProperty fieldProperty,
      DeclaredType declaredType);
}
