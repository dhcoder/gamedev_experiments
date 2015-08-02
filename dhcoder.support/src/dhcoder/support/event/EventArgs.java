package dhcoder.support.event;

/**
 * Some events require extra arguments. In that case, you should create a data class that inherits from this base
 * interface and use an {@link ArgEvent} to send it along with the event.
 */
public interface EventArgs {}
