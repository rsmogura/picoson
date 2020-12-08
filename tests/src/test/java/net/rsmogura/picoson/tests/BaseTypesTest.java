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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import net.rsmogura.picoson.JsonReader;
import org.junit.jupiter.api.Test;

public class BaseTypesTest {

  /**
   * Tests basic types & boxed versions.
   *
   * For every type there is reading of primitive version.
   * Reading boxed version.
   * Reading of null value from boxed version (default value in class is non - null)
   * Not reading one value (value not specified in JSON) -> should stay same
   *    as initialized during construct
   * @throws Exception
   */
  @Test
  public void testBaseTypesRead() throws Exception {
    InputStream userJson = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("net/rsmogura/picoson/tests/base-types.json");
    JsonReader reader = new JsonReader(new InputStreamReader(userJson));
    BaseTypes read = BaseTypes.jsonRead(reader);

    assertEquals(1, read.getI1());
    assertEquals(2, read.getI2());
    assertNull(read.getI3());
    assertEquals(-1, read.getI4());

    assertEquals(9223372036854775807L, read.getL1());
    assertEquals(-9223372036854775805L, read.getL2());
    assertNull(read.getL3());
    assertEquals(-9223372036854775802L, read.getL4());

    assertEquals(125, read.getByte1());
    assertEquals((byte) -2, read.getByte2());
    assertNull(read.getByte3());
    assertEquals((byte) -3, read.getByte4());

    assertEquals(true, read.isB1());
    assertEquals(true, read.getB2());
    assertNull(read.getB3());
    assertEquals(true, read.getB4());
  }
}
