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

import com.google.common.collect.ImmutableSet;
import java.io.CharArrayWriter;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import net.rsmogura.picoson.Json;
import net.rsmogura.picoson.JsonReader;
import net.rsmogura.picoson.JsonSupport;
import net.rsmogura.picoson.JsonWriter;
import net.rsmogura.picoson.abi.JsonObjectDescriptor;
import net.rsmogura.picoson.abi.JsonPropertyDescriptor;
import net.rsmogura.picoson.abi.Names;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SampleDataTest {
    JsonSupport<SampleData> sampleDataJsonSupport = Json.jsonSupport(SampleData.class);

    /** Tests read, using provided JSON. */
    @Test
    public void testRead() throws Exception {
        InputStream userJson = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("net/rsmogura/picoson/tests/user-data-simple.json");
        JsonReader reader = new JsonReader(new InputStreamReader(userJson));
        SampleData read = sampleDataJsonSupport.read(reader);

        assertEquals("rado", read.getUserName());
        assertEquals("SHA256:123", read.getPasswordHash());
        assertEquals(true, read.isActive());
        assertEquals(826281, read.getType());
    }

    @Test
    public void testWrite() throws Exception {
        CharArrayWriter out = new CharArrayWriter();
        JsonWriter writer = new JsonWriter(out);
        SampleData sampleData = new SampleData();
        sampleData.setUserName("user2");
        sampleData.setType(-1);
        sampleDataJsonSupport.write(sampleData, writer);
        writer.close();
    }

    @Test
    public void testNestedClassRead() throws Exception {
        InputStream userJson = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("net/rsmogura/picoson/tests/user-data-simple.json");
        JsonReader reader = new JsonReader(new InputStreamReader(userJson));
        JsonSupport<SampleData.X> jsonSupport = Json.jsonSupport(SampleData.X.class);
        SampleData.X read = jsonSupport.read(reader);

        assertEquals("rado", read.getUserName());
        assertEquals("SHA256:123", read.getPasswordHash());
        assertEquals(true, read.isActive());
        assertEquals(826281, read.getType());
    }

    @Test
    public void testPropertyDescriptor() throws Exception {
        Method initDescriptor = SampleData.class.getDeclaredMethod(Names.DESCRIPTOR_INITIALIZER);
        initDescriptor.setAccessible(true);
        JsonObjectDescriptor objectDescriptor = (JsonObjectDescriptor) initDescriptor
            .invoke(SampleData.class);

        assertEquals(SampleData.class, objectDescriptor.getJsonClass());
        assertEquals(
            ImmutableSet.of("userName", "password-hash", "type", "active"),
            objectDescriptor.getJsonProperties().keySet(),
            "JSON Properties key differs");

        Set<Integer> usedReadIndices = new HashSet<>();
        Set<Integer> usedWriteIndices = new HashSet<>();

        for ( Entry<String, JsonPropertyDescriptor> e : objectDescriptor.getJsonProperties().entrySet()) {
            JsonPropertyDescriptor p = e.getValue();

            assertEquals(e.getKey(), p.getJsonPropertyName(), "Key to value mismatch");
            assertTrue(usedReadIndices.add(p.getReadPropertyIndex()), "Duplicated index");
            assertTrue(usedWriteIndices.add(p.getWritePropertyIndex()), "Duplicated index");
            assertEquals(SampleData.class, p.getReaderClass(),
                "For this simple object read should be done in this class");
            assertEquals(SampleData.class, p.getWriterClass(),
                "For this simple object write should be done in this class");
        }
    }
}
