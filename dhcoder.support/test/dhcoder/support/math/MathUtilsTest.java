package dhcoder.support.math;

import org.junit.Test;

import static dhcoder.support.math.MathUtils.clamp;
import static dhcoder.support.math.MathUtils.isPowerOfTwo;
import static dhcoder.support.math.MathUtils.log2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public final class MathUtilsTest {

    @Test
    public void integerClampWorks() {
        assertThat(clamp(-2, 1, 5), equalTo(1));
        assertThat(clamp(1, 1, 5), equalTo(1));
        assertThat(clamp(5, 1, 5), equalTo(5));
        assertThat(clamp(3, 1, 5), equalTo(3));
        assertThat(clamp(8, 1, 5), equalTo(5));
    }

    @Test
    public void floatClampWorks() {
        assertThat(clamp(-2.5f, 1.3f, 5.8f), equalTo(1.3f));
        assertThat(clamp(1.3f, 1.3f, 5.8f), equalTo(1.3f));
        assertThat(clamp(3.2f, 1.3f, 5.8f), equalTo(3.2f));
        assertThat(clamp(5.8f, 1.3f, 5.8f), equalTo(5.8f));
        assertThat(clamp(9.1f, 1.3f, 5.8f), equalTo(5.8f));
    }

    @Test
    public void testLog2() {
        assertThat(log2(1), equalTo(0));
        assertThat(log2(2), equalTo(1));
        assertThat(log2(3), equalTo(2));
        assertThat(log2(4), equalTo(2));
        assertThat(log2(5), equalTo(3));
        assertThat(log2(6), equalTo(3));
        assertThat(log2(7), equalTo(3));
        assertThat(log2(8), equalTo(3));
        assertThat(log2(9), equalTo(4));

        assertThat(log2(1073741824), equalTo(30));
    }

    @Test
    public void testIsPowerOf2() {
        assertThat(isPowerOfTwo(1), equalTo(true));
        assertThat(isPowerOfTwo(2), equalTo(true));
        assertThat(isPowerOfTwo(3), equalTo(false));
        assertThat(isPowerOfTwo(4), equalTo(true));
        assertThat(isPowerOfTwo(5), equalTo(false));
        assertThat(isPowerOfTwo(6), equalTo(false));
        assertThat(isPowerOfTwo(7), equalTo(false));
        assertThat(isPowerOfTwo(8), equalTo(true));
        assertThat(isPowerOfTwo(9), equalTo(false));

        assertThat(isPowerOfTwo(1073741823), equalTo(false));
        assertThat(isPowerOfTwo(1073741824), equalTo(true));
        assertThat(isPowerOfTwo(1073741825), equalTo(false));
    }
}