package model;

import annotations.Inject;
import annotations.Injectable;

@Injectable
public class Test {

    private Test2 test2;

    @Inject
    public Test(Test2 test2) {
        this.test2 = test2;
    }



}
