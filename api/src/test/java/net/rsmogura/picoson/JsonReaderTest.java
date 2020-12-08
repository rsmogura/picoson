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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import net.rsmogura.picoson.gson.JsonReader;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class JsonReaderTest {

  public JsonReader gsonReader = Mockito.mock(JsonReader.class);

  @Test
  public void testNextByte() throws Exception {
    net.rsmogura.picoson.JsonReader jr = new net.rsmogura.picoson.JsonReader(gsonReader);

    doReturn(-1).when(gsonReader).nextInt();
    assertEquals(-1, jr.nextByte());

    doReturn(256).when(gsonReader).nextInt();
    NumberFormatException nfe = assertThrows(NumberFormatException.class, () -> jr.nextByte());
    assertTrue(nfe.getMessage().contains("256"), "Expected message contains erroneous value");
  }
}