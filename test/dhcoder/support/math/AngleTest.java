package dhcoder.support.math;

import org.junit.Test;

import static dhcoder.test.matchers.IsCloseTo.closeTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public final class AngleTest {

    @Test
    public void testSetAngleToDegrees() {
        Angle angle = Angle.fromDegrees(45f);
        assertThat(angle.getDegrees(), equalTo(45f));
        assertThat(angle.getRadians(), equalTo(45f * Angle.DEG_TO_RAD));
    }

    @Test
    public void testSetAngleToRadians() {
        Angle angle = Angle.fromRadians(Angle.PI / 3f);
        assertThat(angle.getRadians(), equalTo(Angle.PI / 3f));
        assertThat(angle.getDegrees(), equalTo(Angle.PI / 3f * Angle.RAD_TO_DEG));
    }

    @Test
    public void testSetAngleToOtherAngle() {
        Angle angle = Angle.fromDegrees(0f);
        Angle otherAngle = Angle.fromDegrees(45f);
        assertThat(angle.getDegrees(), equalTo(0f));
        angle.setFrom(otherAngle);
        assertThat(angle.getDegrees(), equalTo(45f));
    }

    @Test
    public void testSetAngleToDegreesThenRadians() {
        Angle angle = Angle.fromDegrees(180f);
        angle.setRadians(Angle.PI / 2f);
        assertThat(angle.getDegrees(), closeTo(90f));
    }

    @Test
    public void testSetAngleToRadiansThenDegrees() {
        Angle angle = Angle.fromRadians(Angle.PI);
        angle.setDegrees(90f);
        assertThat(angle.getRadians(), closeTo(Angle.PI / 2));
    }

    @Test
    public void outOfBoundsDegreesAreBounded() {
        Angle angle = Angle.fromDegrees(0f);

        angle.setDegrees(-300f);
        assertThat(angle.getDegrees(), equalTo(60f));

        angle.setDegrees(-3000f);
        assertThat(angle.getDegrees(), equalTo(240f));

        angle.setDegrees(400f);
        assertThat(angle.getDegrees(), equalTo(40f));

        angle.setDegrees(2000f);
        assertThat(angle.getDegrees(), equalTo(200f));
    }

    @Test
    public void outOfBoundsRadiansAreBounded() {
        Angle angle = Angle.fromRadians(0f);

        angle.setRadians(-Angle.PI / 2f);
        assertThat(angle.getRadians(), closeTo(3f * Angle.PI / 2f));

        angle.setRadians(-5f * Angle.PI);
        assertThat(angle.getRadians(), closeTo(Angle.PI));

        angle.setRadians(5f * Angle.PI / 2f);
        assertThat(angle.getRadians(), closeTo(Angle.PI / 2f));

        angle.setRadians(31f * Angle.PI);
        assertThat(angle.getRadians(), closeTo(Angle.PI));
    }
}