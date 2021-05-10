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
import static org.objectweb.asm.Type.BYTE_TYPE;
import static org.objectweb.asm.Type.INT_TYPE;
import static org.objectweb.asm.Type.LONG_TYPE;
import static org.objectweb.asm.Type.SHORT_TYPE;
import static org.objectweb.asm.Type.VOID_TYPE;
import static org.objectweb.asm.Type.getDescriptor;
import static org.objectweb.asm.Type.getInternalName;
import static org.objectweb.asm.Type.getMethodDescriptor;
import static org.objectweb.asm.Type.getType;

import java.util.Collection;
import net.rsmogura.picoson.JsonReader;
import net.rsmogura.picoson.JsonSupport;
import net.rsmogura.picoson.JsonToken;
import net.rsmogura.picoson.JsonWriter;
import net.rsmogura.picoson.abi.JsonObjectDescriptor;
import net.rsmogura.picoson.abi.JsonPropertyDescriptor;
import net.rsmogura.picoson.annotations.Json;
import org.objectweb.asm.Type;

/**
 * Contains binary names and descriptors used to generate code.
 */
public final class BinaryNames {
  // TODO (Minor performance) - number of descriptors here can be strings
  //      no need to build those during initialization (it's always some
  //      performance impr. for compilation time)
  /**
   * Descriptor for annotation {@link net.rsmogura.picoson.annotations.Json}.
   */
  public static final String JSON_ANNOTATION = Type.getDescriptor(Json.class);

  /** Descriptor for {@link net.rsmogura.picoson.abi.Names#READ_PROPERTY_NAME} */
  public static final String READ_PROPERTY_DESCRIPTOR =
      getMethodDescriptor(BOOLEAN_TYPE,
          getType(JsonPropertyDescriptor.class), getType(JsonReader.class));

  public static final String JSON_READER_NAME = Type.getInternalName(JsonReader.class);

  /**
   * Descriptor for {@link net.rsmogura.picoson.abi.Names#INSTANCE_SERIALIZE_METHOD_NAME}
   */
  public static final String INSTANCE_SERIALIZE_METHOD_DESC =
      getMethodDescriptor(VOID_TYPE, getType(JsonWriter.class));

  /** Descriptor for {@link net.rsmogura.picoson.abi.Names#WRITE_PROPERTY_NAME} */
  public static final String WRITE_PROPERTY_DESCRIPTOR =
      getMethodDescriptor(BOOLEAN_TYPE,
          getType(JsonPropertyDescriptor.class), getType(JsonWriter.class));

  /** Internal name for {@link JsonWriter}. */
  public static final String JSON_WRITER_NAME = Type.getInternalName(JsonWriter.class);

  /** Internal name for {@link net.rsmogura.picoson.abi.JsonPropertyDescriptor} */
  public static final String JSON_PROPERTY_DESCRIPTOR_NAME =
      Type.getInternalName(JsonPropertyDescriptor.class);

  /**
   * Method descriptor for obtaining reader index
   * {@link JsonPropertyDescriptor#getReadPropertyIndex()}
   */
  public static final String GET_READ_INDEX_DESCRIPTOR;

  /** No arg method, returning Object. */
  public static final String OBJECT_RETURNING_METHOD = getMethodDescriptor(
      getType(Object.class)
  );

  /** No arg method, returning boolean. */
  public static final String BOOL_RETURNING_METHOD = getMethodDescriptor(BOOLEAN_TYPE);

  /** No arg method, returning byte. */
  public static final String BYTE_RETURNING_METHOD = "()B";

  /** No arg method, returning short. */
  public static final String SHORT_RETURNING_METHOD = "()S";

  /** No arg method, returning int. */
  public static final String INT_RETURNING_METHOD = getMethodDescriptor(INT_TYPE);

  /** No arg method, returning long. */
  public static final String LONG_RETURNING_METHOD = getMethodDescriptor(LONG_TYPE);

  /** No arg method, returning float. */
  public static final String FLOAT_RETURNING_METHOD = "()F";

  /** No arg method, returning double. */
  public static final String DOUBLE_RETURNING_METHOD = "()D";

  /** No arg method, returning void. */
  public static final String VOID_METHOD_DESCRIPTOR = "()V";

  public static final String JSON_WRITER_RETURNING_METHOD
      = getMethodDescriptor(getType(JsonWriter.class));

  /** No arg method, returning {@link java.util.Collection}. */
  public static final String COLLECTION_RETURNING_METHOD =
      getMethodDescriptor(getType(Collection.class));

  /** No arg method, returning String. */
  public static final String STRING_RETURNING_METHOD =
      getMethodDescriptor(getType(String.class));

  public static final String JSON_WRITE_STRING_VALUE =
      getMethodDescriptor(getType(JsonWriter.class), getType(String.class));

  public static final String JSON_WRITE_NUMBER_VALUE =
      getMethodDescriptor(getType(JsonWriter.class), getType(Number.class));

  public static final String JSON_WRITE_BOOLEAN_VALUE =
      getMethodDescriptor(getType(JsonWriter.class), getType(Boolean.class));

  public static final String JSON_WRITE_NULL_VALUE =
      getMethodDescriptor(getType(JsonWriter.class));
  /**
   * Descriptor for json() & #json().
   */
  public static final String JSON_SUPPORT_METHOD_DESCRIPTOR =
      getMethodDescriptor(getType(JsonSupport.class));

  public static final String JSON_SUPPORT_DESCRIPTOR = getDescriptor(JsonSupport.class);

  /**
   * Descriptor for {@link JsonObjectDescriptor}
   */
  public static final String JSON_OBJECT_DESCRIPTOR =
      Type.getDescriptor(JsonObjectDescriptor.class);

  /** Internal name for {@link JsonObjectDescriptor}. */
  public static final String JSON_OBJECT_DESCRIPTOR_NAME =
      Type.getInternalName(JsonObjectDescriptor.class);

  public static final String JSON_OBJECT_DESCRIPTOR_GET_PROPERTIES_DESC =
      getMethodDescriptor(getType(JsonPropertyDescriptor[].class));

  /** Method returning {@link net.rsmogura.picoson.JsonToken} */
  public static final String JSON_TOKEN_RETURNING_METHOD =
      Type.getMethodDescriptor(Type.getType(JsonToken.class));

  static {
    try {
      GET_READ_INDEX_DESCRIPTOR = getMethodDescriptor(
          JsonPropertyDescriptor.class.getDeclaredMethod("getReadPropertyIndex"));
    } catch (NoSuchMethodException e) {
      throw new PicosonGeneratorException("Error while initializing class", e);
    }
  }
}
