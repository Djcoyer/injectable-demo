package model.exception;

public class NoSuitableConstructorException extends RuntimeException {
    private String message;

    public NoSuitableConstructorException() { }

    public NoSuitableConstructorException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
