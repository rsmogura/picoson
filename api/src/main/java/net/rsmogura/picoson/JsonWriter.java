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

import java.io.IOException;
import java.io.Writer;

/**
 * Provides low-level support for writing JSON.
 */
public class JsonWriter implements AutoCloseable {
  private final net.rsmogura.picoson.gson.JsonWriter jsonWriter;

  protected JsonWriter(net.rsmogura.picoson.gson.JsonWriter jsonWriter) {
    this.jsonWriter = jsonWriter;
  }

  public JsonWriter(Writer writer) {
    this.jsonWriter = new net.rsmogura.picoson.gson.JsonWriter(writer);
  }

  public JsonWriter beginArray() throws IOException {
    try {
      jsonWriter.beginArray();
      return this;
    } catch (IOException ioe) {
      throw new JsonWriteException(ioe);
    }
  }

  public JsonWriter endArray() {
    try {
      jsonWriter.endArray();
      return this;
    } catch (IOException e) {
      throw new JsonWriteException(e);
    }
  }

  public JsonWriter beginObject() {
    try {
      jsonWriter.beginObject();
      return this;
    } catch (IOException e) {
      throw new JsonWriteException(e);
    }
  }

  public JsonWriter endObject() {
    try {
      jsonWriter.endObject();
      return this;
    } catch (IOException e) {
      throw new JsonWriteException(e);
    }
  }

  public JsonWriter name(String name) {
    try {
      jsonWriter.name(name);
      return this;
    } catch (IOException e) {
      throw new JsonWriteException(e);
    }
  }

  public JsonWriter value(String value) {
    try {
      jsonWriter.value(value);
      return this;
    } catch (IOException e) {
      throw new JsonWriteException(e);
    }
  }

  public JsonWriter jsonValue(String value) {
    try {
      jsonWriter.value(value);
      return this;
    } catch (IOException e) {
      throw new JsonWriteException(e);
    }
  }

  public JsonWriter nullValue() {
    try {
      jsonWriter.nullValue();
      return this;
    } catch (IOException e) {
      throw new JsonWriteException(e);
    }
  }

  public JsonWriter value(boolean value) {
    try {
      jsonWriter.value(value);
      return this;
    } catch (IOException e) {
      throw new JsonWriteException(e);
    }
  }

  public JsonWriter value(Boolean value) {
    try {
      jsonWriter.value(value);
      return this;
    } catch (IOException e) {
      throw new JsonWriteException(e);
    }
  }

  public JsonWriter value(double value) {
    try {
      jsonWriter.value(value);
      return this;
    } catch (IOException e) {
      throw new JsonWriteException(e);
    }
  }

  public JsonWriter value(long value) {
    try {
      jsonWriter.value(value);
      return this;
    } catch (IOException e) {
      throw new JsonWriteException(e);
    }
  }

  public JsonWriter value(Number value) {
    try {
      jsonWriter.value(value);
      return this;
    } catch (IOException e) {
      throw new JsonWriteException(e);
    }
  }

  public void flush() {
    try {
      jsonWriter.flush();
    } catch (IOException e) {
      throw new JsonWriteException(e);
    }
  }

  public void close() {
    try {
      // TODO GSON closes underlying writer (it should be changed)
      jsonWriter.close();
    } catch (IOException e) {
      throw new JsonWriteException(e);
    }
  }
}
