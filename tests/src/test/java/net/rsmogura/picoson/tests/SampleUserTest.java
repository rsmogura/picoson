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

import net.rsmogura.picoson.JsonReader;
import net.rsmogura.picoson.samples.models.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.CharArrayReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SampleUserTest {

    /** Tests read, using provided JSON. */
    @Test
    public void testRead() throws Exception {
        InputStream userJson = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("net/rsmogura/picoson/tests/user-data-simple.json");
        JsonReader reader = new JsonReader(new InputStreamReader(userJson));
        UserData read = UserData.jsonRead(reader);

        assertEquals("rado", read.getUserName());
        assertEquals("SHA256:123", read.getPasswordHash());
        assertEquals(true, read.isActive());
        assertEquals(826281, read.getType());
    }
}
