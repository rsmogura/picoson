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

import static net.rsmogura.picoson.generator.core.BinaryNames.GET_READ_INDEX_DESCRIPTOR;
import static net.rsmogura.picoson.generator.core.BinaryNames.JSON_PROPERTY_DESCRIPTOR_NAME;
import static net.rsmogura.picoson.generator.core.BinaryNames.JSON_WRITER_NAME;
import static net.rsmogura.picoson.generator.core.BinaryNames.JSON_WRITE_BOOLEAN_VALUE;
import static net.rsmogura.picoson.generator.core.BinaryNames.JSON_WRITE_NUMBER_VALUE;
import static net.rsmogura.picoson.generator.core.BinaryNames.JSON_WRITE_STRING_VALUE;
import static net.rsmogura.picoson.generator.core.BinaryNames.STRING_RETURNING_METHOD;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.F2D;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.I2L;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Type.BOOLEAN_TYPE;
import static org.objectweb.asm.Type.BYTE_TYPE;
import static org.objectweb.asm.Type.DOUBLE_TYPE;
import static org.objectweb.asm.Type.FLOAT_TYPE;
import static org.objectweb.asm.Type.INT_TYPE;
import static org.objectweb.asm.Type.LONG_TYPE;
import static org.objectweb.asm.Type.SHORT_TYPE;
import static org.objectweb.asm.Type.getMethodDescriptor;
import static org.objectweb.asm.Type.getType;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import net.rsmogura.picoson.JsonWriter;
import net.rsmogura.picoson.generator.core.analyze.FieldProperty;
import net.rsmogura.picoson.generator.core.analyze.PropertiesCollector;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class PropertyWriterGenerator extends PropertyAbstractGenerator{

  public PropertyWriterGenerator(MethodVisitor mv, Type owner,
      Elements elements,
      Types typeUtils, PropertiesCollector propertiesCollector) {
    super(mv, owner, elements, typeUtils, propertiesCollector);
  }

  @Override
  protected void getPropertyId() {
    // Read index of property, and store it as local, this has been
    // determined as having big performance impact for reading large objects
    mv.visitVarInsn(ALOAD, PARAM_DESC);
    mv.visitMethodInsn(INVOKEVIRTUAL, JSON_PROPERTY_DESCRIPTOR_NAME,
        "getWritePropertyIndex", GET_READ_INDEX_DESCRIPTOR, false);
  }

  @Override
  protected int getPropertyIndexForCompare(FieldProperty fp) {
    return fp.getWriteIndex();
  }

  @Override
  protected void handlePrimitiveProperty(FieldProperty fieldProperty, TypeMirror propertyType) {
    this.writePropertyName(fieldProperty); // After it JsonWriter on stack

    final Type fieldDescriptor;
    final String writeMethodDesc;

    // Right now JsonWriter has only long version of value, so 32bit integers
    // has to be casted to long.
    // Same story for floats
    boolean castIntToLong = false;
    boolean castFloatToDouble = false;

    // TODO Remove casting by adding floats / ints to reader

    // I really don't like this switch for primitives
    switch (propertyType.getKind()) {
      case BYTE:
        writeMethodDesc = getMethodDescriptor(getType(JsonWriter.class), LONG_TYPE);
        fieldDescriptor = BYTE_TYPE;
        castIntToLong = true;
        break;
      case SHORT:
        writeMethodDesc = getMethodDescriptor(getType(JsonWriter.class), LONG_TYPE);
        fieldDescriptor = SHORT_TYPE;
        castIntToLong = true;
        break;
      case INT:
        // TODO Extract method descriptors to constants
        writeMethodDesc = getMethodDescriptor(getType(JsonWriter.class), LONG_TYPE);
        fieldDescriptor = INT_TYPE;
        castIntToLong = true;
        break;
      case BOOLEAN:
        writeMethodDesc = getMethodDescriptor(getType(JsonWriter.class), BOOLEAN_TYPE);
        fieldDescriptor = BOOLEAN_TYPE;
        break;
      case LONG:
        writeMethodDesc = getMethodDescriptor(getType(JsonWriter.class), LONG_TYPE);
        fieldDescriptor = LONG_TYPE;
        break;
      case FLOAT:
        writeMethodDesc = getMethodDescriptor(getType(JsonWriter.class), DOUBLE_TYPE);
        fieldDescriptor = FLOAT_TYPE;
        castFloatToDouble = true;
        break;
      case DOUBLE:
        writeMethodDesc = getMethodDescriptor(getType(JsonWriter.class), DOUBLE_TYPE);
        fieldDescriptor = DOUBLE_TYPE;
        break;
      default:
        throw new PicosonGeneratorException("Unsupported primitive type "
            + propertyType + " for property serialization "
            + fieldProperty.getPropertyName() + " in " + owner);
    }

    mv.visitVarInsn(ALOAD, PARAM_THIS);
    mv.visitFieldInsn(GETFIELD, owner.getInternalName(),
        fieldProperty.getFieldElement().getSimpleName().toString(),
        fieldDescriptor.getDescriptor());

    if (castIntToLong) {
      mv.visitInsn(I2L);
    }
    if (castFloatToDouble) {
      mv.visitInsn(F2D);
    }

    mv.visitMethodInsn(INVOKEVIRTUAL, JSON_WRITER_NAME,
        "value", writeMethodDesc, false);
  }

  @Override
  protected void preHandleReferenceProperty(FieldProperty fieldProperty,
      DeclaredType declaredType) {

    this.writePropertyName(fieldProperty); // After it JsonWriter on stack

    // Put value of property
    mv.visitVarInsn(ALOAD, PARAM_THIS);
    mv.visitFieldInsn(GETFIELD, owner.getInternalName(),
        fieldProperty.getFieldElement().getSimpleName().toString(),
        utils.descriptorFromType((TypeElement) declaredType.asElement()));
  }

  @Override
  protected void postHandleReferenceProperty(FieldProperty fieldProperty,
      DeclaredType declaredType) {
    //Nothing to do here
  }

  @Override
  protected void handleBasicReferenceProperty(FieldProperty fieldProperty,
      DeclaredType declaredType) {
    final TypeElement numberType = elements.getTypeElement(Number.class.getName());

    // JsonWriter has value method supporting Numbers, String and Booleans;
    // choosing appropriate signature for a field / property type
    final String valueMethodSignature;
    if (typeUtils.isAssignable(declaredType, numberType.asType())) {
      valueMethodSignature = JSON_WRITE_NUMBER_VALUE;
    } else if (typeUtils.isSameType(getBooleanType(), declaredType)) {
      valueMethodSignature = JSON_WRITE_BOOLEAN_VALUE;
    } else if (typeUtils.isSameType(getStringType(), declaredType)) {
      valueMethodSignature = JSON_WRITE_STRING_VALUE;
    } else {
      throw new PicosonGeneratorException("Can't determine method to serialize "
        + declaredType + " at "
        + fieldProperty.getFieldElement().getEnclosingElement().asType()
        + "::" + fieldProperty.getFieldElement().getSimpleName()
        + " - this looks like internal error worth a bug report");
    }

    // Call a method with appropriate signature
    mv.visitMethodInsn(INVOKEVIRTUAL, JSON_WRITER_NAME, "value",
        valueMethodSignature, false);
  }

  private TypeMirror getStringType() {
    return elements.getTypeElement(String.class.getName()).asType();
  }

  private TypeMirror getBooleanType() {
    return elements.getTypeElement(Boolean.class.getName()).asType();
  }

  /**
   * Writes property name to output JSON. On stack will be {@link JsonWriter}
   * @param fieldProperty
   */
  protected void writePropertyName(FieldProperty fieldProperty) {
    mv.visitVarInsn(ALOAD, PARAM_READER_WRITER);
    mv.visitVarInsn(ALOAD, PARAM_DESC);
    mv.visitMethodInsn(INVOKEVIRTUAL, JSON_PROPERTY_DESCRIPTOR_NAME,
        "getJsonPropertyName", STRING_RETURNING_METHOD, false);
    // On stack writer, property name
    mv.visitMethodInsn(INVOKEVIRTUAL, JSON_WRITER_NAME,
        "name", JSON_WRITE_STRING_VALUE, false);
    // On stack writer
  }
}
