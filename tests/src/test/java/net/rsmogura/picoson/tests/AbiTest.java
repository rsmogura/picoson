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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import net.rsmogura.picoson.JsonReader;
import net.rsmogura.picoson.abi.JsonPropertyDescriptor;
import net.rsmogura.picoson.samples.models.UserData;
import org.junit.jupiter.api.Test;

/**
 * Checks if generated methods follows signature.
 *
 * All names should stay hardcoded (not referenced from ABI) and should never change.
 */
public class AbiTest {

  /**
   * Checks if property read methods is generated and has expected signature.
   */
  @Test
  public void testPropertyRead() throws Exception {
    Method propRead = UserData.class.getDeclaredMethod("#jsonReadProp",
        JsonPropertyDescriptor.class, JsonReader.class);
    // If method not found exception is thrown
    assertTrue((propRead.getModifiers() & Modifier.ABSTRACT) == 0);
    assertTrue((propRead.getModifiers() & Modifier.STATIC) == 0);
  }
}
