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

package net.rsmogura.picoson.abi;

/**
 * Holds ABI names, used in Picoson project. Those are names of fields, methods, and
 * other elements used during transformation of classes. As Picoson statically
 * inlines JSON logic into classes, those names (as well generated signatures) should
 * not change to maintain binary compatibility between various classes, and builds.
 */
public final class Names {
  private Names() {

  }

  /**
   * The name of method used to read JSON. This method is public, and it can
   * be used from user-code (as the entry point to deserialize given
   * class). This method is optional.
   */
  public static final String GENERATED_DESERIALIZE_METHOD_NAME = "jsonRead";

  /**
   * The name of internal, synthetic deserialize method. This is a static method
   * and it's typically responsible for setting up class instance and fields.
   * <br />
   * This method is part of ABI (Internal API).
   */
  public static final String INSTANCE_DESERIALIZE_METHOD_NAME = "#jsonRead";

  /**
   * The name of internal, synthetic method to deserialize single property and set
   * the corresponding field / property in object.
   * This method is typically called from {@link #INSTANCE_DESERIALIZE_METHOD_NAME}.
   * <br />
   * This method is part of ABI.
   * <br />
   * This method may not always be present in the class.
   */
  public static final String READ_PROPERTY_NAME = "#jsonReadProp";

  /**
   * The name of internal serialize method.
   * <b>It's part of ABI (Internal API).</b>
   */
  //TODO Align method names with corresponding read!
  public static final String INSTANCE_SERIALIZE_METHOD_NAME = "#jsonWrite";

  /**
   * Name of public method which can be used to serialize instance.
   * This method may not always be present, typically calls
   * {@link #INSTANCE_SERIALIZE_METHOD_NAME}.
   */
  public static final String INSTANCE_SERIALIZE_PUBLIC_METHOD = "jsonWrite";

  /**
   * Default name of public method to return json support class.
   */
  public static final String JSON_SUPPORT_OBTAIN_PUB_METHOD = "json";

  /**
   * Name of internal method used to write property.
   * <br />
   * It is part of ABI.
   * @see #READ_PROPERTY_NAME
   */
  public static final String WRITE_PROPERTY_NAME = "#jsonWriteProp";

  /** The default name of method used to convert object to map. */
  public static final String GENERATED_TO_MAP_METHOD = "toMap";

  /**
   * Method used to initialize object and property descriptors,
   * for a given class. It's static synthetic method.
   */
  public static final String DESCRIPTOR_INITIALIZER = "#jsonInitDesc";

  /**
   * Filed holding object descriptor. This field is typically initialized
   * by {@link #DESCRIPTOR_INITIALIZER}.
   */
  public static final String DESCRIPTOR_HOLDER = "#jsonDesc";

  /**
   * Postfix for support class. The support class consist the name of
   * JSON (model) class and this postfix.
   * <br />
   * ABI: pending decision
   */
  public static final String SUPPORT_CLASS_POSTFIX = "#JsonSupport";

  /**
   * The name of internal synthetic filed to hold single instance
   * of {@link net.rsmogura.picoson.JsonSupport} class.
   * <br />
   * ABI: pending decision
   */
  public static final String SUPPORT_CLASS_HOLDER = "#jsonSupp";

  /**
   * The name of read method implemented by Picoson, to
   * provide user-space ↔️ Picoson internals bridge.
   * @see net.rsmogura.picoson.JsonSupport#
   */
  public static final String SUPPORT_CLASS_READ_IMPL = "readI";

  /**
   * The name of write method implemented by Picoson, to
   * provide user-space ↔️ Picoson internals bridge.
   */
  public static final String SUPPORT_CLASS_WRITE_IMPL = "writeI";
}
