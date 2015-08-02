package dhcoder.support.math;

/**
 * Simply class to contain the logic of doing a binary search
 */
public final class BinarySearch {
    private int minIndex;
    private int maxIndex;
    private int currentIndex;
    private int acceptedIndex;

    public void initialize(final int maxIndex) {
        initialize(0, maxIndex);
    }

    public void initialize(final int minIndex, final int maxIndex) {
        this.minIndex = minIndex;
        this.maxIndex = maxIndex;

        currentIndex = minIndex;
        acceptedIndex = minIndex;

        calculateNextIndex();
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public int getAcceptedIndex() {
        return acceptedIndex;
    }

    /**
     * Convenience method which, in turn, calls {@link #acceptCurrentIndex()} or {@link #rejectCurrentIndex()}
     * appropriately.
     */
    public void acceptCurrentIndexIf(final boolean condition) {
        if (condition) {
            acceptCurrentIndex();
        }
        else {
            rejectCurrentIndex();
        }
    }

    public void acceptCurrentIndex() {
        acceptedIndex = currentIndex;
        minIndex = currentIndex + 1;
        calculateNextIndex();
    }

    public void rejectCurrentIndex() {
        maxIndex = currentIndex - 1;
        calculateNextIndex();
    }

    public boolean isFinished() {
        return (maxIndex < minIndex);
    }

    private int calculateNextIndex() {
        currentIndex = minIndex + (maxIndex - minIndex) / 2;
        return currentIndex;
    }

}
