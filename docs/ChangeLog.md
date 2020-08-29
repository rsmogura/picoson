# Large objects benchmarks
Added tests for checking performance of serializing large objects
(100 fields). As expected those were very slow
due to POC algorithm used for searching the filed
to deserialize (if-else tree with String equals).

```
LargeObjectBenchmarks.gsonSimpleUser     thrpt    5    14002.745 ±   1113.542  ops/s
LargeObjectBenchmarks.gsonSkip           thrpt    5   114738.656 ±   8593.808  ops/s
LargeObjectBenchmarks.jacksonSimpleUser  thrpt    5    44222.390 ±   3695.804  ops/s
LargeObjectBenchmarks.picosonSimpleUser  thrpt    5    25734.908 ±   1046.269  ops/s
UserDataBenchmark.gsonSimpleUser         thrpt    5   172994.062 ±  24945.179  ops/s
UserDataBenchmark.gsonSkip               thrpt    5  1372686.031 ±  45589.697  ops/s
UserDataBenchmark.jacksonSimpleUser      thrpt    5   177038.535 ±   9073.661  ops/s
UserDataBenchmark.picosonSimpleUser      thrpt    5  1156275.374 ± 114709.389  ops/s
```