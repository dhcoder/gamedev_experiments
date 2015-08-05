package dhcoder.libgdx.assets.serialization;

/**
 * Exception to throw when there's an issue with serializing or deserializing a file.
 */
public final class SerialziationException extends RuntimeException {
    public SerialziationException(String message) {
        super(message);
    }
}
