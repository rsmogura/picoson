# Generics

## Generics problem
Let's assume we have following classes
```$java
class DataHolder<T> {
    private String id;
    private T data;
}

class DocumentHolder extends DataHolder<String> {
}
```

As Picoson is a static generator, the serialization and deserialization
inlined into `DocumentHolder` & `DataHolder` should be able to distinguish
how to deserialize `data` field, depending on class being read (so to call
proper read method, i.e. String, Integer, or maybe complex object).
