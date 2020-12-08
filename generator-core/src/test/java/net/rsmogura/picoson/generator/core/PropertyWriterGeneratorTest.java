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

import static net.rsmogura.picoson.generator.core.BinaryNames.JSON_WRITER_NAME;
import static net.rsmogura.picoson.generator.core.BinaryNames.JSON_WRITE_BOOLEAN_VALUE;
import static net.rsmogura.picoson.generator.core.BinaryNames.JSON_WRITE_NUMBER_VALUE;
import static net.rsmogura.picoson.generator.core.BinaryNames.JSON_WRITE_STRING_VALUE;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import net.rsmogura.picoson.generator.core.analyze.FieldProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

@ExtendWith(MockitoExtension.class)
class PropertyWriterGeneratorTest {

  @Mock
  private MethodVisitor mv;

  @Mock
  private Elements elements;

  @Mock
  private Types types;

  @Mock
  private TypeElement numberElement, stringElement, booleanElement, fieldOwner;

  @Mock
  private TypeMirror numberMirror, stringMirror, booleanMirror, fieldOwnerMirror;

  @Mock
  private VariableElement fieldElement;

  @Mock
  private Name fieldName;

  private Type owner = Type.getType("Lmy/test/Type;");
  private PropertyWriterGenerator propertyWriterGenerator;

  @BeforeEach
  void setupTest() {
    lenient().when(elements.getTypeElement(Number.class.getName())).thenReturn(numberElement);
    lenient().when(numberElement.asType()).thenReturn(numberMirror);

    lenient().when(elements.getTypeElement(String.class.getName())).thenReturn(stringElement);
    lenient().when(stringElement.asType()).thenReturn(stringMirror);

    lenient().when(elements.getTypeElement(Boolean.class.getName())).thenReturn(booleanElement);
    lenient().when(booleanElement.asType()).thenReturn(booleanMirror);

    lenient().when(fieldOwner.asType()).thenReturn(fieldOwnerMirror);
    lenient().when(fieldOwnerMirror.toString()).thenReturn("FieldOwner");

    lenient().when(fieldElement.getEnclosingElement()).thenReturn(fieldOwner);

    lenient().when(fieldName.toString()).thenReturn("testField");
    lenient().when(fieldElement.getSimpleName()).thenReturn(fieldName);
    propertyWriterGenerator = new PropertyWriterGenerator(mv, owner,
        elements, types, null);

  }

  @Test
  void handleBasicReferenceProperty_Number() {
    when(types.isAssignable(any(), same(numberMirror))).thenReturn(true);

    propertyWriterGenerator.handleBasicReferenceProperty(new FieldProperty(),
        null);

    verify(mv).visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSON_WRITER_NAME, "value",
        JSON_WRITE_NUMBER_VALUE, false);
  }

  @Test
  void handleBasicReferenceProperty_Boolean() {
    when(types.isAssignable(any(), same(numberMirror))).thenReturn(false);
    when(types.isSameType(same(booleanMirror), any())).thenReturn(true);

    propertyWriterGenerator.handleBasicReferenceProperty(new FieldProperty(),
        null);

    verify(mv).visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSON_WRITER_NAME, "value",
        JSON_WRITE_BOOLEAN_VALUE, false);
  }

  @Test
  void handleBasicReferenceProperty_String() {
    when(types.isAssignable(any(), same(numberMirror))).thenReturn(false);
    when(types.isSameType(same(booleanMirror), any())).thenReturn(false);
    when(types.isSameType(same(stringMirror), any())).thenReturn(true);

    propertyWriterGenerator.handleBasicReferenceProperty(new FieldProperty(),
        null);

    verify(mv).visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSON_WRITER_NAME, "value",
        JSON_WRITE_STRING_VALUE, false);
  }

  /**
   * This is probably most important test for handleBasicReferenceProperty,
   * as all other cases can easily are (and should be) caught by integration tests.
   */
  @Test
  void handleBasicReferenceProperty_Unhandled() {
    when(types.isAssignable(any(), same(numberMirror))).thenReturn(false);
    when(types.isSameType(same(booleanMirror), any())).thenReturn(false);
    when(types.isSameType(same(stringMirror), any())).thenReturn(false);

    final FieldProperty fieldProperty = new FieldProperty();
    fieldProperty.setFieldElement(fieldElement);

    PicosonGeneratorException ex = assertThrows(
        PicosonGeneratorException.class, () ->
            propertyWriterGenerator.handleBasicReferenceProperty(fieldProperty,
                null));

    // Check if we catch some basic information about problem
    assertTrue(ex.getMessage().matches(".*FieldOwner::testField.*internal error.*"));
  }
}