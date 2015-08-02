package dhcoder.support.collection;

import dhcoder.support.opt.Opt;
import org.junit.Test;

import java.util.List;

import static dhcoder.test.TestUtils.assertException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.IsEqual.equalTo;

public final class ArrayMapTest {

    /**
     * Class that always return a hashcode of 1, letting us test objects with different values but where the hashcode
     * would otherwise collide.
     */
    private final class HashCollisionItem {
        private final int value;

        public HashCollisionItem(final int value) {
            this.value = value;
        }

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public boolean equals(final Object o) {
            if (!(o instanceof HashCollisionItem)) {
                return false;
            }

            return value == ((HashCollisionItem)o).value;
        }
    }

    /**
     * Class that always returns a negative hashcode. Useful to make sure negative hashcodes don't break {@link
     * ArrayMap}s.
     */
    private final class NegativeHashItem {
        private final int value;

        public NegativeHashItem(final int value) {
            this.value = value;
        }

        @Override
        public int hashCode() {
            return (value > 0) ? -value : value;
        }

        @Override
        public boolean equals(final Object o) {
            if (!(o instanceof NegativeHashItem)) {
                return false;
            }

            return value == ((NegativeHashItem)o).value;
        }
    }

    @Test
    public void createMapWithIntegerKeys() {
        ArrayMap<Integer, String> numericStringMap = new ArrayMap<Integer, String>();

        numericStringMap.put(1, "one");
        numericStringMap.put(2, "two");
        numericStringMap.put(3, "three");
        numericStringMap.put(4, "four");
        numericStringMap.put(5, "five");
        numericStringMap.put(6, "six");
        numericStringMap.put(7, "seven");
        numericStringMap.put(8, "eight");
        numericStringMap.put(9, "nine");

        assertThat(numericStringMap.getSize(), equalTo(9));

        assertThat(numericStringMap.get(1), equalTo("one"));
        assertThat(numericStringMap.get(2), equalTo("two"));
        assertThat(numericStringMap.get(3), equalTo("three"));
        assertThat(numericStringMap.get(4), equalTo("four"));
        assertThat(numericStringMap.get(5), equalTo("five"));
        assertThat(numericStringMap.get(6), equalTo("six"));
        assertThat(numericStringMap.get(7), equalTo("seven"));
        assertThat(numericStringMap.get(8), equalTo("eight"));
        assertThat(numericStringMap.get(9), equalTo("nine"));

        assertThat(numericStringMap.remove(1), equalTo("one"));
        assertThat(numericStringMap.remove(2), equalTo("two"));
        assertThat(numericStringMap.remove(3), equalTo("three"));
        assertThat(numericStringMap.remove(4), equalTo("four"));
        assertThat(numericStringMap.remove(5), equalTo("five"));
        assertThat(numericStringMap.remove(6), equalTo("six"));
        assertThat(numericStringMap.remove(7), equalTo("seven"));
        assertThat(numericStringMap.remove(8), equalTo("eight"));
        assertThat(numericStringMap.remove(9), equalTo("nine"));

        assertThat(numericStringMap.getSize(), equalTo(0));
    }

    @Test
    public void createMapWithStringKeys() {
        ArrayMap<String, Integer> stringNumericMap = new ArrayMap<String, Integer>();

        stringNumericMap.put("one", 1);
        stringNumericMap.put("two", 2);
        stringNumericMap.put("three", 3);
        stringNumericMap.put("four", 4);
        stringNumericMap.put("five", 5);
        stringNumericMap.put("six", 6);
        stringNumericMap.put("seven", 7);
        stringNumericMap.put("eight", 8);
        stringNumericMap.put("nine", 9);

        assertThat(stringNumericMap.getSize(), equalTo(9));

        assertThat(stringNumericMap.get("one"), equalTo(1));
        assertThat(stringNumericMap.get("two"), equalTo(2));
        assertThat(stringNumericMap.get("three"), equalTo(3));
        assertThat(stringNumericMap.get("four"), equalTo(4));
        assertThat(stringNumericMap.get("five"), equalTo(5));
        assertThat(stringNumericMap.get("six"), equalTo(6));
        assertThat(stringNumericMap.get("seven"), equalTo(7));
        assertThat(stringNumericMap.get("eight"), equalTo(8));
        assertThat(stringNumericMap.get("nine"), equalTo(9));

        assertThat(stringNumericMap.remove("one"), equalTo(1));
        assertThat(stringNumericMap.remove("two"), equalTo(2));
        assertThat(stringNumericMap.remove("three"), equalTo(3));
        assertThat(stringNumericMap.remove("four"), equalTo(4));
        assertThat(stringNumericMap.remove("five"), equalTo(5));
        assertThat(stringNumericMap.remove("six"), equalTo(6));
        assertThat(stringNumericMap.remove("seven"), equalTo(7));
        assertThat(stringNumericMap.remove("eight"), equalTo(8));
        assertThat(stringNumericMap.remove("nine"), equalTo(9));

        assertThat(stringNumericMap.getSize(), equalTo(0));
    }

    @Test
    public void getWithOptWorks() {
        ArrayMap<Integer, String> numericStringMap = new ArrayMap<Integer, String>();

        numericStringMap.put(1, "one");
        numericStringMap.put(2, "two");
        numericStringMap.put(3, "three");
        numericStringMap.put(4, "four");
        numericStringMap.put(5, "five");
        numericStringMap.put(6, "six");
        numericStringMap.put(7, "seven");
        numericStringMap.put(8, "eight");
        numericStringMap.put(9, "nine");

        Opt<String> valueOpt = Opt.withNoValue();

        numericStringMap.get(1, valueOpt);
        assertThat(valueOpt.getValue(), equalTo("one"));

        numericStringMap.get(8, valueOpt);
        assertThat(valueOpt.getValue(), equalTo("eight"));

        numericStringMap.get(99, valueOpt);
        assertThat(valueOpt.hasValue(), equalTo(false));
    }

    @Test
    public void arrayMapHandlesHashCollisions() {
        ArrayMap<HashCollisionItem, Integer> hashCollisionMap = new ArrayMap<HashCollisionItem, Integer>();

        for (int i = 0; i < 10; ++i) {
            hashCollisionMap.put(new HashCollisionItem(i), i);
            assertThat(hashCollisionMap.getSize(), equalTo(i + 1));
        }

        for (int i = 0; i < 10; ++i) {
            assertThat(hashCollisionMap.get(new HashCollisionItem(i)), equalTo(i));
        }

        for (int i = 9; i >= 0; --i) {
            assertThat(hashCollisionMap.remove(new HashCollisionItem(i)), equalTo(i));
            assertThat(hashCollisionMap.getSize(), equalTo(i));
        }
    }

    @Test
    public void arrayMapHandlesNegativeHashCodes() {
        ArrayMap<NegativeHashItem, Integer> negativeHashMap = new ArrayMap<NegativeHashItem, Integer>();

        for (int i = 0; i < 10; ++i) {
            negativeHashMap.put(new NegativeHashItem(i), i);
            assertThat(negativeHashMap.getSize(), equalTo(i + 1));
        }

        for (int i = 0; i < 10; ++i) {
            assertThat(negativeHashMap.get(new NegativeHashItem(i)), equalTo(i));
        }

        for (int i = 9; i >= 0; --i) {
            assertThat(negativeHashMap.remove(new NegativeHashItem(i)), equalTo(i));
            assertThat(negativeHashMap.getSize(), equalTo(i));
        }
    }

    @Test
    public void arrayMapHandlesGrowingCorrectly() {
        ArrayMap<Integer, Integer> numericMap = new ArrayMap<Integer, Integer>(1);

        for (int i = 0; i < 10000; ++i) {
            numericMap.put(i, i);
            assertThat(numericMap.getSize(), equalTo(i + 1));
        }

        for (int i = 0; i < 10000; ++i) {
            assertThat(numericMap.get(i), equalTo(i));
        }
    }

    @Test
    public void removingElementsDoesntBreakProbing() {
        ArrayMap<HashCollisionItem, Integer> hashCollisionMap = new ArrayMap<HashCollisionItem, Integer>();

        for (int i = 0; i < 10; ++i) {
            hashCollisionMap.put(new HashCollisionItem(i), i);
        }

        for (int i = 0; i < 5; ++i) {
            hashCollisionMap.remove(new HashCollisionItem(i));
        }

        for (int i = 0; i < 5; ++i) {
            assertThat(hashCollisionMap.containsKey(new HashCollisionItem(i)), equalTo(false));
        }

        for (int i = 5; i < 10; ++i) {
            // We can still find items added later into the map even when earlier items were removed
            assertThat(hashCollisionMap.get(new HashCollisionItem(i)), equalTo(i));
        }

        for (int i = 0; i < 5; ++i) {
            // We can put back the items, and they should reuse existing slots
            hashCollisionMap.put(new HashCollisionItem(i), i);
        }

        for (int i = 0; i < 10; ++i) {
            assertThat(hashCollisionMap.containsKey(new HashCollisionItem(i)), equalTo(true));
        }
    }

    /**
     * Removing elements leaves dead buckets behind. Make sure if we aggressively add and remove elements, these dead
     * buckets still work fine.
     */
    @Test
    public void removingElementsDoesntBreakGetQuery() {

        ArrayMap<Integer, Integer> numericMap = new ArrayMap<Integer, Integer>(10);
        int capactiy = numericMap.getCapacity();

        // Add and remove a key for each index of the map. This will effectively leave the map empty but full of dead
        // spaces (which are saved for probing reasons).
        for (int i = 0; i < capactiy; ++i) {
            numericMap.put(i, i);
            numericMap.remove(i);
        }

        // Adding/removing keys shouldn't have triggered a resize
        assertThat(numericMap.getCapacity(), equalTo(capactiy));
        assertThat(numericMap.getSize(), equalTo(0));

        // Checking the value of a key should loop around the whole table once, since the probing will keep encountering
        // dead spaces. The map should detect this and exit without running into an infinite loop.
        assertThat(numericMap.containsKey(1), equalTo(false));
    }

    @Test
    public void replaceCanOnlyReplaceExistingKeys() {
        final ArrayMap<Integer, String> numericStringMap = new ArrayMap<Integer, String>();
        numericStringMap.put(1, "oone");

        numericStringMap.replace(1, "one");
        assertThat(numericStringMap.get(1), equalTo("one"));

        assertException("Can only replace key if it is already in the map", IllegalStateException.class,
            new Runnable() {
                @Override
                public void run() {
                    numericStringMap.replace(2, "two");
                }
            });
    }

    @Test
    public void putOrReplaceCanBothPutAndReplace() {
        ArrayMap<Integer, String> numericStringMap = new ArrayMap<Integer, String>();
        numericStringMap.put(1, "oone");

        numericStringMap.putOrReplace(1, "one");
        numericStringMap.putOrReplace(2, "two");

        assertThat(numericStringMap.get(1), equalTo("one"));
        assertThat(numericStringMap.get(2), equalTo("two"));
    }

    @Test
    public void getValuesReturnsCompactListOfValues() {
        ArrayMap<Integer, String> numericStringMap = new ArrayMap<Integer, String>(100);
        numericStringMap.put(1, "one");
        numericStringMap.put(2, "two");
        numericStringMap.put(3, "three");

        final List<String> values = numericStringMap.getValues();
        assertThat(values.size(), equalTo(3));
        assertThat(values, contains("one", "two", "three"));
   }
}