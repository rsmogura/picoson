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

package net.rsmogura.picoson.benchmarks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import net.rsmogura.picoson.JsonReader;
import net.rsmogura.picoson.benchmarks.samples.LargeObject;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.CompilerControl.Mode;
import org.openjdk.jmh.infra.Blackhole;

@CompilerControl(value = Mode.INLINE)
public class LargeObjectBenchmarks {

  private final static String inputJson;

  static {
    InputStream jsonStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(
        "net/rsmogura/picoson/benchmarks/largeObject.json");
    try {
      inputJson = IOUtils.toString(jsonStream, "UTF-8");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private final static char[] inputJsonChars = inputJson.toCharArray();

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Benchmark
  public void jacksonSimpleUser(Blackhole blackhole) {
    try (CharArrayReader chars = new CharArrayReader(inputJsonChars)) {
      LargeObject userData = objectMapper.readValue(chars, LargeObject.class);
      blackhole.consume(userData);
    } catch (IOException ioe) {

    }
  }

  @Benchmark
  public void picosonSimpleUser(Blackhole blackhole) {
    try (CharArrayReader chars = new CharArrayReader(inputJsonChars)) {
      JsonReader reader = new JsonReader(chars);
      LargeObject userData = LargeObject.jsonRead(reader);
      blackhole.consume(userData);
    }
  }

  @Benchmark
  public void gsonSimpleUser(Blackhole blackhole) {
    try (CharArrayReader chars = new CharArrayReader(inputJsonChars)) {
      LargeObject userData = new Gson().fromJson(chars, LargeObject.class);
      blackhole.consume(userData);
    }
  }

  @Benchmark
  public void gsonSkip(Blackhole blackhole) {
    try (CharArrayReader chars = new CharArrayReader(inputJsonChars)) {
      JsonReader reader = new JsonReader(chars);
      reader.skipValue();
    }
  }
}
