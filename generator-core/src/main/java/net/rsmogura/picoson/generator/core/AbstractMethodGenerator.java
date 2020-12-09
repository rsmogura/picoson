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

import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import net.rsmogura.picoson.generator.core.analyze.PropertiesCollector;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class AbstractMethodGenerator {

  protected final MethodVisitor mv;
  protected final Type owner;
  protected final Elements elements;
  protected final PropertiesCollector propertiesCollector;
  protected final GeneratorUtils utils;
  protected final Types typeUtils;

  public AbstractMethodGenerator(
      MethodVisitor mv, Type owner, Elements elements, Types typeUtils,
      PropertiesCollector propertiesCollector) {
    this.mv = mv;
    this.owner = owner;
    this.elements = elements;
    this.typeUtils = typeUtils;
    this.propertiesCollector = propertiesCollector;
    this.utils = new GeneratorUtils(this.elements, this.typeUtils);
  }
}
