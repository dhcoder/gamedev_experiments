package dhcoder.support.event;

import java.util.ArrayList;
import java.util.List;

/**
 * Class which encapsulates the concept of an event, by providing a method to fire the event and notifying a
 * collection of listeners when it is fired.
 *
 * @param <T> is the type of the sender associated with the event.
 */
public final class Event<T> {

    private final List<EventListener<T>> listeners = new ArrayList<EventListener<T>>();

    /**
     * Add a listener to this event, a callback which will get triggered when the event happens.
     */
    public void addListener(final EventListener<T> eventListener) { listeners.add(eventListener); }

    /**
     * Remove a listener added by {@link #addListener(EventListener)}.
     */
    public void removeListener(final EventListener<T> eventListener) { listeners.remove(eventListener); }

    /**
     * Returns true if this event has any listeners attached. This may be useful to check before firing the event.
     */
    public boolean hasListeners() { return listeners.size() > 0; }

    /**
     * Fire this event, triggering all listeners.
     */
    public void fire(final T sender) {
        int listenerCount = listeners.size(); // Simple iteration to avoid Iterator allocation
        for (int i = 0; i < listenerCount; ++i) {
            listeners.get(i).run(sender);
        }
    }

    /**
     * Release all listeners.
     * <p/>
     * This is useful to do when the event is no longer used, as a listener it's holding on to may otherwise keep this
     * event reference alive longer than expected.
     */
    public void clearListeners() { listeners.clear(); }
}
