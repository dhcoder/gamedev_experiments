package dhcoder.support.math;

import dhcoder.support.memory.Poolable;
import dhcoder.support.opt.OptFloat;

import static dhcoder.support.text.StringUtils.format;

/**
 * Simple class that represents a 0 -> 360° angle. You can set and get this angle's value in either degreesOpt or
 * radiansOpt.
 */
public final class Angle implements Poolable {

    /**
     * A float version of java.lang.Math.PI
     */
    public static final float PI = (float)Math.PI;

    /**
     * Convenience constant for π/2
     */
    public static final float HALF_PI = PI / 2f;

    /**
     * Convenience constant for π/4
     */
    public static final float QUARTER_PI = PI / 4f;

    /**
     * Convenience constant for 2π
     */
    public static final float TWO_PI = PI * 2f;

    /**
     * Multiplying this to a value in degrees converts it to radians.
     */
    public static final float RAD_TO_DEG = 180f / PI;

    /**
     * Multiplying this to a value in radians converts it to degrees.
     */
    public static final float DEG_TO_RAD = PI / 180f;

    private static final float FULL_REVOLUTION_RAD = 2 * PI;
    private static final float FULL_REVOLUTION_DEG = 360f;

    public static Angle fromDegrees(float degrees) {
        Angle angle = new Angle();
        angle.setDegrees(degrees);
        return angle;
    }

    public static Angle fromRadians(float radians) {
        Angle angle = new Angle();
        angle.setRadians(radians);
        return angle;
    }

    public static Angle from(Angle otherAngle) {
        Angle angle = new Angle();
        angle.set(otherAngle);
        return angle;
    }

    // One or both of these values are guaranteed to be set at any time. When one value is set, the other invalidated,
    // but when a request is made to get an unset value, it will lazily be calculated at that time.
    private final OptFloat degreesOpt = OptFloat.of(0f);
    private final OptFloat radiansOpt = OptFloat.of(0f);

    /**
     * Use {@link #fromDegrees(float)} or {@link #fromRadians(float)} instead.
     */
    private Angle() {}

    public float getDegrees() {
        if (!degreesOpt.hasValue()) {
            setDegrees(radiansOpt.getValue() * RAD_TO_DEG);
        }
        return degreesOpt.getValue();
    }

    public Angle setDegrees(float degrees) {
        float boundedDegrees = degrees % FULL_REVOLUTION_DEG;
        while (boundedDegrees < 0f) {
            boundedDegrees += FULL_REVOLUTION_DEG;
        }

        degreesOpt.set(boundedDegrees);
        radiansOpt.clear();

        return this;
    }

    public float getRadians() {
        if (!radiansOpt.hasValue()) {
            setRadians(degreesOpt.getValue() * DEG_TO_RAD);
        }
        return radiansOpt.getValue();
    }

    public Angle setRadians(float radians) {
        float boundedRadians = radians % FULL_REVOLUTION_RAD;
        while (boundedRadians < 0f) {
            boundedRadians += FULL_REVOLUTION_RAD;
        }

        radiansOpt.set(boundedRadians);
        degreesOpt.clear();

        return this;
    }

    public Angle set(Angle rhs) {
        degreesOpt.setFrom(rhs.degreesOpt);
        radiansOpt.setFrom(rhs.radiansOpt);

        return this;
    }

    public Angle addDegrees(float degrees) {
        setDegrees(getDegrees() + degrees);
        return this;
    }

    public Angle addRadians(float radians) {
        setRadians(getRadians() + radians);
        return this;
    }

    public Angle add(Angle rhs) {
        addDegrees(rhs.getDegrees());
        return this;
    }

    public Angle subDegrees(float degrees) {
        setDegrees(getDegrees() - degrees);
        return this;
    }

    public Angle subRadians(float radians) {
        setRadians(getRadians() - radians);
        return this;
    }

    public Angle sub(Angle rhs) {
        subDegrees(rhs.getDegrees());
        return this;
    }

    @Override
    public String toString() {
        return format("{0}°", getDegrees());
    }

    @Override
    public void reset() {
        degreesOpt.set(0f);
        radiansOpt.set(0f);
    }
}
