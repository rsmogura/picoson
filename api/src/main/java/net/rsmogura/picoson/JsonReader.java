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
import java.io.Reader;

/**
 * Responsible for reading JSON data in sequential way.
 */
public class JsonReader {
  private final net.rsmogura.picoson.gson.JsonReader gsonReader;

  public void setLenient(boolean lenient) {
    gsonReader.setLenient(lenient);
  }

  public boolean isLenient() {
    return gsonReader.isLenient();
  }

  public JsonReader(Reader in) {
    gsonReader = new net.rsmogura.picoson.gson.JsonReader(in);
  }

  public void beginArray() {
    try {
      gsonReader.beginArray();
    } catch (IOException e) {
      throw new JsonReadException(e);
    }
  }

  public void endArray() {
    try {
      gsonReader.endArray();
    } catch (IOException e) {
      throw new JsonReadException(e);
    }
  }

  public void beginObject() {
    try {
      gsonReader.beginObject();
    } catch (IOException e) {
      throw new JsonReadException(e);
    }
  }

  public void endObject() {
    try {
      gsonReader.endObject();
    } catch (IOException e) {
      throw new JsonReadException(e);
    }
  }

  public boolean hasNext() {
    try {
      return gsonReader.hasNext();
    } catch (IOException e) {
      throw new JsonReadException(e);
    }
  }

  public JsonToken peek() {
    try {
      return gsonReader.peek();
    } catch (IOException e) {
      throw new JsonReadException(e);
    }
  }

  public String nextName() {
    try {
      return gsonReader.nextName();
    } catch (IOException e) {
      throw new JsonReadException(e);
    }
  }

  public String nextString() {
    try {
      if (gsonReader.peek() == JsonToken.NULL) {
        gsonReader.nextNull();
        return null;
      } else {
        return gsonReader.nextString();
      }
    } catch (IOException e) {
      throw new JsonReadException(e);
    }
  }

  public boolean nextBoolean() {
    try {
      return gsonReader.nextBoolean();
    } catch (IOException e) {
      throw new JsonReadException(e);
    }
  }

  public void nextNull() {
    try {
      gsonReader.nextNull();
    } catch (IOException e) {
      throw new JsonReadException(e);
    }
  }

  public double nextDouble() {
    try {
      return gsonReader.nextDouble();
    } catch (IOException e) {
      throw new JsonReadException(e);
    }
  }

  public long nextLong() {
    try {
      return gsonReader.nextLong();
    } catch (IOException e) {
      throw new JsonReadException(e);
    }
  }

  public int nextInt() {
    try {
      return gsonReader.nextInt();
    } catch (IOException e) {
      throw new JsonReadException(e);
    }
  }

  public void close() {
    try {
      gsonReader.close();
    } catch (IOException e) {
      throw new JsonReadException(e);
    }
  }

  public void skipValue() {
    try {
      gsonReader.skipValue();
    } catch (IOException e) {
      throw new JsonReadException(e);
    }
  }

  public String getPath() {
    return gsonReader.getPath();
  }
}
