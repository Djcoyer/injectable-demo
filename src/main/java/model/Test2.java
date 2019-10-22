package model;
import annotations.Inject;
import annotations.Injectable;

@Injectable
public class Test2 {

    @Inject
    private Test3 test3;


    public String getMessage() {
        return this.test3.message;
    }
}