package dhcoder.support.math;

import dhcoder.support.memory.Pool;

import java.util.Random;

/**
 * Like {@link CompassDirection} but with only 4 directions.
 */
public enum CardinalDirection {
    E,
    N,
    W,
    S;

    private static final Random RANDOM = new Random();
    private static final CardinalDirection[] CACHED = values();
    // Regions that correspond with each 45° section of the circle
    private static final CardinalDirection[] REGIONS = new CardinalDirection[CACHED.length * 2];
    private static final Angle[] ANGLES = new Angle[CACHED.length];

    private static final Pool<Angle> anglePool = Pool.of(Angle.class);

    static {
        REGIONS[0] = CardinalDirection.E;
        REGIONS[1] = CardinalDirection.N;
        REGIONS[2] = CardinalDirection.N;
        REGIONS[3] = CardinalDirection.W;
        REGIONS[4] = CardinalDirection.W;
        REGIONS[5] = CardinalDirection.S;
        REGIONS[6] = CardinalDirection.S;
        REGIONS[7] = CardinalDirection.E;

        float angleDeg = 0f;
        for (int i = 0; i < ANGLES.length; i++) {
            ANGLES[i] = Angle.fromDegrees(angleDeg);
            angleDeg += 90f; // 360 / 4 directions
        }
    }

    public static CardinalDirection getRandom() {
        return CACHED[RANDOM.nextInt(CACHED.length)];
    }

    public static CardinalDirection getForAngle(Angle angle) {
        int regionIndex = (int)(angle.getDegrees() / 45f);
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
        return ANGLES[this.ordinal()];
    }
}
