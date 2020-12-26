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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileManager;
import net.rsmogura.picoson.generator.core.analyze.PropertiesCollector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

@ExtendWith(MockitoExtension.class)
class PicosonJavacClassTransformerTest {
  @Mock
  ClassVisitor classVisitor;

  @Mock
  TypeElement jsonClassTypeElement;

  @Mock
  Name jsonClassName;

  @Mock
  PropertiesCollector propertiesCollector;

  @Mock
  JavaFileManager javaFileManager;

  @Mock
  Elements elements;

  @Mock
  Types types;

  PicosonJavacClassTransformer transformer;

  /** Field from PicosonJavacClassTransformer. */
  Field isJsonAnnotated, needGenerateInitializer;

  @BeforeEach
  void setup() throws Exception {
    when(elements.getBinaryName(same(jsonClassTypeElement))).thenReturn(jsonClassName);
    when(jsonClassName.toString()).thenReturn("eu.smogura.TestClass$Internal");

    final PicosonJavacClassTransformer transformerInstance = new PicosonJavacClassTransformer(
        Opcodes.ASM8, classVisitor, jsonClassTypeElement, propertiesCollector,
        javaFileManager, elements, types);
    transformer = spy(transformerInstance);

    isJsonAnnotated = PicosonJavacClassTransformer.class.
        getDeclaredField("isJsonAnnotated");
    isJsonAnnotated.setAccessible(true);

    needGenerateInitializer = PicosonJavacClassTransformer.class.
        getDeclaredField("needGenerateInitializer");
    needGenerateInitializer.setAccessible(true);
  }

  @Test
  void visitMethod() throws Exception {
    // Check nothing happens when not json annotated
    isJsonAnnotated.set(transformer, false);
    transformer.visitMethod(0, "<clinit>", null, null, null);
    verify(transformer, never()).createInitializerGenerator(any());

    reset(transformer);

    isJsonAnnotated.set(transformer, true);
    transformer.visitMethod(0, "<not-clinit>", null, null, null);
    assertTrue((boolean) needGenerateInitializer.get(transformer),
        "Initializer should be generated at the end of class visit - visitEnd");
    verify(transformer, never()).createInitializerGenerator(any());

    reset(transformer);

    transformer.visitMethod(0, "<clinit>", null, null, null);
    verify(transformer).createInitializerGenerator(any());
    assertFalse((boolean) needGenerateInitializer.get(transformer),
        "Initializer should not be generated as it already there (should only be extended)");
  }

  @Test
  void visitEnd_notChangesNotJsonClass() throws Exception {
    isJsonAnnotated.set(transformer, false);
    transformer.visitEnd();

    verify(transformer, never()).generateSupportCode();
    verify(transformer, never()).generateObjectWriter(any());
  }
}