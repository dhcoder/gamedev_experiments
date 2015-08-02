package dhcoder.support.event;

/**
 * A callback which is triggered when an event happens and includes the sender that initiated the event plus any
 * relevant arguments.
 */
public interface ArgEventListener<T extends EventArgs> {
    void run(Object sender, T args);
}
