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

import static javax.tools.Diagnostic.Kind.ERROR;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.Tag;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import java.util.HashMap;
import javax.lang.model.element.Element;
import net.rsmogura.picoson.Constants;
import net.rsmogura.picoson.JsonToken;
import net.rsmogura.picoson.processor.javac.collector.FieldProperty;

/**
 * Generates reader methods.
 */
public class ReaderMethodGenerator extends AbstractJavacGenerator {

  /**
   * The name of parameter used for passing JSON Reader to reader method.
   */
  private final Name jsonReadParam;
  private final ReadUtils readUtils;

  private HashMap<String, FieldProperty> jsonProperties;

  private JCClassDecl processedClass;

  /**
   * Default constructor.
   */
  public ReaderMethodGenerator(JavacProcessingEnvironment processingEnv) {
    super(processingEnv);

    readUtils = new ReadUtils(processingEnv);

    jsonReadParam = names.fromString("jr");
  }

  /**
   * Creates reader method add adds it to class. This method can be used to read object from JSON.
   */
  public void createReaderMethod(Element element) {
    processedClass = (JCClassDecl) trees.getTree(element);

    // Build JSON properties
    jsonProperties = new PropertiesCollector(this.processingEnv)
        .collectProperties(processedClass.sym)
        .getJsonProperties();

    JCMethodDecl delegatedProcessProperty = jsonPropertyReader();
    processedClass.defs = processedClass.defs.append(delegatedProcessProperty);

    JCMethodDecl jsonRead = objectReader(processedClass.sym, processedClass.pos);
    processedClass.defs = processedClass.defs.append(jsonRead);
  }

  private JCMethodDecl objectReader(ClassSymbol element, int pos) {
    final long jsonReadFlags =
        Flags.PUBLIC | Flags.STATIC | (OPENBOX_MODE ? 0 : Flags.SYNTHETIC);

    final JCVariableDecl jsonReaderVar = readUtils.jsonReaderMethodParam(pos);

    final BuildContext ctx = new BuildContext();
    ctx.forStatic = true;
    ctx.resultSymbol = element;

    JCExpression returnType = treeMaker.Ident(element);
    List<JCVariableDecl> params = List.of(jsonReaderVar);
    List<JCTypeParameter> typeParams = List.nil(); // Nothing like this
    List<JCExpression> throwsList = List.nil();

    JCTree.JCBlock block = treeMaker.Block(0, this.objectReaderBody(ctx, jsonReaderVar.name));

    return treeMaker.MethodDef(
        treeMaker.Modifiers(jsonReadFlags),
        names.fromString(Constants.GENERATED_DESERIALIZE_METHOD_NAME),
        returnType,
        typeParams,
        params,
        throwsList,
        block,
        null);
  }

  /**
   * Builds the read target variable. Depending on context it can be a new class or {@code this}.
   */
  protected JCVariableDecl readTargetVariable(final BuildContext ctx) {
    JCExpression init;
    if (ctx.forStatic) {
      init = treeMaker.NewClass(null, null,
          treeMaker.Ident(ctx.resultSymbol), List.nil(), null);
    } else {
      init = treeMaker.This(ctx.resultSymbol.type);
    }

    return treeMaker.VarDef(
        treeMaker.Modifiers(Flags.FINAL),
        this.resultName, treeMaker.Ident(ctx.resultSymbol), init);
  }

  /**
   * Builds methods to process JSON property in delegated mode.
   */
  protected JCMethodDecl jsonPropertyReader() {
    final String paramVarName = "$p"; // Internal name of parameter to hold property being read

    final long methodFlags = Flags.PUBLIC | (OPENBOX_MODE ? 0 : Flags.SYNTHETIC);
    JCExpression returnType = treeMaker.Type(symtab.booleanType);
    JCVariableDecl propertyNameVarTree = treeMaker.at(processedClass.pos).VarDef(
        treeMaker.Modifiers(Flags.PARAMETER | Flags.FINAL),
        this.names.fromString(paramVarName), //Param name
        this.utils.qualIdentSelectClass(String.class),
        null
    );

    // Need to pass position - otherwise compilation can fail due to lack of parameter address
    JCVariableDecl jsonReaderVarTree = readUtils.jsonReaderMethodParam(processedClass.pos);

    List<JCVariableDecl> params = List.of(propertyNameVarTree, jsonReaderVarTree);

    List<JCTypeParameter> typeParams = List.nil();
    List<JCExpression> throwsList = List.nil();
    List<JCStatement> statements = List.nil();

    // Create code to go through the list of known properties
    // and match those names with current property to read
    // If name matches set corresponding field
    // TODO This tree is not performant as it simple if-equals-else-if-equals.. - better algo needed
    for (FieldProperty fieldProperty : jsonProperties.values()) {
      JCIf jcIf = treeMaker.If(
          propertyNameEqualsTree(propertyNameVarTree, fieldProperty.getPropertyName()),
          readAndSetProperty(fieldProperty, jsonReaderVarTree.name),
          null
      );

      statements = statements.append(jcIf);
    }

    // Last statement to execute - return false to indicate property was not processed
    statements = statements.append(treeMaker.Return(treeMaker.Literal(false)));

    JCTree.JCBlock block = treeMaker.Block(0, statements);

    return treeMaker.MethodDef(
        treeMaker.Modifiers(methodFlags),
        names.fromString(Constants.READ_PROPERTY_NAME),
        returnType,
        typeParams,
        params,
        throwsList,
        block,
        null);

  }

  /**
   * Builds tree to check if passed property name is equal to given string
   */
  protected JCMethodInvocation propertyNameEqualsTree(JCVariableDecl propertyNameVarTree,
      String value) {
    return treeMaker.App(
        treeMaker.Select(
            treeMaker.Literal(value),
            this.objectEquals),
        List.of(treeMaker.Ident(propertyNameVarTree.name))
    );
  }

  protected JCBlock readAndSetProperty(FieldProperty fieldProperty, Name readerVar) {
    final Type propertyType = fieldProperty.getFieldSymbol().type;
    final JCExpression valueRead;

    if (propertyType == symtab.stringType) {
      valueRead = readUtils.callJsonReaderMethod(readerVar, "nextString");
    } else if (propertyType == symtab.intType) {
      valueRead = readUtils.callJsonReaderMethod(readerVar, "nextInt");
    } else if (propertyType == symtab.booleanType) {
      valueRead = readUtils.callJsonReaderMethod(readerVar, "nextBoolean");
    } else {
      this.processingEnv.getMessager().printMessage(ERROR, "Unsupported type " + propertyType);
      throw new IllegalStateException("Unsupported type " + propertyType);
    }

    return treeMaker.Block(0,
        List.of(
            treeMaker.Exec(treeMaker.Assign(
                // Need to prefix with this, not to conflict with param names
                treeMaker.Select(
                    treeMaker.Ident(names._this),
                    fieldProperty.getFieldSymbol()
                ),
                valueRead
            )),
            treeMaker.Return(treeMaker.Literal(true))
        )
    );
  }

  protected List<JCStatement> objectReaderBody(BuildContext ctx, Name jsonReaderParam) {
    // The variable to hold target. Depending on context can be fresh instance
    // or this (however constructor creators, are abandoned, so just leftover).

    final JCVariableDecl target = this.readTargetVariable(ctx);

    List<JCStatement> result = List.nil();
    result = result.append(target);
    result = result
        .append(treeMaker.Exec(readUtils.callJsonReaderMethod(jsonReaderParam, "beginObject")));

    JCExpression nextTokenName = treeMaker.Binary(
        Tag.EQ,
        readUtils.callJsonReaderMethod(jsonReaderParam, "peek"),
        utils.qualIdentSelect(JsonToken.class.getName() + ".NAME"));
    // Iterate until token is not NAME, if JSON is correct than after this loop,
    // last token should be END_OBJECT, if JSON was incorrectly read, error will be
    // thrown later on unexpected token
    final Name propName = names.fromString("$pn");

    // Gets next name from JSON stream
    final JCExpression nextPropertyName =
        readUtils.callJsonReaderMethod(jsonReaderParam, "nextName");

    // Selector for readProperty method
    final JCExpression propertyReadMethod = treeMaker.Select(
        treeMaker.Ident(target.getName()),
        names.fromString(Constants.READ_PROPERTY_NAME));

    // Identifier for parameter containing JSON Reader
    final JCExpression jsonReader = treeMaker.Ident(jsonReaderParam);
    final JCStatement executeNextPropRead = treeMaker.Exec(treeMaker.Apply(List.nil(),
        propertyReadMethod,
        List.of(nextPropertyName, jsonReader)
    ));
    final JCStatement propertyReadLoop = treeMaker.WhileLoop(nextTokenName, executeNextPropRead);

    result = result.append(propertyReadLoop);
    result = result.append(treeMaker.Exec(
        readUtils.callJsonReaderMethod(jsonReaderParam, "endObject")));
    // If it's going to be static method, than add return;
    if (ctx.forStatic) {
      result = result.append(treeMaker.Return(treeMaker.Ident(target.name)));
    }

    return result;
  }

  private class BuildContext {

    /*
     * If it's for static the variable will be initialized
     * to default constructor, otherwise to this - with such
     * approach constructor and static method code will be consistent
     */
    private boolean forStatic;
    private ClassSymbol resultSymbol;
  }
}
