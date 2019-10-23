import annotations.Blueprint;
import annotations.Definition;
import annotations.Inject;
import annotations.Injectable;
import model.exception.NoSuitableConstructorException;
import model.exception.UnsupportedClassException;
import org.reflections.Reflections;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Context {
    private Set<Constructor<?>> registeredConstructors;
    private Reflections reflections;
    private Set<Object> instances;


    public Context() {
        this.registeredConstructors = new HashSet<>();
        this.instances = new HashSet<>();
        reflections = new Reflections("");
    }

    public void registerClasses() {
        Set<Class<?>> injectableClasses = reflections.getTypesAnnotatedWith(Injectable.class);
        injectableClasses.forEach(this::registerClassDependencies);

        Set<Object> blueprintInstances = reflections.getTypesAnnotatedWith(Blueprint.class).stream().map(p -> {
            try {
                return p.getConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException();
            }
        }).collect(Collectors.toSet());
        Set<Method> methods = new HashSet<>();

        Map<Object, Set<Method>> methodMap = new HashMap<>();

        blueprintInstances.forEach(c -> methods.addAll(Arrays.stream(c.getClass().getDeclaredMethods()).filter(p -> p.isAnnotationPresent(Definition.class)).collect(Collectors.toList())));

    }

    private void registerClassDependencies(Class<?> classToRegister) {
        if(!classToRegister.isAnnotationPresent(Injectable.class)) {
            throw new UnsupportedClassException("Class not registered for injection");
        }

        Stream<Class<?>> stream;
        //Find constructor that has an @Inject annotation
        Constructor constructor = Arrays.stream(classToRegister.getConstructors()).filter(p -> p.getAnnotation(Inject.class) != null).findFirst().orElse(null);
        if(constructor != null) {
            stream = Arrays.stream(constructor.getParameters()).map(Parameter::getType);
        } else {
            //Find the default, zero-parameter constructor for the class
            constructor = Arrays.stream(classToRegister.getConstructors()).filter(p -> p.getParameterCount() == 0).findFirst().orElseThrow(() -> new NoSuitableConstructorException(""));
            stream = Arrays.stream(classToRegister.getDeclaredFields()).filter(p -> p.isAnnotationPresent(Inject.class)).map(Field::getType);
        }

        if(this.registeredConstructors.contains(constructor))
            return;


        //TODO: ADD A WAY TO TRACK WHICH CLASSES ARE IN PROGRESS TO BE RESOLVED
        stream.forEach(p -> {
            if(!this.registeredConstructors.contains(p.getEnclosingConstructor())) {
                registerClassDependencies(p);
            }
        });

        this.registeredConstructors.add(constructor);
    }

    public <T> T get(Class<T> classType) {
        T type = (T) instances.stream().filter(p -> p.getClass().equals(classType)).findFirst().orElse(null);
        if(type != null)
            return type;

        Constructor<T> constructor = (Constructor<T>) Arrays.stream(classType.getConstructors())
                .filter(p -> this.registeredConstructors.stream().anyMatch(s -> s.equals(p))).findFirst()
                .orElseThrow(() -> new UnsupportedClassException("Class is not registered for injection"));

        constructor.setAccessible(true);
        T instance;

        if(constructor.getParameterCount() > 0) {
            instance = getInstanceThroughConstructor(constructor);
        } else {
            try {
                instance = getInstanceThroughReflection(constructor, classType);

            } catch(Exception e) {
                throw new RuntimeException();
            }
        }

        instances.add(instance);

        return instance;

    }

    private <T> T getInstanceThroughReflection(Constructor<T> classConstructor, Class<T> classType)  {
        T instance;
        try {
            instance = classConstructor.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            throw new NoSuitableConstructorException();
        }

        Arrays.stream(classType.getDeclaredFields())
                .filter(p -> p.isAnnotationPresent(Inject.class)).forEach(field -> {
            try {
                field.setAccessible(true);
                field.set(instance, get(field.getType()));
            } catch(IllegalAccessException ex) {
                throw new UnsupportedClassException("");
            }});

        return instance;
    }

    private <T> T getInstanceThroughConstructor(Constructor<T> classConstructor) {
        List<Object> parameters = Arrays.stream(classConstructor.getParameters()).map(p -> get(p.getType())).collect(Collectors.toList());
        try {
            return classConstructor.newInstance(parameters.toArray());
        } catch(Exception e) {
            throw new NoSuitableConstructorException("");
        }
    }
}