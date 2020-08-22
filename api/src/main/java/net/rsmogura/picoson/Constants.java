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

package net.rsmogura.picoson;

public final class Constants {
  private Constants() {

  }

  /** The name of synthetic method used to read JSON. */
  public static final String GENERATED_DESERIALIZE_METHOD_NAME = "jsonRead";

  /** The name of internal deserialize method. This is instance method
   * and it may be always present (i.e. in situations when builders
   * or constructor params are used).
   * <br />
   * However, this method is used in "standard" situation when
   * class hierarchy has to maintained.
   */
  public static final String INSTANCE_DESERIALIZE_METHOD_NAME = "$jsonRead";

  public static final String READ_PROPERTY_NAME = "$jsonReadProp";

  /** The default name of method used to convert object to map. */
  public static final String GENERATED_TO_MAP_METHOD = "toMap";
}
