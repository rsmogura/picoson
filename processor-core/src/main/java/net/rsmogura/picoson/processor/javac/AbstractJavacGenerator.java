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

import static com.sun.tools.javac.code.Flags.PUBLIC;

import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type.MethodType;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.Flow;
import com.sun.tools.javac.comp.MemberEnter;
import com.sun.tools.javac.comp.Resolve;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import java.lang.reflect.Method;

/**
 * The abstract class for all generators build code on JavaC. <br /> This class & subclasses follows
 * JavaC coding style, which is less object like.
 */
public class AbstractJavacGenerator {

  protected static final Boolean OPENBOX_MODE = true;

  protected final JavacProcessingEnvironment processingEnv;

  protected final Context context;
  protected final TreeMaker treeMaker;
  protected final Trees trees;
  protected final Names names;
  protected final Types types;
  protected final Resolve resolve;
  protected final Attr attr;
  protected final MemberEnter memberEnter;
  protected final Enter enter;
  protected final Symtab symtab;
  protected final Flow flow;

  protected final MethodSymbol objectEquals;

  /**
   * The name of {@link net.rsmogura.picoson.JsonReader} class
   */
  protected final String jsonReaderClassName;

  /**
   * The "result" name used "r" to make code footprint smaller.
   */
  protected final Name resultName;

  protected final Method memberEnterMethod;

  protected final Utils utils;

  /**
   * Default constructor initializes shared services and common objects.
   */
  public AbstractJavacGenerator(JavacProcessingEnvironment processingEnv) {
    this.processingEnv = processingEnv;

    this.context = processingEnv.getContext();
    this.treeMaker = TreeMaker.instance(this.context);
    this.trees = Trees.instance(processingEnv);
    this.names = Names.instance(this.context);
    this.types = Types.instance(this.context);
    this.resolve = Resolve.instance(this.context);
    this.attr = Attr.instance(this.context);
    this.memberEnter = MemberEnter.instance(this.context);
    this.enter = Enter.instance(this.context);
    this.symtab = Symtab.instance(this.context);
    this.flow = Flow.instance(this.context);
    this.utils = new Utils(processingEnv);

    this.objectEquals = new MethodSymbol(
        PUBLIC,
        names.equals,
        new MethodType(
            List.of(symtab.objectType),
            symtab.booleanType,
            List.nil(),
            symtab.methodClass),
        symtab.objectType.tsym);
    try {
      this.memberEnterMethod =
          this.memberEnter.getClass().getDeclaredMethod("memberEnter", JCTree.class, Env.class);
      this.memberEnterMethod.setAccessible(true);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }

    // We keep this name hardcoded here, in case someone would do refactor.
    // As everything is statically compiled, the JSON Reader can't change
    // location, name, nor exposed methods.
    this.jsonReaderClassName = "net.rsmogura.picoson.JsonReader";

    this.resultName = this.names.fromString("r");
  }


}
