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
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCReturn;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import java.util.HashMap;
import net.rsmogura.picoson.abi.JsonObjectDescriptor;
import net.rsmogura.picoson.abi.JsonObjectDescriptorBuilder;
import net.rsmogura.picoson.abi.JsonPropertyDescriptor;
import net.rsmogura.picoson.abi.Names;
import net.rsmogura.picoson.processor.javac.collector.FieldProperty;

public class JsonObjectDescriptorGenerator extends AbstractJavacGenerator {
  /**
   * Default constructor initializes shared services and common objects.
   *
   * @param processingEnv
   */
  public JsonObjectDescriptorGenerator(
      JavacProcessingEnvironment processingEnv) {
    super(processingEnv);
  }

  public void generatePropertyDescriptors(PicosonProcessorContext ctx) {
    JCClassDecl processedClass = ctx.getProcessedClass();
    TreeMaker atMaker = treeMaker.at(processedClass.pos);

    JCVariableDecl objectDescHolder = createAndInitObjectDescHolder(atMaker, ctx);
    processedClass.defs = processedClass.defs.append(objectDescHolder);

    JCMethodDecl initMethodDef = buildInitDescriptorMethod(atMaker, ctx);
    processedClass.defs = processedClass.defs.append(initMethodDef);
  }

  private JCVariableDecl createAndInitObjectDescHolder(TreeMaker atMaker,
      PicosonProcessorContext ctx) {
//    MethodType initMethodType = new MethodType(
//        List.nil(),
//        attr.attribType(utils.qualIdentSelectClass(JsonObjectDescriptor.class), thiz.type.tsym),
//        List.nil(),
//        symtab.methodClass
//    );

    // Create symbol describing variable
    VarSymbol sym = new VarSymbol(
        Flags.STATIC | Flags.FINAL | Flags.PUBLIC | Flags.SYNTHETIC,
        names.fromString(Names.DESCRIPTOR_HOLDER),
        elements.getTypeElement(JsonObjectDescriptor.class.getCanonicalName()).type,
        ctx.getProcessedClass().sym
    );
    ctx.setObjectDescriptorHolder(sym);

    return atMaker.VarDef(
        sym,
        atMaker.Apply(
            List.nil(),
            atMaker.Ident(names.fromString(Names.DESCRIPTOR_INITIALIZER)),
            List.nil()
        )
    );
  }

  protected JCMethodDecl buildInitDescriptorMethod(TreeMaker atMaker, PicosonProcessorContext ctx) {
    return atMaker.MethodDef(
        atMaker.Modifiers( Flags.STATIC | Flags.PROTECTED),
        names.fromString(Names.DESCRIPTOR_INITIALIZER),
        utils.qualIdentSelectClass(JsonObjectDescriptor.class),
        List.<JCTypeParameter>nil(),
        List.<JCVariableDecl>nil(),
        List.<JCExpression>nil(),
        buildInitDescriptorMethodBody(atMaker, ctx),
        null
    );
  }

  protected JCBlock buildInitDescriptorMethodBody(TreeMaker atMaker, PicosonProcessorContext ctx) {
    final JCClassDecl processedClass = ctx.getProcessedClass();
    final Name objDescName = names.fromString("objDesc");
    final JCVariableDecl objDescVar = createDescriptorBuilder(atMaker, objDescName);

    List<JCStatement> stats = List.of(
        objDescVar,
        callSetJsonClass(processedClass, atMaker, objDescVar)
    );

    for (FieldProperty prop : ctx.getProperties().values()) {
      JCNewClass propDescCreate = atMaker.NewClass(
          null,
          List.<JCExpression>nil(),
          utils.qualIdentSelectClass(JsonPropertyDescriptor.class),
          List.of(
              atMaker.Literal(prop.getPropertyName()),
              atMaker.Literal(prop.getFieldSymbol().name.toString()),
              atMaker.Literal(prop.getReadIndex()),
              atMaker.ClassLiteral(processedClass.sym),
              atMaker.Literal(prop.getWriteIndex()),
              atMaker.ClassLiteral(processedClass.sym)
          ),
          null
      );

      // Add property descriptor to list
      JCMethodInvocation addPropertyDescriptor = treeMaker.Apply(List.nil(),
          atMaker.Select(
              atMaker.Ident(objDescName),
              names.fromString("addPropertyDescriptor")),
          List.of(propDescCreate)
      );

      stats = stats.append(atMaker.Exec(addPropertyDescriptor));
    }

    JCReturn buildAndReturn = atMaker.Return(atMaker.Apply(
        List.nil(),
        atMaker.Select(atMaker.Ident(objDescVar.name), names.fromString("build")),
        List.nil()
    ));

    stats = stats.append(buildAndReturn);
    return atMaker.Block(0, stats);
  }

  private JCExpressionStatement callSetJsonClass(JCClassDecl thiz, TreeMaker atMaker,
      JCVariableDecl objDescVar) {
    return atMaker.Exec(atMaker.Apply(
        List.nil(),
        atMaker.Select(atMaker.Ident(objDescVar.name), names.fromString("setJsonClass")),
        List.of(
            atMaker.ClassLiteral(thiz.sym)
        )
    ));
  }

  private JCVariableDecl createDescriptorBuilder(TreeMaker atMaker, Name objDescName) {
    JCNewClass descBuilderNew = atMaker.NewClass(
        null,
        List.nil(),
        utils.qualIdentSelectClass(JsonObjectDescriptorBuilder.class),
        List.nil(),
        null
    );

    JCVariableDecl objDescVar = atMaker.VarDef(atMaker.Modifiers(0), objDescName,
        utils.qualIdentSelectClass(JsonObjectDescriptorBuilder.class),
        descBuilderNew);
    return objDescVar;
  }

}
