package dhcoder.support.math;

import junit.framework.TestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class BitUtilsTest extends TestCase {

    public void testRequireSingleBit() throws Exception {
        for (int i = 0; i < 32; i++) {
            assertThat(BitUtils.hasSingleBit(1 << i), equalTo(true));
        }

        assertThat(BitUtils.hasSingleBit(3), equalTo(false));
        assertThat(BitUtils.hasSingleBit(11), equalTo(false));
        assertThat(BitUtils.hasSingleBit(99381), equalTo(false));
    }

    public void testGetBitIndex() throws Exception {
        for (int i = 0; i < 32; i++) {
            assertThat(BitUtils.getBitIndex(1 << i), equalTo(i));
        }
    }
}