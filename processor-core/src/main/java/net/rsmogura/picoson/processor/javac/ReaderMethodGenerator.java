package net.rsmogura.picoson.processor.javac;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import net.rsmogura.picoson.Constants;

import javax.lang.model.element.Element;

/**
 * Generates reader methods.
 */
public class ReaderMethodGenerator extends AbstractJavacGenerator {

  /** The name of parameter used for passing JSON Reader to reader method. */
  private final Name jsonReadParam;

  /**
   * Default constructor.
   */
  public ReaderMethodGenerator(JavacProcessingEnvironment processingEnv) {
    super(processingEnv);

    jsonReadParam = names.fromString("jr");
  }

  /**
   * Creates reader method add adds it to class.
   * This method can be used to read object from JSON.
   */
  public void createReaderMethod(Element element) {
    JCTree.JCClassDecl classDecl = (JCTree.JCClassDecl) trees.getTree(element);
    JCTree.JCMethodDecl jsonRead = buildMethodDeclaration(classDecl.sym);
    classDecl.defs = classDecl.defs.append(jsonRead);
  }

  private JCTree.JCMethodDecl buildMethodDeclaration(ClassSymbol element) {
    final long jsonReadFlags =
      Flags.PUBLIC | Flags.STATIC | (OPENBOX_MODE ? 0 : Flags.SYNTHETIC);

    final BuildContext ctx = new BuildContext();
    ctx.forStatic = true;
    ctx.resultSymbol = element;

    JCTree.JCExpression returnType = treeMaker.Ident(element);
    List<JCVariableDecl> params = List.of(
            treeMaker.VarDef(
                    treeMaker.Modifiers(Flags.PARAMETER | Flags.FINAL),
                    this.jsonReadParam,
                    this.makeQualIdentSelect(this.jsonReaderClassName),
                    null
            )
    );
    List<JCTree.JCTypeParameter> typeParams = List.nil(); // Nothing like this
    List<JCTree.JCExpression> throwsList = List.nil();

    JCTree.JCBlock block = treeMaker.Block(0, this.buildJsonRead(ctx));

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
   * Builds the result variable definition.
   */
  protected JCVariableDecl addResultVarDef(final BuildContext ctx) {
    JCTree.JCExpression init;
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

  protected List<JCStatement> buildJsonRead(BuildContext ctx) {
    List<JCStatement> result = List.nil();
    result = result.append(this.addResultVarDef(ctx));

    // If it's going to be static method, than add return;
    if (ctx.forStatic) {
      result = result.append(treeMaker.Return(treeMaker.Literal(TypeTag.BOT, null)));
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
