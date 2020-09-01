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

package net.rsmogura.picoson.benchmarks;

import org.openjdk.jmh.infra.Blackhole;

/**
 * Interface for benchmarks to compare execution speed with other parsers. We don't compare
 * to compete but to have good relative point to determine if efforts are worth.
 */
public abstract class ParsersComparingBenchmark {

  /**
   * Make tests using Jackson.
   */
  public abstract void jackson(Blackhole blackhole);

  /**
   * Make tests using Picoson (us).
   */
  public abstract void picoson(Blackhole blackhole);

  /**
   * Make tests using Gson.
   */
  public abstract void gson(Blackhole blackhole);

  /**
   * Just parse JSON stream (probably implementation will call skip).
   */
  public abstract void gsonParseOnly(Blackhole blackhole);
}
