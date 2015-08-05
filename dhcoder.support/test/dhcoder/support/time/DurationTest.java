package dhcoder.support.time;

import org.junit.Test;

import static dhcoder.test.matchers.IsCloseTo.closeTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public final class DurationTest {

    @Test
    public void testFromSeconds() {
        Duration duration = Duration.fromSeconds(90f);

        assertThat(duration.getMinutes(), equalTo(1.5f));
        assertThat(duration.getSeconds(), equalTo(90f));
        assertThat(duration.getMilliseconds(), equalTo(90000f));
    }

    @Test
    public void testFromMinutes() {
        Duration duration = Duration.fromMinutes(1.5f);

        assertThat(duration.getMinutes(), equalTo(1.5f));
        assertThat(duration.getSeconds(), equalTo(90f));
        assertThat(duration.getMilliseconds(), equalTo(90000f));
    }

    @Test
    public void testFromMilliseconds() {
        Duration duration = Duration.fromMilliseconds(90000f);

        assertThat(duration.getMinutes(), equalTo(1.5f));
        assertThat(duration.getSeconds(), equalTo(90f));
        assertThat(duration.getMilliseconds(), equalTo(90000f));
    }

    @Test
    public void testEmptyDuration() {
        Duration duration = Duration.zero();

        assertThat(duration.getSeconds(), equalTo(0f));
        assertThat(duration.isZero(), equalTo(true));
    }

    @Test
    public void testSetMethods() {
        Duration duration = Duration.zero();

        duration.setSeconds(3f);
        assertThat(duration.getSeconds(), equalTo(3f));

        duration.setMinutes(4f);
        assertThat(duration.getMinutes(), equalTo(4f));

        duration.setMilliseconds(5f);
        assertThat(duration.getMilliseconds(), equalTo(5f));

        Duration otherDuration = Duration.fromSeconds(6f);
        duration.set(otherDuration);
        assertThat(duration.getSeconds(), equalTo(6f));

        duration.setZero();
        assertThat(duration.getSeconds(), equalTo(0f));
    }

    @Test
    public void testAddMethods() {
        Duration duration = Duration.zero();

        duration.addSeconds(1f);
        assertThat(duration.getSeconds(), equalTo(1f));

        duration.addMilliseconds(500f);
        assertThat(duration.getSeconds(), equalTo(1.5f));

        duration.addMinutes(2f);
        assertThat(duration.getSeconds(), closeTo(121.5f));

        Duration otherDuration = Duration.fromMilliseconds(1500f);
        duration.add(otherDuration);
        assertThat(duration.getSeconds(), closeTo(123f));
    }

    @Test
    public void settingNegativeDurationBecomesZero() {
        assertThat(Duration.fromSeconds(-5).getSeconds(), equalTo(0f));
    }

}