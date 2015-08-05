package dhcoder.support.event;

import java.util.ArrayList;

/**
 * Like {@link Event} but includes additional {@link EventArgs} when fired.
 */
public final class ArgEvent<T extends EventArgs> {

    private final ArrayList<ArgEventListener<T>> listeners = new ArrayList<ArgEventListener<T>>();

    public void addListener(ArgEventListener<T> listener) {
        listeners.add(listener);
    }

    public void removeListener(ArgEventListener<T> listener) {
        listeners.remove(listener);
    }

    public boolean hasListeners() { return listeners.size() > 0; }

    /**
     * Fire this event, triggering all listeners.
     */
    public void fire(Object sender, T args) {
        int numListeners = listeners.size();
        for (int i = 0; i < numListeners; ++i) {
            ArgEventListener<T> listener = listeners.get(i);
            listener.run(sender, args);
        }
    }

    /**
     * Release all listeners.
     * <p/>
     * This is useful to do when the event is no longer used, as a listener it's holding on to may otherwise keep it
     * alive longer than expected.
     */
    public void clearListeners() {
        listeners.clear();
    }
}
