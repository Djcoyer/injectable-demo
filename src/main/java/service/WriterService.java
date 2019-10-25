package service;

public class WriterService {

    private String defaultMessage;

    public WriterService(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public void writeMessage() {
        System.out.println(defaultMessage);
    }


    public void writeMessage(String message) {
        System.out.println(message);
    }
}
