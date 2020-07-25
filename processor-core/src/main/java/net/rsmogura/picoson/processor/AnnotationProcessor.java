package net.rsmogura.picoson.processor;

import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import net.rsmogura.picoson.annotations.JsonConstructor;
import net.rsmogura.picoson.processor.javac.ReaderMethodGenerator;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@SupportedAnnotationTypes("net.rsmogura.picoson.*")
public class AnnotationProcessor extends AbstractProcessor {
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    JavacProcessingEnvironment javacProcessingEnv = (JavacProcessingEnvironment) this.processingEnv;
    ReaderMethodGenerator readerMethodGenerator = new ReaderMethodGenerator(javacProcessingEnv);

    roundEnv
        .getElementsAnnotatedWith(JsonConstructor.class)
        .forEach(readerMethodGenerator::createReaderMethod);
    return true;
  }
}
