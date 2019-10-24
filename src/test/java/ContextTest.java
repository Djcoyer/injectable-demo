import annotations.Blueprint;
import annotations.Inject;
import annotations.Injectable;
import config.WriterConfig;
import model.Test2;
import model.Test3;
import model.exception.NoSuitableConstructorException;
import model.exception.UnsupportedClassException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.reflections.Reflections;
import service.WriterService;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

public class ContextTest {
    private Context context;
    private Reflections reflections;

    @Before
    public void initMocks() {
        this.reflections = Mockito.mock(Reflections.class);
        context = new Context();
        try {
            Field field = context.getClass().getDeclaredField("reflections");
            field.setAccessible(true);
            field.set(context, this.reflections);
        } catch(IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void get_returnsClassWithDependencies_classHasFieldInjection() {
        HashSet<Class<?>> classes = new HashSet<>();
        classes.add(model.Test.class);
        classes.add(Test2.class);
        classes.add(Test3.class);
        when(reflections.getTypesAnnotatedWith(Injectable.class)).thenReturn(classes);

        //act
        context.registerClasses();
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

        //act
        context.registerClasses();

        model.Test instance = context.get(model.Test.class);
        assertNotNull(instance);
    }

    @Test(expected = UnsupportedClassException.class)
    public void registerClasses_throwsUnsupportedClass_noInjectableAnnotation() {
        HashSet<Class<?>> classes = new HashSet<>();
        classes.add(BadInjectClass.class);
        when(reflections.getTypesAnnotatedWith(Injectable.class)).thenReturn(classes);

        //act
        context.registerClasses();

    }

    @Test(expected = NoSuitableConstructorException.class)
    public void registerClasses_throwsNoSuitableConstructor_noEmptyConstructorExists() {
        HashSet<Class<?>> classes = new HashSet<>();
        classes.add(InjectWithoutDefaultConstructorClass.class);

        when(reflections.getTypesAnnotatedWith(Injectable.class)).thenReturn(classes);
        context.registerClasses();
    }

    @Test(expected = UnsupportedClassException.class)
    public void registerClasses_throwsUnsupportedClass_badParameter() {
        HashSet<Class<?>> classes = new HashSet<>();
        classes.add(InjectWithBadParameter.class);

        when(reflections.getTypesAnnotatedWith(Injectable.class)).thenReturn(classes);
        context.registerClasses();
    }

    @Test
    public void get_returnsInstance_BlueprintDefinition() {
        HashSet<Class<?>> classes = new HashSet<>();
        classes.add(model.Test.class);
        classes.add(Test2.class);
        classes.add(Test3.class);
        when(reflections.getTypesAnnotatedWith(Injectable.class)).thenReturn(classes);


        Set<Class<?>> blueprints = new HashSet<>();
        blueprints.add(WriterConfig.class);
        when(reflections.getTypesAnnotatedWith(Blueprint.class)).thenReturn(blueprints);

        context.registerClasses();

        WriterService fromDefinition = context.get(WriterService.class);

        assertNotNull(fromDefinition);

        Test2 test2 = context.get(Test2.class);

        assertNotNull(test2);
    }
}


class BadInjectClass {

}

@Injectable
class InjectWithBadParameter {

    private BadInjectClass badInjectClass;

    @Inject
    public InjectWithBadParameter(BadInjectClass badInjectClass) {
        this.badInjectClass = badInjectClass;
    }
}

@Injectable
class InjectWithoutDefaultConstructorClass {
    @Inject
    BadInjectClass badInjectClass;

    public InjectWithoutDefaultConstructorClass(String message) {

    }
}