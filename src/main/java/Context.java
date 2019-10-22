import annotations.Inject;
import annotations.Injectable;
import model.exception.NoSuitableConstructorException;
import model.exception.UnsupportedClassException;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

public class Context {
    private Set<Constructor<?>> registeredConstructors;
    private Reflections reflections;
    private Set<Object> instances;


    public Context() {
        this.registeredConstructors = new HashSet<>();
        this.instances = new HashSet<>();
        reflections = new Reflections("");
        this.registerClasses();
    }

    public void registerClasses() {
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(Injectable.class);
        annotatedClasses.forEach(this::registerClassDependencies);
    }

    private void registerClassDependencies(Class<?> classToRegister) {
        if(!classToRegister.isAnnotationPresent(Injectable.class)) {
            throw new UnsupportedClassException("Class not registered for injection");
        }


        List<Class<?>> autowiredDependencies;

        Constructor constructor = Arrays.stream(classToRegister.getConstructors()).filter(p -> p.getAnnotation(Inject.class) != null).findFirst().orElse(null);
        if(constructor != null) {
            autowiredDependencies = Arrays.stream(constructor.getParameters()).map(Parameter::getType).collect(Collectors.toList());
        } else {
            constructor = Arrays.stream(classToRegister.getConstructors()).filter(p -> p.getParameterCount() == 0).findFirst().orElse(null);
            if(constructor == null) {
                throw new NoSuitableConstructorException();
                //Handle error since no default constructor exists
            }
            autowiredDependencies = Arrays.stream(classToRegister.getDeclaredFields()).filter(p -> p.isAnnotationPresent(Inject.class)).map(Field::getType).collect(Collectors.toList());
        }

        if(this.registeredConstructors.contains(constructor))
            return;

        autowiredDependencies.forEach(p -> {
            if(!this.registeredConstructors.contains(p.getEnclosingConstructor())) {
                registerClassDependencies(p);
            }
        });

        this.registeredConstructors.add(constructor);
    }

    public <T> T get(Class<T> classType) {
        try {
            if(!this.registeredConstructors.contains(classType.getConstructor()))
                throw new UnsupportedClassException("Class is not registered for injection");
        } catch (NoSuchMethodException e) {
            throw new NoSuitableConstructorException("No constructor for class exists.");
        }
        Optional<T> typeOptional = (Optional<T>) instances.stream().filter(p -> p.getClass().equals(classType)).findFirst();
        if(typeOptional.isPresent())
            return typeOptional.get();


        Constructor<T> constructor = (Constructor<T>) Arrays.stream(classType.getConstructors())
                .filter(p -> this.registeredConstructors.stream().anyMatch(s -> s.equals(p))).findFirst().orElse(null);
        if(constructor == null)
            return null;
        constructor.setAccessible(true);
        T instance;

        if(constructor.getParameterCount() > 0) {
            instance = getInstanceThroughConstructor(constructor, classType);
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

    private <T> T getInstanceThroughReflection(Constructor<T> classConstructor, Class<T> classType) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        T instance = classConstructor.newInstance();
        List<Field> autowiredFields = Arrays.stream(classType.getDeclaredFields())
                .filter(p -> p.isAnnotationPresent(Inject.class)).collect(Collectors.toList());
        autowiredFields.forEach(field -> {
            try {
                field.setAccessible(true);
                field.set(instance, get(field.getType()));
            } catch(IllegalAccessException ex) {
//                        throw new RuntimeException();
            }

        });

        return instance;
    }

    private <T> T getInstanceThroughConstructor(Constructor<T> classConstructor, Class<T> classType) {
        List<Object> parameters = new ArrayList<>();
        for(Parameter parameter: classConstructor.getParameters()) {
            parameters.add(get(parameter.getType()));
        }
        try {
            return classConstructor.newInstance(parameters.toArray());
        } catch(Exception e) {
            throw new RuntimeException("");
        }
    }
}