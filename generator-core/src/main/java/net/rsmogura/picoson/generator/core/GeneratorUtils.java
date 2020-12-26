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

import static javax.lang.model.type.TypeKind.BOOLEAN;
import static javax.lang.model.type.TypeKind.DECLARED;

import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Utility class.
 */
public class GeneratorUtils {
  private final Elements elements;
  private final Types typeUtils;

  public GeneratorUtils(Elements elements, Types typeUtils) {
    this.elements = elements;
    this.typeUtils = typeUtils;
  }

  public String descriptorFromType(TypeElement typeElement) {
    final Name binaryName = elements.getBinaryName(typeElement);
    return "L" + binaryName.toString().replaceAll("\\.", "/") + ";";
  }

  public String descriptorFromTypeMirror(TypeMirror typeMirror) {
    switch (typeMirror.getKind()) {
      case DECLARED:
        final DeclaredType erasedType = (DeclaredType) typeUtils.erasure(typeMirror);
        return descriptorFromType((TypeElement) erasedType.asElement());
      case BOOLEAN:
        return "Z";
      case BYTE:
        return "B";
      case SHORT:
        return "S";
      case INT:
        return "I";
      case LONG:
        return "J";
      case CHAR:
        return "C";
      case FLOAT:
        return "F";
      case DOUBLE:
        return "D";
      default:
      throw new PicosonGeneratorException("Unsupported type " + typeMirror
        + " when mapping to descriptor. This is internal error worth a bug report");
    }
  }

  public String methodDescriptorFromTypeMirror(TypeMirror returnType, TypeMirror... args) {
    if (args.length > 0) {
      throw new PicosonGeneratorException("Args descriptors not supported yet");
    }

    return "()" + descriptorFromTypeMirror(returnType);
  }

  public String internalName(TypeElement typeElement) {
    final Name binaryName = elements.getBinaryName(typeElement);
    return internalNameFromBinary(binaryName.toString());
  }

  public String internalNameFromBinary(String binaryName) {
    return binaryName.replace('.', '/');
  }
}
