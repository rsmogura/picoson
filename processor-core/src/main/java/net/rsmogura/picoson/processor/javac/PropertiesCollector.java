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
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import java.util.HashMap;
import javax.tools.Diagnostic;
import net.rsmogura.picoson.annotations.JsonProperty;
import net.rsmogura.picoson.processor.javac.collector.FieldProperty;

public class PropertiesCollector extends AbstractJavacGenerator {

  private HashMap<String, FieldProperty> jsonProperties = new HashMap<>();

  private int currentReadIndex;
  private int currentWriteIndex;

  /**
   * Default constructor initializes shared services and common objects.
   */
  public PropertiesCollector(JavacProcessingEnvironment processingEnv) {
    super(processingEnv);
  }

  protected static boolean isUserCodeSymbol(Symbol symbol) {
    return ((symbol.flags() & (Flags.SYNTHETIC | Flags.STATIC)) == 0);
  }

  /**
   * Collects all properties for given class symbol.
   */
  public PropertiesCollector collectProperties(ClassSymbol classSymbol) {
    classSymbol.complete();
    classSymbol.members().getSymbols().forEach(this::collectSymbol);

    classSymbol.getSuperclass().tsym.members().getSymbols().forEach(this::collectSymbol);

    return this;
  }

  public HashMap<String, FieldProperty> getJsonProperties() {
    return jsonProperties;
  }

  protected void collectSymbol(Symbol symbol) {
    // Skip symbols which can't be used in serialization
    // mainly synthetic and static
    if (!isUserCodeSymbol(symbol)) {
      return;
    }

    if (!checkSupportLogError(symbol)) {
      return;
    }

    if (symbol instanceof VarSymbol) {
      VarSymbol varSymbol = (VarSymbol) symbol;
      processFieldProperty(varSymbol);
    } else if (symbol instanceof MethodSymbol) {
      MethodSymbol methodSymbol = (MethodSymbol) symbol;
      // TODO Not supported right now
    }

  }

  protected void processFieldProperty(VarSymbol varSymbol) {
    if ((varSymbol.flags() & (Flags.SYNTHETIC | Flags.STATIC)) == 0) {
      FieldProperty fieldProperty = new FieldProperty();

      JsonProperty annotation = varSymbol.getAnnotation(JsonProperty.class);
      if (annotation != null && !annotation.value().isEmpty()) {
        fieldProperty.setPropertyName(annotation.value());
      } else {
        fieldProperty.setPropertyName(varSymbol.name.toString());
      }
      fieldProperty.setFieldSymbol(varSymbol);
      fieldProperty.setReadIndex(this.currentReadIndex++);
      fieldProperty.setWriteIndex(this.currentWriteIndex++);

      this.jsonProperties.put(fieldProperty.getPropertyName(), fieldProperty);
    }
  }

  protected boolean checkSupportLogError(Symbol symbol) {
    if (symbol instanceof VarSymbol) {
      if ((symbol.flags() & Flags.FINAL) != 0) {
        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
            String.format("Final fields are not supported right now: %s in %s",
                symbol.getSimpleName(), symbol.owner.getSimpleName())
        );

        // TODO Add exit from annotation processor
        return false;
      }
    }

    return true;
  }

}
