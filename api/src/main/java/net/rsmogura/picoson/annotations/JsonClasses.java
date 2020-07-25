package net.rsmogura.picoson.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes classes associated with given type, method, or field, which can't be discoverds
 * during compilation when following properties chain. <br/>
 * Typical use is with {@link JsonModule} to mark class required to be added to module context.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface JsonClasses {
    public Class[] value() default {};
}
