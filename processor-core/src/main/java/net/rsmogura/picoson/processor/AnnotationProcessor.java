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
import net.rsmogura.picoson.annotations.Json;
import net.rsmogura.picoson.processor.javac.ReaderMethodGenerator;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileManager;
import java.util.Set;

/**
 * Entry class for processing annotations.
 */
@SupportedAnnotationTypes("net.rsmogura.picoson.*")
public class AnnotationProcessor extends AbstractProcessor {
  private JavacProcessingEnvironment javacProcessingEnv;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);

    javacProcessingEnv = (JavacProcessingEnvironment) this.processingEnv;

    addClassTransformationHandlers(javacProcessingEnv);
  }

  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    ReaderMethodGenerator readerMethodGenerator = new ReaderMethodGenerator(javacProcessingEnv);

    roundEnv
        .getElementsAnnotatedWith(Json.class)
        .forEach(e -> new ReaderMethodGenerator(javacProcessingEnv).createReaderMethod(e));

    return true;
  }

  protected void addClassTransformationHandlers(JavacProcessingEnvironment javacProcessingEnv) {
    final JavacTask currentTask = JavacTask.instance(this.processingEnv);
    final JavaFileManager fileManager = javacProcessingEnv.getContext().get(JavaFileManager.class);

    final PicosonTransformJavacTaskListener picosonTransformJavacTaskListener =
            new PicosonTransformJavacTaskListener(fileManager);

    // TODO Can this transform be added multiple times to same JavaC
    // Here the plugin API is used inside annotation processor
    // with different life-cycle - it should be prevented
    // adding twice same transformer as it can impact performance.
    currentTask.addTaskListener(picosonTransformJavacTaskListener);
  }
}
