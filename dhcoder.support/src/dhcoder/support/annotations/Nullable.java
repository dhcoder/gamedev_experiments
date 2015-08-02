package dhcoder.support.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An element annotated with {@code Nullable} can be {@code null}. Any parameter or return value
 * marked with {@code Nullable} should be commented on what that means, although in general
 * {@code NotNull} should be preferred over {@code Nullable}. Besides expressing intent, tools can
 * be set up to check for this annotation and provide static analysis to warn about possible null
 * values being accessed.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.PARAMETER})
public @interface Nullable {
}
