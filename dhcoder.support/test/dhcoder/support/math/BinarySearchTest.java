package dhcoder.support.math;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public final class BinarySearchTest {

    private BinarySearch binarySearch;
    @Before
    public void setUp() {
        binarySearch = new BinarySearch();
    }

    @Test
    public void testBinarySearchFindsAllSubdivisions() {

        for (int cutoffValue = 0; cutoffValue <= 10; ++cutoffValue) {
            binarySearch.initialize(10);

            while (!binarySearch.isFinished()) {
                binarySearch.acceptCurrentIndexIf(binarySearch.getCurrentIndex() <= cutoffValue);
            }

            assertThat(binarySearch.getAcceptedIndex(), equalTo(cutoffValue));
        }
    }
}