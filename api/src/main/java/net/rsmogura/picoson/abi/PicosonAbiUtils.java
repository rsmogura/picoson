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

package net.rsmogura.picoson.abi;

/**
 * The utils used in Picoson core. Calls to this methods are compiled into classes,
 * and to maintain backward compatibility, it's signature, nor behaviour should not change,
 * as project would have to be re-compiled.
 */
public class PicosonAbiUtils {

  /**
   * Hashing function for String. The hashes can be compiled into code, so this method should
   * not change.
   */
  public static int hashString(String stringToHash) {
    return 0;
  }

  public static boolean isEmpty(String string) {
    return string == null || string.isEmpty();
  }
}
