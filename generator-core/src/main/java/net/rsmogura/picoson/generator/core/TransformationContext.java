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

package net.rsmogura.picoson.generator.core;

import static net.rsmogura.picoson.abi.Names.SUPPORT_CLASS_POSTFIX;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import lombok.NonNull;
import net.rsmogura.picoson.generator.core.analyze.PropertiesCollector;

/**
 * Class to carry objects, services, utils etc used during transformation
 * and initialized for given class.
 */
public class TransformationContext {
  protected final Elements elements;
  protected final PropertiesCollector propertiesCollector;
  protected final GeneratorUtils generatorUtils;
  protected final Types typeUtils;
  protected final TypeElement jsonClass;
  protected final String jsonClassInternalName;
  protected final String supportClassInternalName;

  public TransformationContext(
      @NonNull Elements elements,
      @NonNull Types typeUtils,
      @NonNull PropertiesCollector propertiesCollector,
      @NonNull TypeElement jsonClass) {
    this.elements = elements;
    this.propertiesCollector = propertiesCollector;
    this.typeUtils = typeUtils;
    this.jsonClass = jsonClass;;

    this.generatorUtils = new GeneratorUtils(elements, typeUtils);

    this.jsonClassInternalName = generatorUtils.internalName(jsonClass);

    String supportClassName = elements.getBinaryName(jsonClass).toString() + SUPPORT_CLASS_POSTFIX;
    this.supportClassInternalName = supportClassName.replace('.', '/');
  }
}
