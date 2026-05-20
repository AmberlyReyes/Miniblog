package app.util;

public class NoExisteUsuarioException extends RuntimeException {

    public NoExisteUsuarioException(String message) {
        super(message);
    }
}
