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

import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sun.source.util.JavacTask;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Context;
import javax.tools.JavaFileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

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
    doReturn(javacTask).when(annotationProcessor).getJavacTaskInstance();

    when(javacProcessingEnvironment.getContext()).thenReturn(javacContext);
    lenient().when(javacContext.get(JavacTask.class)).thenReturn(javacTask);
    when(javacContext.get(JavaFileManager.class)).thenReturn(javaFileManager);

  }
  @Test
  void init() {
    annotationProcessor.init(javacProcessingEnvironment);

    verify(annotationProcessor).addClassTransformationHandlers(same(javacProcessingEnvironment));
  }

  @Test
  void addClassTransformationHandlers() {
    annotationProcessor.addClassTransformationHandlers(javacProcessingEnvironment);
    verify(javacTask).addTaskListener(any(PicosonTransformJavacTaskListener.class));
  }
}