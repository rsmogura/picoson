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
import static net.rsmogura.picoson.generator.core.BinaryNames.BOOL_RETURNING_METHOD;
import static net.rsmogura.picoson.generator.core.BinaryNames.GET_READ_INDEX_DESCRIPTOR;
import static net.rsmogura.picoson.generator.core.BinaryNames.INT_RETURNING_METHOD;
import static net.rsmogura.picoson.generator.core.BinaryNames.JSON_PROPERTY_DESCRIPTOR_NAME;
import static net.rsmogura.picoson.generator.core.BinaryNames.JSON_READER_NAME;
import static net.rsmogura.picoson.generator.core.BinaryNames.LONG_RETURNING_METHOD;
import static net.rsmogura.picoson.generator.core.BinaryNames.STRING_RETURNING_METHOD;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.IF_ICMPNE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Type.BOOLEAN_TYPE;
import static org.objectweb.asm.Type.INT_TYPE;
import static org.objectweb.asm.Type.LONG_TYPE;

import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import net.rsmogura.picoson.generator.core.analyze.FieldProperty;
import net.rsmogura.picoson.generator.core.analyze.PropertiesCollector;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Generator for property reader. The property reader is responsible for
 * processing current property from JSON stream.
 */
public class PropertyReaderGenerator extends PropertyAbstractGenerator {

  public PropertyReaderGenerator(MethodVisitor mv, Type owner,
      Elements elements, PropertiesCollector propertiesCollector) {
    super(mv, owner, elements, propertiesCollector);
  }

  protected void handlePrimitiveProperty(FieldProperty fieldProperty,
      TypeMirror propertyType) {
    String readerMethodName;
    String readerMethodDescriptor;
    Type fieldDescriptor;

    switch (propertyType.getKind()) {
      case INT:
        readerMethodName = "nextInt";
        readerMethodDescriptor = INT_RETURNING_METHOD;
        fieldDescriptor = INT_TYPE;
        break;
      case BOOLEAN:
        readerMethodName = "nextBoolean";
        readerMethodDescriptor = BOOL_RETURNING_METHOD;
        fieldDescriptor = BOOLEAN_TYPE;
        break;
      case LONG:
        readerMethodName = "nextLong";
        readerMethodDescriptor = LONG_RETURNING_METHOD;
        fieldDescriptor = LONG_TYPE;
        break;
      default:
        throw new PicosonGeneratorException("Unsupported primitive type "
          + propertyType + " for property "
          + fieldProperty.getPropertyName() + " in " + owner);
    }

    mv.visitVarInsn(ALOAD, PARAM_THIS);
    mv.visitVarInsn(ALOAD, PARAM_READER_WRITER);
    mv.visitMethodInsn(INVOKEVIRTUAL, JSON_READER_NAME,
        readerMethodName, readerMethodDescriptor, false);
    mv.visitFieldInsn(PUTFIELD, owner.getInternalName(),
        fieldProperty.getFieldElement().getSimpleName().toString(),
        fieldDescriptor.getDescriptor());
  }

  @Override
  protected void preHandleReferenceProperty(FieldProperty fieldProperty,
      DeclaredType declaredType) {
    mv.visitVarInsn(ALOAD, PARAM_THIS);
    mv.visitVarInsn(ALOAD, PARAM_READER_WRITER);
  }

  @Override
  protected void postHandleReferenceProperty(FieldProperty fieldProperty,
      DeclaredType declaredType) {
    mv.visitFieldInsn(PUTFIELD, owner.getInternalName(),
        fieldProperty.getFieldElement().getSimpleName().toString(),
        utils.descriptorFromType((TypeElement) declaredType.asElement()));
  }

  protected void handleString(FieldProperty fieldProperty,
      DeclaredType declaredType) {
    mv.visitMethodInsn(INVOKEVIRTUAL, JSON_READER_NAME,
        "nextString", STRING_RETURNING_METHOD, false);
  }

  protected void handleArrayProperty(FieldProperty fieldProperty) {
    // TODO Implement array properties
  }
}
