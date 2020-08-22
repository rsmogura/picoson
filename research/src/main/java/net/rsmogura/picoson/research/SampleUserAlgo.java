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

package net.rsmogura.picoson.research;

import net.rsmogura.picoson.gson.JsonReader;
import net.rsmogura.picoson.JsonToken;

import java.io.IOException;

public class SampleUserAlgo {
  private String userName;
  private String email;

  private void $readJson(JsonReader reader) {
    try {
      reader.beginObject();
      while (reader.peek() != JsonToken.END_OBJECT) {
        String propertyName = reader.nextName();
        if ("userName".equals(propertyName)) {
          this.userName = reader.nextString();
        } else if ("email".equals(propertyName)) {
          this.email = reader.nextString();
        }
      }
    }catch(IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  public static SampleUserAlgo readJson(JsonReader reader) {
    SampleUserAlgo sampleUserAlgo = new SampleUserAlgo();
    sampleUserAlgo.$readJson(reader);
    return sampleUserAlgo;
  }
}
