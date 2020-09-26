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

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.reflect.Method;
import net.rsmogura.picoson.abi.Names;
import net.rsmogura.picoson.tests.SampleData.NotSupposedToBeJsoned;
import org.junit.jupiter.api.Test;

public class OtherTransformationTests {

  /**
   * Checks if class not intended for having JSON methods, will not have those.
   * In other words checks if non JSON classes are not affected.
   */
  @Test
  public void testNotMarkedClassDontGetChanged() {
    final Method[] methods = NotSupposedToBeJsoned.class.getMethods();
    for (Method m : methods) {
      assertFalse(m.getName().equals(Names.DESCRIPTOR_INITIALIZER));
      assertFalse(m.getName().equals(Names.INSTANCE_DESERIALIZE_METHOD_NAME));
      assertFalse(m.getName().contains("#json"));
      assertFalse(m.getName().contains("$json"));
    }
  }
}
