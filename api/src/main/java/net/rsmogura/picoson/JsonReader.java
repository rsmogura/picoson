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

  protected JsonReader(net.rsmogura.picoson.gson.JsonReader gsonReader) {
    this.gsonReader = gsonReader;
  }

  public JsonReader(Reader in) {
    // Don't use this(), not to depend on inlining, we focus on performance and
    // this part of code is small
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

  /**
   * @return string or {@code null} if value is set to {@code null} in JSON
   */
  public String nextString() {
    // Note: this implementation differs from GSON, which does not
    //       return null.
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

  /**
   * @return {@link Boolean} value or {@code null} if value is set to {@code null} in JSON
   */
  public Boolean nextBoxedBoolean() {
    try {
      if (gsonReader.peek() == JsonToken.NULL) {
        gsonReader.nextNull();
        return null;
      }
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

  public float nextFloat() {
    // TODO Move support to Gson
    try {
      // TODO Better create nextFloat in gsonReader
      // Checking for overflow in case of double -> float is not so easy
      return (float) gsonReader.nextDouble();
    } catch (IOException e) {
      throw new JsonReadException(e);
    }
  }

  /**
   * @return {@link Float} value or {@code null} if value is set to {@code null} in JSON
   */
  public Float nextBoxedFloat() {
    try {
      if (gsonReader.peek() == JsonToken.NULL) {
        gsonReader.nextNull();
        return null;
      }
      return this.nextFloat();
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

  /**
   * @return {@link Double} value or {@code null} if value is set to {@code null} in JSON
   */
  public Double nextBoxedDouble() {
    try {
      if (gsonReader.peek() == JsonToken.NULL) {
        gsonReader.nextNull();
        return null;
      }
      return this.nextDouble();
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

  /**
   * @return {@link Long} value or {@code null} if value is set to {@code null} in JSON
   */
  public Long nextBoxedLong() {
    try {
      if (gsonReader.peek() == JsonToken.NULL) {
        gsonReader.nextNull();
        return null;
      }
      return gsonReader.nextLong();
    } catch (IOException e) {
      throw new JsonReadException(e);
    }
  }

  public byte nextByte() {
    try {
      int i = gsonReader.nextInt();
      byte b = (byte) i;
      if (i != b) {
        throw new NumberFormatException("The value " + i + " is not a byte @" + gsonReader.toString());
      }

      return b;
    } catch (IOException e) {
      throw new JsonReadException(e);
    }
  }

  /**
   * @return {@link Byte} value or {@code null} if value is set to {@code null} in JSON
   */
  public Byte nextBoxedByte() {
    try {
      if (gsonReader.peek() == JsonToken.NULL) {
        gsonReader.nextNull();
        return null;
      }
      return this.nextByte();
    } catch (IOException e) {
      throw new JsonReadException(e);
    }
  }

  public short nextShort() {
    try {
      int i = gsonReader.nextInt();
      short s = (short) i;
      if (i != s) {
        throw new NumberFormatException("The value " + i + " is not a short @" + gsonReader.toString());
      }

      return s;
    } catch (IOException e) {
      throw new JsonReadException(e);
    }
  }

  /**
   * @return {@link Short} value or {@code null} if value is set to {@code null} in JSON
   */
  public Short nextBoxedShort() {
    try {
      if (gsonReader.peek() == JsonToken.NULL) {
        gsonReader.nextNull();
        return null;
      }
      return this.nextShort();
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

  /**
   * @return {@link Integer} value or {@code null} if value is set to {@code null} in JSON
   */
  public Integer nextBoxedInt() {
    try {
      if (gsonReader.peek() == JsonToken.NULL) {
        gsonReader.nextNull();
        return null;
      }
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
