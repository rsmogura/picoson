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

package net.rsmogura.picoson.processor;

import com.sun.source.util.JavacTask;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.tools.JavaFileManager;

import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnnotationProcessorTest {

  @Spy
  AnnotationProcessor annotationProcessor = new AnnotationProcessor();

  @Mock
  JavacProcessingEnvironment javacProcessingEnvironment;

  @Mock
  Context javacContext;

  @Mock
  JavacTask javacTask;

  @Mock
  JavaFileManager javaFileManager;

  @BeforeEach
  void beforeEach() {
    when(javacProcessingEnvironment.getContext()).thenReturn(javacContext);
    when(javacContext.get(JavacTask.class)).thenReturn(javacTask);
    when(javacContext.get(JavaFileManager.class)).thenReturn(javaFileManager);

  }
  @Test
  void init() {
    annotationProcessor.init(javacProcessingEnvironment);

    doNothing().when(annotationProcessor).addClassTransformationHandlers(same(javacProcessingEnvironment));
    verify(annotationProcessor).addClassTransformationHandlers(same(javacProcessingEnvironment));
  }

  @Test
  void addClassTransformationHandlers() {
    annotationProcessor.addClassTransformationHandlers(javacProcessingEnvironment);
    verify(javacTask).addTaskListener(any(PicosonTransformJavacTaskListener.class));
  }
}