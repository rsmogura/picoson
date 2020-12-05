# Info
Contains various comparisions to justify why this way not the other

# JsonObjectDescriptor getProperties returns array

```text
Array version JsonPropertyDescriptor[] of JsonObjectDescriptor.getProperties

Benchmark                         Mode  Cnt        Score        Error  Units
UserDataBenchmarksWrite.gson     thrpt    5   218382.152 ±   5580.687  ops/s
UserDataBenchmarksWrite.jackson  thrpt    5  3913127.362 ± 173774.121  ops/s
UserDataBenchmarksWrite.picoson  thrpt    5  6427794.525 ± 168717.757  ops/s

ArrayList<JsonObjectDescriptor> version of JsonObjectDescriptor.getProperties
Benchmark                         Mode  Cnt        Score        Error  Units
UserDataBenchmarksWrite.gson     thrpt    5   216734.875 ±   3560.100  ops/s
UserDataBenchmarksWrite.jackson  thrpt    5  3958068.924 ±  84360.951  ops/s
UserDataBenchmarksWrite.picoson  thrpt    5  6049388.910 ± 182620.426  ops/s
```