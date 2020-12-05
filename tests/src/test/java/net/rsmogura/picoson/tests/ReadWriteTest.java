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

package net.rsmogura.picoson.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.util.HashMap;
import java.util.Random;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import net.rsmogura.picoson.JsonReader;
import net.rsmogura.picoson.JsonWriter;
import net.rsmogura.picoson.annotations.Json;
import net.rsmogura.picoson.annotations.JsonProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Read-write tests This tests reads JSON writes it and reads again to check
 * if values are serialized correctly.
 */
public class ReadWriteTest {
  private Random random = new Random(System.currentTimeMillis());

  private HashMap<String, Integer> nullChecksMap = new HashMap<>();

  /**
   * This test creates input class with random data, clones it
   * serializes to JSON and deserializes from JSON to check if data
   * are same.
   * <br />
   * This test uses pseudo random values, and invokes number of passes to be
   * absolutely sure.
   * <br />
   * Number of passes is required to check null values serialization.
   */
  @Test
  public void testReadWrite() throws Exception {
    for (int i=0; i < 1000; i++) {
      testReadWriteCheck();
    }
  }

  protected void testReadWriteCheck() {
    final ReadWriteTestModel srcModel = prepareSampleModel();
    final ReadWriteTestModel toSerializeData = srcModel.clone();
    final ReadWriteTestModel readData;

    try(CharArrayWriter writeBuff = new CharArrayWriter();
        JsonWriter writer = new JsonWriter(writeBuff)) {
      toSerializeData.jsonWrite(writer);
      writer.flush();

      try (CharArrayReader readBuff = new CharArrayReader(writeBuff.toCharArray())) {
        readData = ReadWriteTestModel.jsonRead(new JsonReader(readBuff));
      }
    }

    // Sanity checks (just to test tests)
    assertNotSame(srcModel, toSerializeData);
    assertNotSame(toSerializeData, readData);
    assertNotSame(srcModel, readData);

    // Check if clone and data to serialize are same
    assertEquals(srcModel, toSerializeData);

    // Check if source class is same as readData
    assertEquals(srcModel, readData);

    // Sanity check
    readData._intField1--;
    readData.intField2++;
    assertNotSame(srcModel, readData);
  }

  protected ReadWriteTestModel prepareSampleModel() {
    return ReadWriteTestModel.builder()
        ._intField1(random.nextInt())
        .intField2(random.nextInt())
        .booleanField(random.nextFloat() < 0.5)
        .stringField(orMaybeNull("string", Integer.toString(random.nextInt())))
        .longField(Long.MAX_VALUE + random.nextInt())
        .build();
  }

  /**
   * Used to replace objet value with null, to check null serialization & deserialization.
   */
  protected <T> T orMaybeNull(String field, T t) {
    Integer i = this.nullChecksMap.getOrDefault(field, 0);
    this.nullChecksMap.put(field, ++i);
    if (i % 2 == 0) {
      return null;
    } else {
      return t;
    }
  }

  @Json
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  private static class ReadWriteTestModel implements Cloneable {
    @JsonProperty("intField1")
    private int _intField1 = -1;

    private int intField2 = -1;

    private String stringField = "a";

    private boolean booleanField;

    private long longField;

    @SneakyThrows
    @Override
    protected ReadWriteTestModel clone() {
      return (ReadWriteTestModel) super.clone();
    }
  }
}
