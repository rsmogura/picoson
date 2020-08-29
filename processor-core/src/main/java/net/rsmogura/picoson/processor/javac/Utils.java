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

import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

public class Utils {

  private final TreeMaker treeMaker;
  private final Names names;
  private final Context context;

  public Utils(JavacProcessingEnvironment processingEnv) {
    this.context = processingEnv.getContext();
    this.treeMaker = TreeMaker.instance(this.context);
    this.names = Names.instance(this.context);
  }

  protected JCTree.JCExpression qualIdentSelectClass(Class clazz) {
    return qualIdentSelect(clazz.getName());
  }

  protected JCTree.JCExpression qualIdentSelect(String qualIdent) {
    String[] idents = qualIdent.split("\\.");
    JCTree.JCExpression last = treeMaker.Ident(names.fromString(idents[0]));

    for (int i = 1; i < idents.length; i++) {
      last = treeMaker.Select(last, names.fromString(idents[i]));
    }

    return last;
  }
}
