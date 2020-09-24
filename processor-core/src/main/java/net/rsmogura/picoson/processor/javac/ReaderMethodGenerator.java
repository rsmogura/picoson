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
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
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
import java.util.Map;
import javax.lang.model.element.Element;
import net.rsmogura.picoson.abi.JsonPropertyDescriptor;
import net.rsmogura.picoson.abi.Names;
import net.rsmogura.picoson.JsonToken;
import net.rsmogura.picoson.generator.core.analyze.FieldProperty;
import net.rsmogura.picoson.generator.core.analyze.PropertiesCollector;

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

  private PicosonProcessorContext processorContext;

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
    // TODO This class mixes instance state, helpers, context, and passing by method arguments
    //      This is correct & works due to Javac internals, however code should stick to one
    //      convention (especially if it's ends to be POC)
    processedClass = (JCClassDecl) trees.getTree(element);
    processorContext = new PicosonProcessorContext(processedClass);

    // Build JSON properties
    jsonProperties = new PropertiesCollector(this.processingEnv)
        .collectProperties(processedClass.sym)
        .getJsonProperties();
    processorContext.setProperties(jsonProperties);

    // TODO Make generator instance field
    new JsonObjectDescriptorGenerator(processingEnv).generatePropertyDescriptors(processorContext);

    JCMethodDecl delegatedProcessProperty = jsonPropertyReader();
    processedClass.defs = processedClass.defs.append(delegatedProcessProperty);

    JCMethodDecl jsonRead = createJsonRead(processedClass.sym, processedClass.pos);
    processedClass.defs = processedClass.defs.append(jsonRead);
  }

  private JCMethodDecl createJsonRead(ClassSymbol element, int pos) {
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

    JCTree.JCBlock block = treeMaker.Block(0, jsonReadBody(ctx, jsonReaderVar.name));

    return treeMaker.MethodDef(
        treeMaker.Modifiers(jsonReadFlags),
        names.fromString(Names.GENERATED_DESERIALIZE_METHOD_NAME),
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
   * Builds method to deserialize single JSON property. Built method chooses by property index
   * what actual property should be de-serialized and how.
   */
  protected JCMethodDecl jsonPropertyReader() {
    final Name descriptorParam = names.fromString("$p"); // Internal name of parameter to hold property being read
    final Name readIndex = names.fromString("$ridx");

    final long methodFlags = Flags.PUBLIC | (OPENBOX_MODE ? 0 : Flags.SYNTHETIC);
    JCExpression returnType = treeMaker.Type(symtab.booleanType);
    JCVariableDecl descriptorParamVar = treeMaker.at(processedClass.pos).VarDef(
        treeMaker.Modifiers(Flags.PARAMETER | Flags.FINAL),
        descriptorParam, //Param name
        this.utils.qualIdentSelectClass(JsonPropertyDescriptor.class),
        null
    );

    // Need to pass position - otherwise compilation can fail due to lack of parameter address
    JCVariableDecl jsonReaderVarTree = readUtils.jsonReaderMethodParam(processedClass.pos);

    List<JCVariableDecl> params = List.of(descriptorParamVar, jsonReaderVarTree);

    List<JCTypeParameter> typeParams = List.nil();
    List<JCExpression> throwsList = List.nil();
    List<JCStatement> statements = List.nil();

    // Create variable to hold read property index
    final JCMethodInvocation getReadPropertyIndex = treeMaker.Apply(List.nil(),
        treeMaker.Select(treeMaker.Ident(descriptorParam), names.fromString("getReadPropertyIndex")),
        List.nil()
    );
    final JCVariableDecl readIndexVar = treeMaker.VarDef(
        treeMaker.Modifiers(0),
        readIndex,
        treeMaker.Type(symtab.intType),
        getReadPropertyIndex);

    statements = statements.append(readIndexVar);

    // Create code to go through the list of known properties
    // and match those properties by index
    // TODO This tree is not performant as it simple if-equals-else-if-equals.. - better algo needed
    for (FieldProperty fieldProperty : jsonProperties.values()) {
      JCIf jcIf = treeMaker.If(
          propertyMatches(treeMaker.Ident(readIndex), fieldProperty),
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
        names.fromString(Names.READ_PROPERTY_NAME),
        returnType,
        typeParams,
        params,
        throwsList,
        block,
        null);

  }

  /**
   * Builds tree to check if passed property matches current property
   */
  protected JCExpression propertyMatches(JCExpression readIndex, FieldProperty fieldProperty) {
    // XXX This method will not work in case of class hierarchy!!! Property indices can varry
    // Find index from passed descriptor (runtime)

    // Return compare tree, to check if indices match
    return treeMaker.Binary(Tag.EQ,
        readIndex,
        treeMaker.Literal(fieldProperty.getReadIndex())
    );
  }

  protected JCBlock readAndSetProperty(FieldProperty fieldProperty, Name readerVar) {
    final Type propertyType = (Type) fieldProperty.getFieldElement().asType();
    final JCExpression valueRead;

    if (propertyType == symtab.stringType) {
      valueRead = readUtils.callJsonReaderMethod(readerVar, "nextString");
    } else if (propertyType == symtab.intType) {
      valueRead = readUtils.callJsonReaderMethod(readerVar, "nextInt");
    } else if (propertyType == symtab.booleanType) {
      valueRead = readUtils.callJsonReaderMethod(readerVar, "nextBoolean");
    } else {
//      this.processingEnv.getMessager().printMessage(ERROR, "Unsupported type " + propertyType);
      return treeMaker.Block(0, List.of(treeMaker.Return(treeMaker.Literal(false))));
//      throw new IllegalStateException("Unsupported type " + propertyType);
    }

    return treeMaker.Block(0,
        List.of(
            treeMaker.Exec(treeMaker.Assign(
                // Need to prefix with this, not to conflict with param names
                treeMaker.Select(
                    treeMaker.Ident(names._this),
                    (VarSymbol) fieldProperty.getFieldElement()
                ),
                valueRead
            )),
            treeMaker.Return(treeMaker.Literal(true))
        )
    );
  }

  protected List<JCStatement> jsonReadBody(BuildContext ctx, Name jsonReaderParam) {
    final Name descriptorsMap = names.fromString("$dscm");
    final Name currentDescriptor = names.fromString("$pdsc");

    // The variable to hold target. Depending on context can be fresh instance
    // or this (however constructor creators, are abandoned, so just leftover).
    final JCVariableDecl targetVar = this.readTargetVariable(ctx);
    // Holds descriptor from static filed (avoids getfield)
    final JCVariableDecl descriptorsMapVar = getJsonProperties(descriptorsMap);
    // Updated with every iteration with descriptor found on map
    final JCVariableDecl currentDescriptorVar = createPropertyDescriptorVar(currentDescriptor);

    List<JCStatement> result = List.of(targetVar, descriptorsMapVar, currentDescriptorVar);
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

    /////////////////////////////////////////////////////
    // Get descriptor and assign it to $pdscm
    // Output: assign expression
    ////////////////////////////////////////////////////

    // Gets next name from JSON stream
    final JCAssign descriptorVarAssign = getDescriptorFromMapAndVarStore(
        jsonReaderParam, descriptorsMap, currentDescriptor);

    // Calls single property with descriptor
    final JCExpressionStatement executeReadSingleProperty = readSinglePropertyWithDescriptor(
        jsonReaderParam, currentDescriptor, targetVar);

    // Execute read only if property descriptor has been found (JSON property supported
    // by class chain)
    final JCIf isPropertyDefinedThanRead = treeMaker.If(
        treeMaker.Binary(
            Tag.NE,
            descriptorVarAssign,
            treeMaker.Literal(TypeTag.BOT, null)
        ),
        executeReadSingleProperty,
        null
    );

    // Define while loop `while (reader.
    final JCStatement propertyReadLoop = treeMaker.WhileLoop(
        nextTokenName, isPropertyDefinedThanRead);

    result = result.append(propertyReadLoop);
    result = result.append(treeMaker.Exec(
        readUtils.callJsonReaderMethod(jsonReaderParam, "endObject")));
    // If it's going to be static method, than add return;
    if (ctx.forStatic) {
      result = result.append(treeMaker.Return(treeMaker.Ident(targetVar.name)));
    }

    return result;
  }

  private JCExpressionStatement readSinglePropertyWithDescriptor(Name jsonReaderParam,
      Name currentDescriptor, JCVariableDecl targetVar) {
    final JCExpression propertyReadMethod = treeMaker.Select(
        treeMaker.Ident(targetVar.getName()),
        names.fromString(Names.READ_PROPERTY_NAME));
    final JCExpressionStatement executeReadSingleProperty = treeMaker.Exec(
        treeMaker.Apply(List.nil(),
          propertyReadMethod,
          List.of(treeMaker.Ident(currentDescriptor), treeMaker.Ident(jsonReaderParam))
      )
    );
    return executeReadSingleProperty;
  }

  private JCAssign getDescriptorFromMapAndVarStore(Name jsonReaderParam, Name descriptorsMap,
      Name currentDescriptor) {
    final JCExpression nextPropertyName =
        readUtils.callJsonReaderMethod(jsonReaderParam, "nextName");

    // Calls $dscm.get(propertyName)
    final JCMethodInvocation getDescriptor = treeMaker.Apply(List.nil(),
        treeMaker.Select(treeMaker.Ident(descriptorsMap), names.fromString("get")),
        List.of(nextPropertyName));
    // $pdsc = $dscm.get(propertyName) - however assignment will happen in if

    final JCAssign descriptorVarAssign =
        treeMaker.Assign(treeMaker.Ident(currentDescriptor), getDescriptor);
    return descriptorVarAssign;
  }

  private JCVariableDecl getJsonProperties(Name descriptorVarName) {
    return treeMaker.at(processedClass).VarDef(
        treeMaker.Modifiers(0),
        descriptorVarName,
        treeMaker.TypeApply(
            utils.qualIdentSelectClass(Map.class),
            List.of(
                utils.qualIdentSelectClass(String.class),
                utils.qualIdentSelectClass(JsonPropertyDescriptor.class)
            )
        ),
        treeMaker.Apply(List.nil(),
            treeMaker.Select(
                treeMaker.Ident(processorContext.getObjectDescriptorHolder()),
                names.fromString("getJsonProperties")
            ),
            List.nil()
        )
    );
  }

  private JCVariableDecl createPropertyDescriptorVar(Name propertyDescName) {
    return treeMaker.at(processedClass).VarDef(
        treeMaker.Modifiers(0),
        propertyDescName,
        utils.qualIdentSelectClass(JsonPropertyDescriptor.class),
        null
    );
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
