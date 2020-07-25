package net.rsmogura.picoson.processor.javac;

import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.*;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import java.lang.reflect.Method;

/**
 * The abstract class for all generators build code on JavaC.
 * <br />
 * This class & subclasses follows JavaC coding style,
 * which is less object like.
 */
public class AbstractJavacGenerator {
  protected static final Boolean OPENBOX_MODE = true;
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

  /**
   * The name of {@link net.rsmogura.picoson.JsonReader} class
   */
  protected final String jsonReaderClassName;

  /** The "result" name used "r" to make code footprint smaller. */
  protected final Name resultName;

  protected final Method memberEnterMethod;

  /**
   * Default constructor initializes shared services and common objects.
   */
  public AbstractJavacGenerator(JavacProcessingEnvironment processingEnv) {
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

  protected JCTree.JCExpression makeQualIdentSelect(String qualIdent) {
    String[] idents = qualIdent.split("\\.");
    JCTree.JCExpression last = treeMaker.Ident(names.fromString(idents[0]));

    for (int i = 1; i < idents.length; i++) {
      last = treeMaker.Select(last, names.fromString(idents[i]));
    }

    return last;
  }
}
