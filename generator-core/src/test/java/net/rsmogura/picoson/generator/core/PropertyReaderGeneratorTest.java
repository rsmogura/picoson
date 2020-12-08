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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

@ExtendWith(MockitoExtension.class)
class PropertyReaderGeneratorTest {
  @Mock
  private MethodVisitor mv;

  @Mock
  private Elements elements;

  @Mock
  private Types types;

  @Mock
  private DeclaredType declaredType;

  @Mock
  private TypeElement declaredTypeElement;

  @Mock
  private Name declaredTypeElementSimpleName;

  @Mock
  private Name binaryName;

  private PropertyReaderGenerator readerGenerator;

  private static Type owner = Type.getType("Lmy/test/Type;");

  @BeforeEach
  void setUp() {
    readerGenerator = new PropertyReaderGenerator(mv, owner, elements, types, null);
    lenient().when(declaredType.asElement()).thenReturn(declaredTypeElement);
    lenient().when(declaredTypeElement.getSimpleName()).thenReturn(declaredTypeElementSimpleName);
    lenient().when(binaryName.toString()).thenReturn("my.test.Type$AtTest");
  }

  @Test
  void handleBasicReferenceProperty() {
    when(declaredType.getKind()).thenReturn(DECLARED);
    when(types.erasure(declaredType)).thenReturn(declaredType);
    when(elements.getBinaryName(declaredTypeElement)).thenReturn(binaryName);

    when(declaredTypeElementSimpleName.toString()).thenReturn("Long");
    readerGenerator.handleBasicReferenceProperty(null, declaredType);
    verify(mv).visitMethodInsn(INVOKEVIRTUAL, "net/rsmogura/picoson/JsonReader",
        "nextBoxedLong", "()Lmy/test/Type$AtTest;", false);
    // Note the signature for read method is build from declared type, so it
    //      doesn't have to be ()Ljava/lang/Long;

    // "Special cases"
    when(declaredTypeElementSimpleName.toString()).thenReturn("String");
    readerGenerator.handleBasicReferenceProperty(null, declaredType);
    verify(mv).visitMethodInsn(INVOKEVIRTUAL, "net/rsmogura/picoson/JsonReader",
        "nextString", "()Lmy/test/Type$AtTest;", false);

    when(declaredTypeElementSimpleName.toString()).thenReturn("Integer");
    readerGenerator.handleBasicReferenceProperty(null, declaredType);
    verify(mv).visitMethodInsn(INVOKEVIRTUAL, "net/rsmogura/picoson/JsonReader",
        "nextBoxedInt", "()Lmy/test/Type$AtTest;", false);
  }
}