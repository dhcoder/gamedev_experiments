package dhcoder.support.shape;

/**
 * Base class for shapes. Subclasses should encapsulate the size and bounds of a shape but not its location - that way,
 * the same shape can be reused for collision tests in many different locations.
 */
public interface Shape {
    boolean containsPoint(float x, float y);
}