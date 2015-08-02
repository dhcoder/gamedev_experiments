package dhcoder.support.time;

import dhcoder.support.memory.Poolable;

import static dhcoder.support.text.StringUtils.format;

/**
 * An class which represents a time duration.
 */
public final class Duration implements Poolable {

    public static Duration zero() {
        return new Duration();
    }

    public static Duration fromSeconds(final float secs) {
        Duration duration = new Duration();
        duration.setSeconds(secs);
        return duration;
    }

    public static Duration fromMinutes(final float minutes) {
        Duration duration = new Duration();
        duration.setMinutes(minutes);
        return duration;
    }

    public static Duration fromMilliseconds(final float milliseconds) {
        Duration duration = new Duration();
        duration.setMilliseconds(milliseconds);
        return duration;
    }

    public static Duration from(final Duration duration) {
        Duration clonsedDuration = new Duration();
        clonsedDuration.setFrom(duration);
        return clonsedDuration;
    }

    private float seconds;

    /**
     * Use {@link #fromSeconds(float)}, {@link #fromMinutes(float)}, or {@link #fromMilliseconds(float)} instead.
     */
    private Duration() {}

    public float getSeconds() {
        return seconds;
    }

    public Duration setSeconds(final float secs) {
        seconds = (secs > 0f) ? secs : 0f;
        return this;
    }

    public float getMinutes() {
        return seconds / 60f;
    }

    public Duration setMinutes(final float minutes) {
        setSeconds(minutes * 60f);
        return this;
    }

    public float getMilliseconds() {
        return seconds * 1000f;
    }

    public Duration setMilliseconds(final float milliseconds) {
        setSeconds(milliseconds / 1000f);
        return this;
    }

    public Duration setFrom(final Duration duration) {
        setSeconds(duration.seconds);
        return this;
    }

    public Duration addSeconds(final float secs) {
        setSeconds(getSeconds() + secs);
        return this;
    }

    public Duration addMinutes(final float minutes) {
        setMinutes(getMinutes() + minutes);
        return this;
    }

    public Duration addMilliseconds(final float milliseconds) {
        setMilliseconds(getMilliseconds() + milliseconds);
        return this;
    }

    public Duration add(final Duration duration) {
        setSeconds(getSeconds() + duration.getSeconds());
        return this;
    }

    public Duration subtractSeconds(final float secs) {
        setSeconds(getSeconds() - secs);
        return this;
    }

    public Duration subtractMinutes(final float minutes) {
        setMinutes(getMinutes() - minutes);
        return this;
    }

    public Duration subtractMilliseconds(final float milliseconds) {
        setMilliseconds(getMilliseconds() - milliseconds);
        return this;
    }

    public Duration subtract(final Duration duration) {
        setSeconds(getSeconds() - duration.getSeconds());
        return this;
    }

    public Duration setZero() {
        setSeconds(0f);
        return this;
    }

    public boolean isZero() { return seconds == 0f; }

    @Override
    public void reset() {
        setZero();
    }

    @Override
    public String toString() {
        return format("{0}s", seconds);
    }
}
