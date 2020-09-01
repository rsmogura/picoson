# TODO

## Functions
* serialization,
* inheritance,
* support for rest of basic types and boxed versions,
* serialization / deserialization of collections (lists, sets, collections),
* serialization / deserialization of arrays,
* serialization / deserialization complex objects.

## Implementation
* POC for using class file transformers with annotation processor to
  transform code (can be more realistic solution) - marking generated
  methods / fields as synthetic as one generated from APT can't be
  marked as so
* Clean code
  * use `PicosonProcesssorContext` to pass state between various generators
  * remove `BuildContext`,
  * try to make generators stateless, 

## Documentation
* contract for reader entry method
* contract for property read method