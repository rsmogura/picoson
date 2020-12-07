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

  protected /* synthetic */ void #jsonWrite(JSONWriter out) {
      out.beginObject();
      JsonPropertyDescriptor[] props = #jsonDesc.getProperties();
      int propsLen = props.length;
      // Iterate over all properties in descriptor
      for(int i = 0; i < propsLen; ++i) {
        this.#jsonWriteProp(props[i], out);
      }

      var1.endObject();
  }

  protected boolean #jsonWriteProp(JsonPropertyDescriptor pd, JsonWriter out) {
      int var3 = var1.getWritePropertyIndex();
      // 1 is the property index assigned during class transformation
      // it corresponds to passwordHash field, and it's used to speed
      // up searching for properties.
      //
      // The if-else-if tree (pending optimization)
      // is generated inside `PropertyAbstractGenerator.generate`.
      if (var3 == 1) {
        var2.name(var1.getJsonPropertyName()).value(this.passwordHash);
        return (boolean)1;
      } else if (var3 == 3) {
        var2.name(var1.getJsonPropertyName()).value(this.active);
        return (boolean)1;
      } else if (var3 == 0) {
        var2.name(var1.getJsonPropertyName()).value(this.userName);
        return (boolean)1;
      } else if (var3 == 2) {
        var2.name(var1.getJsonPropertyName()).value((long)this.type);
        return (boolean)1;
      } else {
        return (boolean)0;
      }
  }
}
```
#ABI
ABI stands for Application Binary Interface and it's the set of rules for
interactions between, typically native applications, libraries and functions;
in native world it's something like API.

However, as Picoson is statically compiled the term ABI is used instead of
API (probably incorrectly), to emphasize significance of (some) generated
methods and their signature.

The ABI classes, are internal API classes.

# Major classes & packages
* `Names` - this class contains names used in generated code, many names
    