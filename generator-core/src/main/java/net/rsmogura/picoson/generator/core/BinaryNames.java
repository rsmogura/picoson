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

import static org.objectweb.asm.Type.BOOLEAN_TYPE;
import static org.objectweb.asm.Type.INT_TYPE;
import static org.objectweb.asm.Type.LONG_TYPE;

import net.rsmogura.picoson.JsonReader;
import net.rsmogura.picoson.abi.JsonPropertyDescriptor;
import org.objectweb.asm.Type;

/**
 * Contains binary names and descriptors used to generate code.
 */
public final class BinaryNames {
  /** Descriptor for {@link net.rsmogura.picoson.abi.Names#READ_PROPERTY_NAME} */
  public static final String READ_PROPERTY_DESCRIPTOR =
      Type.getMethodDescriptor(BOOLEAN_TYPE,
          Type.getType(JsonPropertyDescriptor.class), Type.getType(JsonReader.class));


  public static final String JSON_READER_NAME = Type.getInternalName(JsonReader.class);


  /** Internal name for {@link net.rsmogura.picoson.abi.JsonPropertyDescriptor} */
  public static final String JSON_PROPERTY_DESCRIPTOR_NAME =
      Type.getInternalName(JsonPropertyDescriptor.class);


  /**
   * Method descriptor for obtaining reader index
   * {@link JsonPropertyDescriptor#getReadPropertyIndex()}
   */
  public static final String GET_READ_INDEX_DESCRIPTOR;

  /** No arg method, returning boolean. */
  public static final String BOOL_RETURNING_METHOD = Type.getMethodDescriptor(BOOLEAN_TYPE);

  /** No arg method, returning int. */
  public static final String INT_RETURNING_METHOD = Type.getMethodDescriptor(INT_TYPE);

  /** No arg method, returning long. */
  public static final String LONG_RETURNING_METHOD = Type.getMethodDescriptor(LONG_TYPE);

  /** No arg method, returning String. */
  public static final String STRING_RETURNING_METHOD =
      Type.getMethodDescriptor(Type.getType(String.class));


  static {
    try {
      GET_READ_INDEX_DESCRIPTOR = Type.getMethodDescriptor(
          JsonPropertyDescriptor.class.getDeclaredMethod("getReadPropertyIndex"));
    } catch (NoSuchMethodException e) {
      throw new PicosonGeneratorException("Error while initializing class", e);
    }
  }
}
