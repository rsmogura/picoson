package net.rsmogura.picoson.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Creates JSON deserialize constructor. This constructor can
 * <b>only</b> be used to deserialize the known type (no polymorphism
 * is supported).
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface JsonConstructor {
}
