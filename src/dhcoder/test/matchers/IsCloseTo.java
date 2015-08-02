/*  Copyright (c) 2000-2006 hamcrest.org
 */
package dhcoder.test.matchers;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Like {@link org.hamcrest.number.IsCloseTo} but works on floats instead of Doubles
 */
public final class IsCloseTo extends TypeSafeMatcher<Float> {
    @Factory
    public static Matcher<Float> closeTo(final float operand, final float error) {
        return new IsCloseTo(operand, error);
    }

    @Factory
    /**
     * Return a matcher that uses a default error value that should cover for imprecision in float math.
     */
    public static Matcher<Float> closeTo(final float operand) {
        return closeTo(operand, 0.01f);
    }

    private final float delta;
    private final float value;

    public IsCloseTo(final float value, final float error) {
        this.delta = error;
        this.value = value;
    }

    @Override
    public boolean matchesSafely(final Float item) {
        return actualDelta(item) <= 0.0f;
    }

    @Override
    public void describeMismatchSafely(final Float item, final Description mismatchDescription) {
        mismatchDescription.appendValue(item).appendText(" differed by ").appendValue(actualDelta(item));
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("a numeric value within ").appendValue(delta).appendText(" of ").appendValue(value);
    }

    private double actualDelta(final Float item) {
        return (Math.abs((item - value)) - delta);
    }

}
