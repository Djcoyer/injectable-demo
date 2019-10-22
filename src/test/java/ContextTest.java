import annotations.Injectable;
import model.Test2;
import model.Test3;
import model.exception.NoSuitableConstructorException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.util.HashSet;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

public class ContextTest {
    private Context context;
    private Reflections reflections;

    @Before
    public void initMocks() {
        this.reflections = Mockito.mock(Reflections.class);
    }

    public void init() {
        context = new Context();
        try {
            Field field = context.getClass().getDeclaredField("reflections");
            field.setAccessible(true);
            field.set(context, this.reflections);
        } catch(Exception e) { }
    }

    @Test
    public void get_returnsClassWithDependencies_classHasFieldInjection() {
        HashSet<Class<?>> classes = new HashSet<>();
        classes.add(model.Test.class);
        classes.add(Test2.class);
        classes.add(Test3.class);
        when(reflections.getTypesAnnotatedWith(Injectable.class)).thenReturn(classes);
        init();

        Test2 instance = context.get(Test2.class);
        assertNotNull(instance);
    }

    @Test
    public void get_returnsClassWithDependencies_classHasConstructorInjection() {
        HashSet<Class<?>> classes = new HashSet<>();
        classes.add(model.Test.class);
        classes.add(Test2.class);
        classes.add(Test3.class);
        when(reflections.getTypesAnnotatedWith(Injectable.class)).thenReturn(classes);
        init();
        model.Test instance = context.get(model.Test.class);
        assertNotNull(instance);
    }

    @Test(expected = NoSuitableConstructorException.class)
    public void registerClasses_throwsNoSuitableConstructor_noEmptyConstructorExists() {
        HashSet<Class<?>> classes = new HashSet<>();
        classes.add(BadInjectClass.class);

        when(reflections.getTypesAnnotatedWith(Injectable.class)).thenReturn(classes);
        init();
    }
}


class BadInjectClass {


    public BadInjectClass(String message) {

    }
}