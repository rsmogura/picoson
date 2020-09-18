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

import net.rsmogura.picoson.abi.Names;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Class transformer supporting JavaC ATP.
 * <br />
 * Even using JavaC API ATP has some limitations, this transformer
 * can help overcome this limitations.
 */
public class PicosonJavacClassTransformer extends ClassVisitor {
  public PicosonJavacClassTransformer(int api, ClassVisitor cv) {
    super(api, cv);
  }

  @Override
  public FieldVisitor visitField(int access, String name, String desc,
                                 String signature, Object value) {
    if (Names.DESCRIPTOR_HOLDER.equals(name)) {
      // Override synthetic
      access = access | Opcodes.ACC_SYNTHETIC;
    }

    FieldVisitor fieldVisitor = super.visitField(access, name, desc, signature, value);
    return fieldVisitor;
  }

  @Override
  public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    return super.visitAnnotation(desc, visible);
  }
}
