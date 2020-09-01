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

import lombok.Value ;

@Value
public class JsonPropertyDescriptor {
  private final String jsonPropertyName;
  private final String internalPropertyName;

  private final int readPropertyIndex;
  private final Class<?> readerClass;

  private final int writePropertyIndex;
  private final Class<?> writerClass;

  public JsonPropertyDescriptor(String jsonPropertyName, String internalPropertyName,
      int readPropertyIndex, Class<?> readerClass, int writePropertyIndex,
      Class<?> writerClass) {
    this.jsonPropertyName = jsonPropertyName;
    this.internalPropertyName = internalPropertyName;
    this.readPropertyIndex = readPropertyIndex;
    this.readerClass = readerClass;
    this.writePropertyIndex = writePropertyIndex;
    this.writerClass = writerClass;
  }
}
