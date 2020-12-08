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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GeneratorUtilsTest {
  @Mock
  Elements elements;

  @Mock
  Types types;

  @Mock
  TypeMirror typeMirror;

  @Mock
  DeclaredType testDeclaredType;

  @Mock
  TypeElement testTypeElement;

  @Mock
  Name testName;

  GeneratorUtils generatorUtils;

  @BeforeEach
  void setup() {
    lenient().when(testName.toString()).thenReturn("test.binaryName.Test$Name");
    lenient().when(elements.getBinaryName(testTypeElement)).thenReturn(testName);
    lenient().when(types.erasure(typeMirror)).thenReturn(testDeclaredType);
    lenient().when(typeMirror.getKind()).thenReturn(TypeKind.DECLARED);
    lenient().when(testDeclaredType.asElement()).thenReturn(testTypeElement);

    generatorUtils = new GeneratorUtils(elements, types);
  }

  @Test
  void descriptorFromType() {
    assertEquals("Ltest/binaryName/Test$Name;",
        generatorUtils.descriptorFromType(testTypeElement));
  }

  @Test
  void descriptorFromTypeMirror() {
    assertEquals("Ltest/binaryName/Test$Name;",
        generatorUtils.descriptorFromTypeMirror(typeMirror));

    // Ensure type has been erased (useful for generics)
    verify(types).erasure(typeMirror);

    // Check primitive types
    verifyKindAgainstSignature("Z", TypeKind.BOOLEAN);
    verifyKindAgainstSignature("B", TypeKind.BYTE);
    verifyKindAgainstSignature("S", TypeKind.SHORT);
    verifyKindAgainstSignature("I", TypeKind.INT);
    verifyKindAgainstSignature("J", TypeKind.LONG);
    verifyKindAgainstSignature("C", TypeKind.CHAR);
    verifyKindAgainstSignature("F", TypeKind.FLOAT);
    verifyKindAgainstSignature("D", TypeKind.DOUBLE);
  }

  private void verifyKindAgainstSignature(String expectedSignature, TypeKind kind) {
    when(typeMirror.getKind()).thenReturn(kind);
    assertEquals(expectedSignature, generatorUtils.descriptorFromTypeMirror(typeMirror));
  }


  /**
   * Checks if exception is thrown for unsupported types
   */
  @Test
  void descriptorFromTypeMirror_Throws() {
    final EnumSet<TypeKind> toCheckKinds = EnumSet.allOf(TypeKind.class);
    toCheckKinds.remove(TypeKind.DECLARED);
    toCheckKinds.removeIf(TypeKind::isPrimitive);

    toCheckKinds.forEach(kind -> {
      when(typeMirror.getKind()).thenReturn(kind);
      assertThrows(PicosonGeneratorException.class, () ->
          generatorUtils.descriptorFromTypeMirror(typeMirror));
    });
  }

  @Test
  void methodDescriptorFromTypeMirror() {
    assertEquals("()Ltest/binaryName/Test$Name;",
        generatorUtils.methodDescriptorFromTypeMirror(typeMirror));
  }
}