# Picoson
Picoson is extremely fast (10x faster!!) static-compiled Json reader and writer for Java.

It's intended to:
* Support native compilation (frameworks like Quarkus, Micronaut, or no framework).
* Have small memory and code foot print.
* Be extremely fast (and it is 10x faster - see [Benchmarks](#Benchmarks).

# Benchmarks
This is sample benchmark for POC (you can find it `benchamrks` module).

Picoson is 10x faster!!!

```text
Benchmark                                   Mode  Cnt        Score   Error  Units
SampleUserAlgoBenchmark.gsonSimpleUser     thrpt    2   212964.961          ops/s
SampleUserAlgoBenchmark.jacksonSimpleUser  thrpt    2   287052.224          ops/s
SampleUserAlgoBenchmark.picosonSimpleUser  thrpt    2  2056586.454          ops/s

```
  