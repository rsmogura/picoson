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

import static javax.lang.model.type.TypeKind.DECLARED;
import static org.objectweb.asm.Opcodes.IF_ICMPNE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.ISTORE;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import net.rsmogura.picoson.annotations.Json;
import net.rsmogura.picoson.generator.core.analyze.FieldProperty;
import net.rsmogura.picoson.generator.core.analyze.PropertiesCollector;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public abstract class PropertyAbstractGenerator extends AbstractMethodGenerator {
  protected static final int PARAM_THIS = 0;
  protected static final int PARAM_DESC = 1;
  protected static final int PARAM_READER_WRITER = 2;
  protected static final int PARAM_DESCRIPTOR_IDX = 3;

  /**
   * The basic boxed types, are types which have a support in reader and
   * writer.
   */
  protected static final Set<String> BASIC_BOXED_TYPES = new HashSet<>(Arrays.asList(
      Byte.class.getName(),
      Short.class.getName(),
      Integer.class.getName(),
      Long.class.getName(),

      Boolean.class.getName(),

      Float.class.getName(),
      Double.class.getName(),

      String.class.getName()
  ));

  public PropertyAbstractGenerator(MethodVisitor mv, Type owner,
      Elements elements,
      Types typeUtils, PropertiesCollector propertiesCollector) {
    super(mv, owner, elements, typeUtils, propertiesCollector);
  }

  public void generate() {
    getPropertyId();
    mv.visitVarInsn(ISTORE, PARAM_DESCRIPTOR_IDX);

    // TODO This is if-else-if block, which is terrible slow, for large number of properties
    //      This should be changed to BST.
    for (FieldProperty fp : propertiesCollector.getJsonProperties().values()) {
      final Label elseBlock = new Label();
      mv.visitVarInsn(ILOAD, PARAM_DESCRIPTOR_IDX);
      mv.visitLdcInsn(getPropertyIndexForCompare(fp));
      mv.visitJumpInsn(IF_ICMPNE, elseBlock);

      // Normal block
      handleProperty(fp);
      mv.visitLdcInsn(true);
      mv.visitInsn(Opcodes.IRETURN);

      // Begin else block. Because we have return in every true-block
      // no need to generate skip all label, to jump to the end of if-else
      // tree.
      mv.visitLabel(elseBlock);
    }
    mv.visitLabel(new Label());
    mv.visitLdcInsn(false);
    mv.visitInsn(Opcodes.IRETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  /**
   * Obtain property id. The property id can differ depending if it's for read or
   * write.
   * <br />
   * This method is responsible for obtaining property id from current local variables
   * and to put it on stack.
   *
   * @see #getPropertyIndexForCompare(FieldProperty)
   */
  protected abstract void getPropertyId();

  /**
   * Obtains property id from descriptor. The property id can differ depending if it's for read or *
   * write.
   * <br />
   * This template method is counterpart for {@link #getPropertyId()} and it's used
   * to get constant value to be embed in code for comparision.
   *
   * @see #getPropertyId()
   */
  protected abstract int getPropertyIndexForCompare(FieldProperty fp);

  protected void handleProperty(FieldProperty fieldProperty) {
    final VariableElement fieldElement = fieldProperty.getFieldElement();
    final TypeMirror propertyType = fieldElement.asType();
    final TypeKind typeKind = propertyType.getKind();

    if (typeKind == DECLARED) {
      final DeclaredType declaredType = (DeclaredType) propertyType;
      handleReferenceProperty(fieldProperty, declaredType);
    } else if (typeKind.isPrimitive()) {
      handlePrimitiveProperty(fieldProperty, propertyType);
    }
  }

  protected abstract void handlePrimitiveProperty(FieldProperty fieldProperty,
      TypeMirror propertyType);

  protected void handleReferenceProperty(FieldProperty fieldProperty,
      DeclaredType declaredType) {
    final TypeElement typeElement = (TypeElement) declaredType.asElement();
    final Name binaryName = elements.getBinaryName(typeElement);

    if (BASIC_BOXED_TYPES.contains(binaryName.toString())) {
      preHandleReferenceProperty(fieldProperty, declaredType);
      handleBasicReferenceProperty(fieldProperty, declaredType);
      postHandleReferenceProperty(fieldProperty, declaredType);
    } else if (isJsonClass(typeElement)) {
      preHandleComplexProperty(fieldProperty, declaredType);
      handleComplexProperty(fieldProperty, declaredType);
      postHandleReferenceProperty(fieldProperty, declaredType);
    } else {
      throw new PicosonGeneratorException("Unsupported type " + binaryName
        + " for field " + fieldProperty.getPropertyName()
        + " at class " + fieldProperty.getFieldElement().getEnclosingElement().getSimpleName());
    }
  }

  /**
   * Checks if this is JSON class, which should have reader and writer associated with it.
   */
  protected boolean isJsonClass(TypeElement typeElement) {
    final Json jsonAnnotation = typeElement.getAnnotation(Json.class);
    return jsonAnnotation != null;
  }

  protected abstract void preHandleReferenceProperty(FieldProperty fieldProperty,
      DeclaredType declaredType);
  protected abstract void preHandleComplexProperty(FieldProperty fieldProperty,
      DeclaredType declaredType);

  protected abstract void postHandleReferenceProperty(FieldProperty fieldProperty,
      DeclaredType declaredType);

  protected abstract void handleBasicReferenceProperty(FieldProperty fieldProperty,
      DeclaredType declaredType);

  /**
   * Handles complex type, like a class (typically invokes reader / writer for this class).
   */
  protected abstract void handleComplexProperty(FieldProperty fieldProperty,
      DeclaredType declaredType);
}
