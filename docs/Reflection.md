# Reflection (property descriptors)

## Introduction

The reflection is **internal** functionality of Picoson used to capture
and store required information about class and JSON mode, in order to
* increase performance,
* allow more complicated models.

## Design
The Picoson API exposes ABI package containing `JsonObjectDescriptor` and `JsonPropertyDescriptor`s.
These objects hold the structure of JSON model and corresponding data (like in Java reflection).

`JsonPropertyDescriptor` contains (mainly):
* names of property (JSON and internal / Java),
* read and write indexes - the unique numbers assigned to property to identify read / write
  operation,
* classes responsible for read write operations.
 
The structure is captured during transformation of JSON class, inlined into code, and result
descriptor is stored in a static field (`$jsonDesc`) during class static initialization for
further usage.

To build `JsonPropertyDescriptor`, `JsonObjectDescriptorBuilder` is used.
`JsonObjectDescriptorBuilder` builds final descriptor form passed information
and can change original model. 

## Usage of descriptors
During deserialization loop, the property descriptor is selected from property descriptors map
(`$jsonDesc.getJsonProperties()`) using the name of currently read property from JSON.

The read method determines action (what property to set and how) using read index passed from
descriptor, and inlined indices assigned during code generation.

## Read & write indices
Those indices can be found in `JsonPropertyDescriptor` and are used as 
identifiers of property to serialize or deserialize in performant fashion
(using string equals is much slower than checking integers).

The read / write index value is valid only in class marked as reader / writer respectively. So,
in case of inheritance method can determine if the call could be processed or should pass control
to super method.

Below is a simplified code for reading single property, presenting usage of
read index.
```
int idx = pd.getReadPropertyIndex();
if (idx == 0) {
  // read and set property A
} else if (idx == 1) {
 // read and set property B
} 
``` 
## Results
Using integer indices helped reduce parsing times significantly
(see Benchmarks)

## Notes
Read / write indices were designed on concept of vtables, however due
to possibility of changing the super class and not re-compiling
the subclass, the index can change depending on level of class in hierarchy
