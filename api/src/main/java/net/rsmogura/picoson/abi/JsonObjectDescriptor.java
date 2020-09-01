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

import java.util.Map;
import lombok.Value;

/**
 * Describes JSON object. This class is typically statically instantiated and kept
 * in synthetic field (created during transformation) in JSON object.
 */
@Value
public final class JsonObjectDescriptor {
  /** The class described by this descriptor. */
  private final Class<?> jsonClass;

  /** Holds descriptor of super class. */
  private final JsonObjectDescriptor superDescriptor;

  /** Maps the JSON property to descriptor. */
  private final Map<String, JsonPropertyDescriptor> jsonProperties;

  /** Maps internal property name to descriptor. */
  private final Map<String, JsonPropertyDescriptor> internalProperties;

  public JsonObjectDescriptor(Class<?> jsonClass,
      JsonObjectDescriptor superDescriptor,
      Map<String, JsonPropertyDescriptor> jsonProperties,
      Map<String, JsonPropertyDescriptor> internalProperties) {

    this.jsonClass = jsonClass;
    this.superDescriptor = superDescriptor;
    this.jsonProperties = jsonProperties;
    this.internalProperties = internalProperties;
  }
}
