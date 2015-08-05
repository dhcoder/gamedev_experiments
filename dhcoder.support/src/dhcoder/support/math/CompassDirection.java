package dhcoder.support.math;

import dhcoder.support.memory.Pool;

import java.util.Random;

/**
 * Enumeration for compass directions and various utility methods.
 */
public enum CompassDirection {
    E,
    NE,
    N,
    NW,
    W,
    SW,
    S,
    SE;

    private static final Random RANDOM = new Random();
    private static final CompassDirection[] CACHED = values();
    // Regions that correspond with each 22.5° section of the circle
    private static final CompassDirection[] REGIONS = new CompassDirection[CACHED.length * 2];
    private static final Angle[] ANGLES = new Angle[CACHED.length];

    private static final Pool<Angle> anglePool = Pool.of(Angle.class);

    static {
        REGIONS[0] = CompassDirection.E;
        REGIONS[1] = CompassDirection.NE;
        REGIONS[2] = CompassDirection.NE;
        REGIONS[3] = CompassDirection.N;
        REGIONS[4] = CompassDirection.N;
        REGIONS[5] = CompassDirection.NW;
        REGIONS[6] = CompassDirection.NW;
        REGIONS[7] = CompassDirection.W;
        REGIONS[8] = CompassDirection.W;
        REGIONS[9] = CompassDirection.SW;
        REGIONS[10] = CompassDirection.SW;
        REGIONS[11] = CompassDirection.S;
        REGIONS[12] = CompassDirection.S;
        REGIONS[13] = CompassDirection.SE;
        REGIONS[14] = CompassDirection.SE;
        REGIONS[15] = CompassDirection.E;

        float angleDeg = 0f;
        for (int i = 0; i < ANGLES.length; i++) {
            ANGLES[i] = Angle.fromDegrees(angleDeg);
            angleDeg += 45f; // 360 / 8 directions
        }
    }

    public static CompassDirection getRandom() {
        return CACHED[RANDOM.nextInt(CACHED.length)];
    }

    public static CompassDirection getForAngle(Angle angle) {
        int regionIndex = (int)(angle.getDegrees() / 22.5f);
        return REGIONS[regionIndex];
    }

    public boolean isFacing(Angle angle, Angle epsilon) {
        Angle angleMin = anglePool.grabNew().set(angle).sub(epsilon);
        Angle angleMax = anglePool.grabNew().set(angle).add(epsilon);
        boolean isFacing =
            (getForAngle(angleMin) == this || getForAngle(angleMax) == this || getForAngle(angle) == this);

        anglePool.free(angleMin);
        anglePool.free(angleMax);
        return isFacing;
    }

    public boolean isFacing(Angle angle) {
        return getForAngle(angle) == this;
    }

    public Angle getAngle() {
        return ANGLES[ordinal()];
    }
}
