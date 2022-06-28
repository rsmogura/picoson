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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import net.rsmogura.picoson.JsonReader;
import net.rsmogura.picoson.JsonSupport;
import net.rsmogura.picoson.JsonWriter;
import net.rsmogura.picoson.annotations.Json;
import net.rsmogura.picoson.annotations.JsonProperty;
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
      JsonSupport<ReadWriteTestModel> jsonSupport = net.rsmogura.picoson.Json.jsonSupport(ReadWriteTestModel.class);
      jsonSupport.write(toSerializeData, writer);
      writer.flush();

      final char[] chars = writeBuff.toCharArray();
      final String s = new String(chars);

      // Few naive tests if JSON is OK
      assertTrue(s.matches(".*\"intField\":" + srcModel.intField + ".*"));
      assertTrue(s.matches(".*\"booleanField\":" + srcModel.booleanField + ".*"));

      try (CharArrayReader readBuff = new CharArrayReader(chars)) {
        readData = jsonSupport.read(new JsonReader(readBuff));
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

    // Sanity check (what if equals got broken?)
    readData._intField1--;
    readData.intField2++;
    assertNotSame(srcModel, readData);
  }

  protected ReadWriteTestModel prepareSampleModel() {
    return ReadWriteTestModel.builder()
        ._intField1(random.nextInt())
        .intField2(random.nextInt())
        .booleanField(random.nextFloat() < 0.5)
        .booleanBField(orMaybeNull("1", random.nextFloat() < 0.5))
        .stringField(orMaybeNull("1", Integer.toString(random.nextInt())))
        .byteField((byte) random.nextInt())
        .byteBField(orMaybeNull("2", (byte) random.nextInt()))
        .shortField((short) random.nextInt())
        .shortBField(orMaybeNull("3", (short) random.nextInt()))
        .intField(random.nextInt())
        .intBField(orMaybeNull("4", random.nextInt()))
        .longField(Long.MAX_VALUE + random.nextInt())
        .longBField(orMaybeNull("5", random.nextLong()))
        .floatField(random.nextFloat())
        .floatBField(orMaybeNull("6", random.nextFloat()))
        .doubleField(random.nextDouble())
        .doubleBField(orMaybeNull("6", random.nextDouble()))
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
    private Boolean booleanBField;

    private byte byteField;
    private Byte byteBField;

    private short shortField;
    private Short shortBField;

    private int intField;
    private Integer intBField;

    private long longField;
    private Long longBField;

    private float floatField;
    private Float floatBField;

    private double doubleField;
    private Double doubleBField;

    @SneakyThrows
    @Override
    protected ReadWriteTestModel clone() {
      return (ReadWriteTestModel) super.clone();
    }
  }
}
