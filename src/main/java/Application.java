import model.Test2;

public class Application {

    public static void main(String[] args) {
        Context context = new Context();
        context.registerClasses();

        Test2 test = context.get(Test2.class);

        System.out.println(test.getMessage());
    }
}
