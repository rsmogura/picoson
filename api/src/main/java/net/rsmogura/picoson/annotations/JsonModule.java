package net.rsmogura.picoson.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks class as an Json module and entry point for serialization.
 * There's no need to implement anything inside this class.
 * <br />
 * As the intention of Pickoson is to enable fast start up times, and native compilation,
 * all JSON support logic is generated statically on compile time and no reflection is used.
 * Because of this applications should have an entry points for serialization and deserialization compiled
 * to bytecode.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface JsonModule {
}
