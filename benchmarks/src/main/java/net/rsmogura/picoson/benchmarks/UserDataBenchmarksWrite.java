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
import java.io.IOException;
import java.io.OutputStreamWriter;
import net.rsmogura.picoson.JsonWriter;
import net.rsmogura.picoson.samples.models.UserData;
import org.apache.commons.io.output.NullOutputStream;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.CompilerControl.Mode;
import org.openjdk.jmh.infra.Blackhole;

@CompilerControl(value = Mode.INLINE)
public class UserDataBenchmarksWrite extends ParsersComparingBenchmark {
  private static final UserData userData;
  private static final ObjectMapper objectMapper = new ObjectMapper();

  static {
    userData = new UserData();
    userData.setPasswordHash("SHA256:157874489nsvw");
    userData.setType(2);
    userData.setActive(true);
    userData.setUserName("userWrite");
  }

  @Benchmark
  @Override
  public void jackson(Blackhole blackhole) {
    try {
      objectMapper.writeValue(new OutputStreamWriter(new NullOutputStream()), userData);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Benchmark
  @Override
  public void picoson(Blackhole blackhole) {
    userData.jsonWrite(new JsonWriter(new OutputStreamWriter(new NullOutputStream())));
  }

  @Benchmark
  @Override
  public void gson(Blackhole blackhole) {
    new Gson().toJson(userData, UserData.class, new OutputStreamWriter(new NullOutputStream()));
  }

  @Override
  public void gsonParseOnly(Blackhole blackhole) {

  }
}
