package net.rsmogura.picoson.processor;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;

/** Generates deserialization code */
public class DeserializerGenerator {
  //  private final TreeMaker treeMaker = TreeMaker.instance()
  private final Symbol.ClassSymbol rootClass;

  public DeserializerGenerator(Symbol.ClassSymbol rootClass) {
    this.rootClass = rootClass;
  }

  //  protected JCTree.JCMethodDecl generateConstructor() {
  ////    new JCTree.JCMethodDecl()
  //  }
}
