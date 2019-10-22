package model.exception;

public class UnsupportedClassException extends RuntimeException {
    private String message;

    public UnsupportedClassException(String message) {
        this.message = message;
    }

    public UnsupportedClassException(String message, String message1) {
        super(message);
        this.message = message1;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
