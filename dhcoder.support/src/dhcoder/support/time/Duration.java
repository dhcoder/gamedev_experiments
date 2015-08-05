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

    public static Duration fromSeconds(float secs) {
        Duration duration = new Duration();
        duration.setSeconds(secs);
        return duration;
    }

    public static Duration fromMinutes(float minutes) {
        Duration duration = new Duration();
        duration.setMinutes(minutes);
        return duration;
    }

    public static Duration fromMilliseconds(float milliseconds) {
        Duration duration = new Duration();
        duration.setMilliseconds(milliseconds);
        return duration;
    }

    public static Duration from(Duration duration) {
        Duration clonedDuration = new Duration();
        clonedDuration.set(duration);
        return clonedDuration;
    }

    private float seconds;

    /**
     * Use {@link #fromSeconds(float)}, {@link #fromMinutes(float)}, or {@link #fromMilliseconds(float)} instead.
     */
    private Duration() {}

    public float getSeconds() {
        return seconds;
    }

    public Duration setSeconds(float secs) {
        seconds = (secs > 0f) ? secs : 0f;
        return this;
    }

    public float getMinutes() {
        return seconds / 60f;
    }

    public Duration setMinutes(float minutes) {
        setSeconds(minutes * 60f);
        return this;
    }

    public float getMilliseconds() {
        return seconds * 1000f;
    }

    public Duration setMilliseconds(float milliseconds) {
        setSeconds(milliseconds / 1000f);
        return this;
    }

    public Duration set(Duration duration) {
        setSeconds(duration.seconds);
        return this;
    }

    public Duration addSeconds(float secs) {
        setSeconds(getSeconds() + secs);
        return this;
    }

    public Duration addMinutes(float minutes) {
        setMinutes(getMinutes() + minutes);
        return this;
    }

    public Duration addMilliseconds(float milliseconds) {
        setMilliseconds(getMilliseconds() + milliseconds);
        return this;
    }

    public Duration add(Duration duration) {
        setSeconds(getSeconds() + duration.getSeconds());
        return this;
    }

    public Duration subtractSeconds(float secs) {
        setSeconds(getSeconds() - secs);
        return this;
    }

    public Duration subtractMinutes(float minutes) {
        setMinutes(getMinutes() - minutes);
        return this;
    }

    public Duration subtractMilliseconds(float milliseconds) {
        setMilliseconds(getMilliseconds() - milliseconds);
        return this;
    }

    public Duration subtract(Duration duration) {
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
