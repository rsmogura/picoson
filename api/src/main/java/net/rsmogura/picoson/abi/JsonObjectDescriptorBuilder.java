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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import net.rsmogura.picoson.PicosonClassException;

/**
 * Builder for {@link JsonObjectDescriptor} - simplifies creation of descriptors in
 * generated code, and makes code footprint smaller;
 */
public class JsonObjectDescriptorBuilder {

  /** The class described by this descriptor. */
  @Getter
  @Setter
  private Class<?> jsonClass;

  /** Holds descriptor of super class. */
  @Getter
  @Setter
  private JsonObjectDescriptor superDescriptor;

  @Getter
  private final List<JsonPropertyDescriptor> properties = new ArrayList<>();

  public void addPropertyDescriptor(JsonPropertyDescriptor propertyDescriptor) {
    this.properties.add(propertyDescriptor);
  }

  public JsonObjectDescriptor build() {
    if (jsonClass == null) {
      throw new PicosonClassException("The JSON class is not defined");
    }

    HashMap<String, JsonPropertyDescriptor> jsonProperties = new HashMap<>();
    HashMap<String, JsonPropertyDescriptor> internalProperties = new HashMap<>();

    properties.forEach(property -> {
      if (PicosonAbiUtils.isEmpty(property.getJsonPropertyName())) {
        throw new PicosonClassException("Empty JSON property name in class "
          + jsonClass.getName() + " for property " + property.getInternalPropertyName());
      }

      if (jsonProperties.put(property.getJsonPropertyName(), property) != null) {
        throw new PicosonClassException("Json property " + property.getJsonPropertyName()
          + " is already defined in " + jsonClass.getName());
      }

      if (internalProperties.put(property.getJsonPropertyName(), property) != null) {
        throw new PicosonClassException("Property " + property.getJsonPropertyName()
            + " is already defined in " + jsonClass.getName());
      }
    });

    return new JsonObjectDescriptor(jsonClass, superDescriptor, jsonProperties, internalProperties);
  }
}
