package dhcoder.support.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An element annotated with {@code NotNull} should never be {@code null}, or else an exception
 * should be thrown. Besides expressing intent, tools can be set up to check for this annotation
 * and provide static analysis to enforce it.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.PARAMETER})
public @interface NotNull {
}
