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

import lombok.Data;
import net.rsmogura.picoson.annotations.Json;

@Json
@Data
public class BaseTypes {
  private int i1;
  private Integer i2;
  private Integer i3 = -1;
  private Integer i4 = -1;

  private long l1;
  private Long l2;
  private Long l3 = -1L;
  private Long l4 = -9223372036854775802L;

  private byte byte1;
  private Byte byte2;
  private Byte byte3 = -1;
  private Byte byte4 = -3;

  private boolean b1;
  private Boolean b2;
  private Boolean b3 = true;
  private Boolean b4 = true;
}
