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

import static net.rsmogura.picoson.generator.core.BinaryNames.BOOL_RETURNING_METHOD;
import static net.rsmogura.picoson.generator.core.BinaryNames.BYTE_RETURNING_METHOD;
import static net.rsmogura.picoson.generator.core.BinaryNames.DOUBLE_RETURNING_METHOD;
import static net.rsmogura.picoson.generator.core.BinaryNames.FLOAT_RETURNING_METHOD;
import static net.rsmogura.picoson.generator.core.BinaryNames.GET_READ_INDEX_DESCRIPTOR;
import static net.rsmogura.picoson.generator.core.BinaryNames.INT_RETURNING_METHOD;
import static net.rsmogura.picoson.generator.core.BinaryNames.JSON_PROPERTY_DESCRIPTOR_NAME;
import static net.rsmogura.picoson.generator.core.BinaryNames.JSON_READER_NAME;
import static net.rsmogura.picoson.generator.core.BinaryNames.LONG_RETURNING_METHOD;
import static net.rsmogura.picoson.generator.core.BinaryNames.SHORT_RETURNING_METHOD;
import static net.rsmogura.picoson.generator.core.BinaryNames.STRING_RETURNING_METHOD;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Type.BOOLEAN_TYPE;
import static org.objectweb.asm.Type.BYTE_TYPE;
import static org.objectweb.asm.Type.DOUBLE_TYPE;
import static org.objectweb.asm.Type.FLOAT_TYPE;
import static org.objectweb.asm.Type.INT_TYPE;
import static org.objectweb.asm.Type.LONG_TYPE;
import static org.objectweb.asm.Type.SHORT_TYPE;
import static org.objectweb.asm.Type.getType;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import net.rsmogura.picoson.generator.core.analyze.FieldProperty;
import net.rsmogura.picoson.generator.core.analyze.PropertiesCollector;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * Generator for property reader. The property reader is responsible for
 * processing current property from JSON stream.
 */
public class PropertyReaderGenerator extends PropertyAbstractGenerator {

  public PropertyReaderGenerator(MethodVisitor mv, Type owner,
      Elements elements, Types typeUtils, PropertiesCollector propertiesCollector) {
    super(mv, owner, elements, typeUtils, propertiesCollector);
  }

  @Override
  protected void getPropertyId() {
    // Read index of property, and store it as local, this has been
    // determined as having big performance impact for reading large objects
    mv.visitVarInsn(ALOAD, PARAM_DESC);
    mv.visitMethodInsn(INVOKEVIRTUAL, JSON_PROPERTY_DESCRIPTOR_NAME,
        "getReadPropertyIndex", GET_READ_INDEX_DESCRIPTOR, false);
  }

  @Override
  protected int getPropertyIndexForCompare(FieldProperty fp) {
    return fp.getReadIndex();
  }

  protected void handlePrimitiveProperty(FieldProperty fieldProperty,
      TypeMirror propertyType) {
    String readerMethodName;
    String readerMethodDescriptor;
    String fieldDescriptor = utils.descriptorFromTypeMirror(propertyType);

    switch (propertyType.getKind()) {
      case BYTE:
        readerMethodName = "nextByte";
        readerMethodDescriptor = BYTE_RETURNING_METHOD;
        break;
      case SHORT:
        readerMethodName = "nextShort";
        readerMethodDescriptor = SHORT_RETURNING_METHOD;
        break;
      case INT:
        readerMethodName = "nextInt";
        readerMethodDescriptor = INT_RETURNING_METHOD;
        break;
      case BOOLEAN:
        readerMethodName = "nextBoolean";
        readerMethodDescriptor = BOOL_RETURNING_METHOD;
        break;
      case LONG:
        readerMethodName = "nextLong";
        readerMethodDescriptor = LONG_RETURNING_METHOD;
        break;
      case FLOAT:
        readerMethodName = "nextFloat";
        readerMethodDescriptor = FLOAT_RETURNING_METHOD;
        break;
      case DOUBLE:
        readerMethodName = "nextDouble";
        readerMethodDescriptor = DOUBLE_RETURNING_METHOD;
        break;
      default:
        throw new PicosonGeneratorException("Unsupported primitive type "
          + propertyType + " for property deserialization "
          + fieldProperty.getPropertyName() + " in " + owner);
    }

    mv.visitVarInsn(ALOAD, PARAM_THIS);
    mv.visitVarInsn(ALOAD, PARAM_READER_WRITER);
    mv.visitMethodInsn(INVOKEVIRTUAL, JSON_READER_NAME,
        readerMethodName, readerMethodDescriptor, false);
    mv.visitFieldInsn(PUTFIELD, owner.getInternalName(),
        fieldProperty.getFieldElement().getSimpleName().toString(),
        fieldDescriptor);
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

  @Override
  protected void handleBasicReferenceProperty(FieldProperty fieldProperty,
      DeclaredType declaredType) {
    // Dynamically build a method to execute
    // This will be based of simple name of type (Byte, Integer, Long, String)
    final String typeSimpleName = declaredType.asElement().getSimpleName().toString();

    final String nextMethodName;
    // For every rule there are exceptions...
    if ("String".equals(typeSimpleName)) {
      nextMethodName = "nextString";
    } else if ("Integer".equals(typeSimpleName)) {
      nextMethodName = "nextBoxedInt";
    } else {
      nextMethodName = "nextBoxed" + typeSimpleName;
    }

    final String nextMethodDescriptor = utils.methodDescriptorFromTypeMirror(declaredType);

    // The reader is on stack put by preHandleReferenceProperty
    mv.visitMethodInsn(INVOKEVIRTUAL, JSON_READER_NAME,
        nextMethodName,
        nextMethodDescriptor,
        false);
    // The value will be assigned to field by postHandleReferenceProperty
  }

  protected void handleArrayProperty(FieldProperty fieldProperty) {
    // TODO Implement array properties
  }
}
