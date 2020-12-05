# Architecture overview

## What is Picoson

## Why Picoson

## Diagram
```
+----------------------------+
|            class           |
|                            |
|    +-------------------+   |
|    |                   |   |
|    |  class properties |   |
|    |                   |   |
|    +-------------------+   |
|                            |
|    +-------------------+   |                 +----------------------+
|    | Picoson generated |   |                 |                      |
|    | code              |<------------------->|       Picoson        |
|    |                   |   |                 |         ABI          |
|    +-------------------+   |                 |                      |
+----------------------------+                 +----------------------+
```

* Picoson API & ABI do not access non-generated code (no reflection)
* Generated code:
   * is responsible for accessing class properties,
   * can call methods from API & ABI,
   * can use methods & types from API & ABI for internal purposes
     (i.e. object descriptors are built using `JsonObjectDescriptorBuilder`)

#Generated class API / ABI overview
```java
@Json
public class Data {
  @JsonProperty("field1")
  private String field1;

  /** Contains object descriptor describing all properties in this class
   * including super class chain. Initialized during class initialization. */
  static /* synthetic */ JsonObjectDescriptor #jsonDesc;

  protected void #jsonWrite(JSONWriter out) {
  }

  protected boolean #jsonWriteProp(JsonPropertyDescriptor pd, JsonWriter out) {
  }
}
```
#ABI
ABI stands for Application Binary Interface and it's the set of rules for
interactions between, typically native, applications, libraries and functions;
in native world it's something like API.

However, as Picoson is statically compiled the term ABI is used instead of
API (probably increctly), to emphasize significance of (some) generated
methods and their signature.

The ABI classes, are internal API classes.

# Major classes & packages
* `Names` - this class containes names used in generated code, many names
    