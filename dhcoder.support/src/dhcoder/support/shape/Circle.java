package dhcoder.support.shape;

import static dhcoder.support.text.StringUtils.format;

/**
 * Class that represents a circle
 */
public final class Circle implements Shape {

    private float radius;

    private Circle() {
        this(0f);
    }

    public Circle(float radius) {
        setRadius(radius);
    }

    @Override
    public boolean containsPoint(float x, float y) {
        return (x * x + y * y) <= radius * radius;
    }

    @Override
    public String toString() {
        return format("r={0}", radius);
    }

    public float getRadius() {
        return radius;
    }

    public Circle setRadius(float radius) {
        if (radius < 0f) {
            throw new IllegalArgumentException(format("Can't create circle with < 0 radius {0}", radius));
        }

        this.radius = radius;
        return this;
    }
}