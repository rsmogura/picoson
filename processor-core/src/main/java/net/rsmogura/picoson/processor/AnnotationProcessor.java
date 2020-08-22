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

import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import net.rsmogura.picoson.annotations.Json;
import net.rsmogura.picoson.processor.javac.ReaderMethodGenerator;

/**
 * Entry class for processing annotations.
 */
@SupportedAnnotationTypes("net.rsmogura.picoson.*")
public class AnnotationProcessor extends AbstractProcessor {

  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    JavacProcessingEnvironment javacProcessingEnv = (JavacProcessingEnvironment) this.processingEnv;
    ReaderMethodGenerator readerMethodGenerator = new ReaderMethodGenerator(javacProcessingEnv);

    roundEnv
        .getElementsAnnotatedWith(Json.class)
        .forEach(e -> new ReaderMethodGenerator(javacProcessingEnv).createReaderMethod(e));
    return true;
  }
}
