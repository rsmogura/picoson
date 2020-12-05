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

import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Utility class.
 */
public class GeneratorUtils {
  private final Elements elements;

  public GeneratorUtils(Elements elements) {
    this.elements = elements;
  }

  public String descriptorFromType(TypeElement typeElement) {
    final Name binaryName = elements.getBinaryName(typeElement);
    return "L" + binaryName.toString().replaceAll("\\.", "/") + ";";
  }
}
