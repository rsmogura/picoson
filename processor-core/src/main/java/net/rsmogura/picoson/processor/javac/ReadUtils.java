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

package net.rsmogura.picoson.processor.javac;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import net.rsmogura.picoson.JsonReader;

/**
 * Utilities for common trees related to reading.
 */
public class ReadUtils extends AbstractJavacGenerator {

  public ReadUtils(JavacProcessingEnvironment processingEnv) {
    super(processingEnv);
  }

  /**
   * Builds tree to call JsonReader no arg methods.
   */
  protected JCExpression callJsonReaderMethod(Name readerVar, String methodName) {
    return treeMaker.Apply(
        null,
        treeMaker.Select(treeMaker.Ident(readerVar), names.fromString(methodName)),
        List.nil()
    );
  }

  /**
   * Generates variable tree which can be used to store {@link JsonReader}.
   */
  protected JCVariableDecl jsonReaderMethodParam(int pos) {
    return treeMaker.at(pos).VarDef(
        treeMaker.Modifiers(Flags.PARAMETER | Flags.FINAL),
        names.fromString("$jr"), //Param name
        utils.qualIdentSelect(JsonReader.class.getName()),
        null
    );
  }
}
