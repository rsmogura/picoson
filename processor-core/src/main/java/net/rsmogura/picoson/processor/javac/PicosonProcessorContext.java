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

import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import java.util.HashMap;
import lombok.Data;
import net.rsmogura.picoson.processor.javac.collector.FieldProperty;

/**
 * The generation context keeps various data related to generation.
 */
@Data
public class PicosonProcessorContext {
  private JCClassDecl processedClass;

  /**
   * Symbol name to store holder for descriptors, used when referencing it,
   * as synthetic fields can't be reached from code and processor works on
   * attribution phase,
   */
  private VarSymbol objectDescriptorHolder;

  /** Discovered JSON properties. */
  private HashMap<String, FieldProperty> properties;

  public PicosonProcessorContext(JCClassDecl processedClass) {
    this.processedClass = processedClass;
  }
}
