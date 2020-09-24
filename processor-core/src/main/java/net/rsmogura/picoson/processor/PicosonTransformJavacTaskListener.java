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

import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import net.rsmogura.picoson.generator.core.PicosonGeneratorException;
import net.rsmogura.picoson.generator.core.PicosonJavacClassTransformer;
import net.rsmogura.picoson.generator.core.analyze.PropertiesCollector;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

/** Initiates transform of single JSON. */
class PicosonTransformJavacTaskListener implements TaskListener {
  // TODO In turn used only to print logs in Property collectors - is factoring ok?
  private final ProcessingEnvironment processingEnvironment;
  private final JavaFileManager fileManager;

  public PicosonTransformJavacTaskListener(
      ProcessingEnvironment processingEnvironment,
      JavaFileManager fileManager) {
    this.processingEnvironment = processingEnvironment;
    this.fileManager = fileManager;
  }

  @Override
  public void finished(TaskEvent e) {
    if (e.getKind() == TaskEvent.Kind.GENERATE) {
      try {
        transformClass(e, fileManager);
      } catch (PicosonGeneratorException pex) {
        throw pex;
      } catch (Exception ex) {
        throw new PicosonGeneratorException(ex);
      }
    }
  }

  /** Transform class basing on event. Extracts output file object basing on event. */
  protected void transformClass(TaskEvent e, JavaFileManager fileManager) throws IOException {
    // TODO Add tests for inner classes

    JavaFileObject javaClassFile =
        fileManager.getJavaFileForOutput(
            StandardLocation.CLASS_OUTPUT,
            // TODO Flat name (inner classes)
            e.getTypeElement().getQualifiedName().toString(),
            JavaFileObject.Kind.CLASS,
            e.getSourceFile());

    try (InputStream classIn = javaClassFile.openInputStream()) {
      PropertiesCollector propertiesCollector = new PropertiesCollector(processingEnvironment);
      propertiesCollector.collectProperties(e.getTypeElement());

      ClassReader classReader = new ClassReader(classIn);
      ClassWriter classWriter =
          new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
      PicosonJavacClassTransformer picosonJavacClassTransformer =
          new PicosonJavacClassTransformer(Opcodes.ASM5, classWriter,
              propertiesCollector, processingEnvironment.getElementUtils());
      classReader.accept(picosonJavacClassTransformer, 0);

      try (OutputStream out = javaClassFile.openOutputStream()) {
        out.write(classWriter.toByteArray());
      }
    }
  }
}
