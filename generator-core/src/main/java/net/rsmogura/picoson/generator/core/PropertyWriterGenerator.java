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
import static net.rsmogura.picoson.generator.core.BinaryNames.JSON_WRITE_STRING_VALUE;
import static net.rsmogura.picoson.generator.core.BinaryNames.STRING_RETURNING_METHOD;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.I2L;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Type.BOOLEAN_TYPE;
import static org.objectweb.asm.Type.INT_TYPE;
import static org.objectweb.asm.Type.LONG_TYPE;
import static org.objectweb.asm.Type.getMethodDescriptor;
import static org.objectweb.asm.Type.getType;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import net.rsmogura.picoson.JsonWriter;
import net.rsmogura.picoson.generator.core.analyze.FieldProperty;
import net.rsmogura.picoson.generator.core.analyze.PropertiesCollector;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class PropertyWriterGenerator extends PropertyAbstractGenerator{

  public PropertyWriterGenerator(MethodVisitor mv, Type owner,
      Elements elements,
      PropertiesCollector propertiesCollector) {
    super(mv, owner, elements, propertiesCollector);
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
    boolean castToLong = false;

    // I really don't like this switch for primitives
    switch (propertyType.getKind()) {
      case INT:
        // TODO Extract method descriptors to constants
        writeMethodDesc = getMethodDescriptor(getType(JsonWriter.class), LONG_TYPE);
        fieldDescriptor = INT_TYPE;
        castToLong = true;
        break;
      case BOOLEAN:
        writeMethodDesc = getMethodDescriptor(getType(JsonWriter.class), BOOLEAN_TYPE);
        fieldDescriptor = BOOLEAN_TYPE;
        break;
      case LONG:
        writeMethodDesc = getMethodDescriptor(getType(JsonWriter.class), LONG_TYPE);
        fieldDescriptor = LONG_TYPE;
        break;
      default:
        throw new PicosonGeneratorException("Unsupported primitive type "
            + propertyType + " for property "
            + fieldProperty.getPropertyName() + " in " + owner);
    }

    mv.visitVarInsn(ALOAD, PARAM_THIS);
    mv.visitFieldInsn(GETFIELD, owner.getInternalName(),
        fieldProperty.getFieldElement().getSimpleName().toString(),
        fieldDescriptor.getDescriptor());

    if (castToLong) {
      mv.visitInsn(I2L);
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
  protected void handleString(FieldProperty fieldProperty, DeclaredType declaredType) {
    mv.visitMethodInsn(INVOKEVIRTUAL, JSON_WRITER_NAME,
        "value", JSON_WRITE_STRING_VALUE, false);
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
