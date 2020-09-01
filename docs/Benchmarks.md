# Benchmarks
## Disclaimer
Benchmarks are eecuted in different conditions, sometimes
with overclocked CPU sometimes not, or even can be on different systems

The absolute values can varry, however ratio to particular tests should be taken
on account

## After implementing Property Descriptors
The major change introduced here was about using integer
indices to determine property instead of String equals.

The property descriptor is selected from static map
for every JSON property read.

```
LargeObjectBenchmarks.gson           thrpt    5    12995.967 ±    722.522  ops/s
LargeObjectBenchmarks.gsonParseOnly  thrpt    5   107987.533 ±   4315.126  ops/s
LargeObjectBenchmarks.jackson        thrpt    5    42447.963 ±    619.809  ops/s
LargeObjectBenchmarks.picoson        thrpt    5    52651.851 ±   1993.111  ops/s
UserDataBenchmark.gson               thrpt    5   152306.954 ±   3417.681  ops/s
UserDataBenchmark.gsonParseOnly      thrpt    5  1166907.637 ± 127259.538  ops/s
UserDataBenchmark.jackson            thrpt    5   164282.927 ±   9121.129  ops/s
UserDataBenchmark.picoson            thrpt    5   954122.930 ± 217686.472  ops/s
```

### Benchmark when getter to read index was called for every property check
**Note: just for information purposes**
Iterating result, as storing index in variable increased speed significantly,
looks like getter **was not inlined** (maybe because every call was in separate block
and compiler could not extract value). It would be interesting to follow such a code with compiler:
```
if (pd.getReadePropertyIndex() == 0) {
} else if (pd.getReadPropertyIndex() == 1) {
}
```
**Benchmarks with ineffective code**
```
LargeObjectBenchmarks.gsonSimpleUser     thrpt    5    13342.286 ±   268.460  ops/s
LargeObjectBenchmarks.gsonSkip           thrpt    5    97327.351 ±  3758.821  ops/s
LargeObjectBenchmarks.jacksonSimpleUser  thrpt    5    45789.469 ±  3621.049  ops/s
LargeObjectBenchmarks.picoson            thrpt    5    32536.999 ±   481.963  ops/s
UserDataBenchmark.gsonSimpleUser         thrpt    5   174435.775 ±  3388.944  ops/s
UserDataBenchmark.gsonSkip               thrpt    5  1327756.808 ± 20432.440  ops/s
UserDataBenchmark.jacksonSimpleUser      thrpt    5   184651.059 ±  5936.024  ops/s
UserDataBenchmark.picoson                thrpt    5  1128360.230 ± 18690.566  ops/s
```

## Added benchmarks for large objects (dozens of properties) 
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