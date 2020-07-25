package net.rsmogura.picoson.samples.models;

import net.rsmogura.picoson.annotations.JsonClasses;
import net.rsmogura.picoson.annotations.JsonModule;
import net.rsmogura.picoson.samples.models.simple.RootObject;

@JsonModule
@JsonClasses({
        UserData.class
})
public class JsonModule1 {
}
