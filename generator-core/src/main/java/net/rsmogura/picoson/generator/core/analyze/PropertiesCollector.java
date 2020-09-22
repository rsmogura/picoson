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

package net.rsmogura.picoson.generator.core.analyze;

import java.util.HashMap;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import net.rsmogura.picoson.annotations.JsonProperty;

public class PropertiesCollector {

  private final ProcessingEnvironment processingEnv;

  private final HashMap<String, FieldProperty> jsonProperties = new HashMap<>();

  private int currentReadIndex;
  private int currentWriteIndex;

  /**
   * Default constructor initializes shared services and common objects.
   */
  public PropertiesCollector(ProcessingEnvironment processingEnv) {
    this.processingEnv = processingEnv;
  }

  /**
   * Collects all properties for given class symbol.
   */
  public PropertiesCollector collectProperties(TypeElement classElement) {
    classElement.getEnclosedElements().forEach(this::processElement);
    return this;
  }

  public HashMap<String, FieldProperty> getJsonProperties() {
    return jsonProperties;
  }

  protected void processElement(Element element) {
    if (!validateElement(element)) {
      // Element not valid
      return;
    }

    if (element instanceof VariableElement) {
      processField((VariableElement) element);
    }
  }

  protected void processField(VariableElement varElement) {
    FieldProperty fieldProperty = new FieldProperty();

    JsonProperty annotation = varElement.getAnnotation(JsonProperty.class);
    if (annotation != null && !annotation.value().isEmpty()) {
      fieldProperty.setPropertyName(annotation.value());
    } else {
      fieldProperty.setPropertyName(varElement.getSimpleName().toString());
    }
    fieldProperty.setFieldElement(varElement);
    fieldProperty.setReadIndex(this.currentReadIndex++);
    fieldProperty.setWriteIndex(this.currentWriteIndex++);

    this.jsonProperties.put(fieldProperty.getPropertyName(), fieldProperty);
  }

  /**
   * Validates element, prints information if required.
   *
   * @return true if element is valid
   */
  protected boolean validateElement(Element element) {
    Set<Modifier> modifiers = element.getModifiers();

    // Static elements are not supported by default
    if (modifiers.contains(Modifier.STATIC)) {
      return false;
    }

    if (element instanceof VariableElement) {
      VariableElement varElement = (VariableElement) element;
      if (modifiers.contains(Modifier.FINAL)) {
        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
            String.format("Final fields are not supported right now: %s in %s",
                varElement.getSimpleName(), varElement.getEnclosingElement().getSimpleName())
        );

        // TODO Add exit from annotation processor
        return false;
      }
    }

    return true;
  }

}
