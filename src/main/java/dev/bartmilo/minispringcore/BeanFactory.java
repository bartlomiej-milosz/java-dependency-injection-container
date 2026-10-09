package dev.bartmilo.minispringcore;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import dev.bartmilo.minispringcore.annotations.Autowired;
import dev.bartmilo.minispringcore.exceptions.BeanCreationException;
import dev.bartmilo.minispringcore.exceptions.CircularDependencyException;
import dev.bartmilo.minispringcore.exceptions.NoSuchBeanDefinitionException;

public class BeanFactory {
  private final BeanDefinitionRegistry registry;
  // Circular dependency detection
  private final Set<Class<?>> beansCurrentlyInCreation = new LinkedHashSet<>();
  // Map a class to an instance
  private final Map<Class<?>, Object> beanMap = new HashMap<>();

  public BeanFactory(BeanDefinitionRegistry registry) {
    this.registry = Objects.requireNonNull(registry, "Bean registry must not be null");
  }

  public <T> T getBean(Class<T> type) {
    Objects.requireNonNull(type, "Bean type must not be null");
    if (!this.registry.getBeanDefinitions().containsKey(type)) {
      throw new NoSuchBeanDefinitionException(
          String.format("No bean named '%s' available", type.getName()));
    }
    if (beanMap.containsKey(type)) {
      return type.cast(beanMap.get(type));
    }
    Class<?> implementation = this.registry.getBeanDefinitions().get(type);
    if (!beansCurrentlyInCreation.add(type)) {
      throw new CircularDependencyException(String.format(
          "Circular dependency detected while creating bean type: %s (implementation: %s)",
          type.getName(), implementation.getName()));
    }
    try {
      T instance = type.cast(this.createBean(implementation));
      this.beanMap.put(type, instance);
      return instance;
    } finally {
      beansCurrentlyInCreation.remove(type);
    }
  }

  private Object createBean(Class<?> clazz) {
    Constructor<?> constructor = selectConstructor(clazz);
    Object[] args = resolveDependencies(constructor);
    try {
      return constructor.newInstance(args);
    } catch (InvocationTargetException e) {
      throw new BeanCreationException(
          "Constructor failed while creating bean " + clazz.getName() + ": " + constructor,
          e.getCause());
    } catch (ReflectiveOperationException | IllegalArgumentException e) {
      throw new BeanCreationException(
          "Cannot invoke constructor for bean " + clazz.getName() + ": " + constructor, e);
    }
  }

  private Object[] resolveDependencies(Constructor<?> constructor) {
    Class<?>[] paramTypes = constructor.getParameterTypes();
    Object[] args = new Object[paramTypes.length];
    for (int i = 0; i < paramTypes.length; i++) {
      args[i] = this.getBean(paramTypes[i]);
    }
    return args;
  }

  private Constructor<?> selectConstructor(Class<?> clazz) {
    Constructor<?> autowiredConstructor = null;
    Constructor<?>[] constructors = clazz.getConstructors();
    for (Constructor<?> c : constructors) {
      if (c.isAnnotationPresent(Autowired.class)) {
        if (autowiredConstructor != null) {
          throw new BeanCreationException(
              "Multiple constructors annotated with @Autowired found in: " + clazz.getName());
        }
        autowiredConstructor = c;
      }
    }
    if (autowiredConstructor != null) {
      return autowiredConstructor;
    }
    Constructor<?> greediestConstructor = null;
    boolean ambiguous = false;
    for (Constructor<?> c : constructors) {
      if (greediestConstructor == null
          || c.getParameterCount() > greediestConstructor.getParameterCount()) {
        greediestConstructor = c;
        ambiguous = false;
      } else if (c.getParameterCount() == greediestConstructor.getParameterCount()) {
        ambiguous = true;
      }
    }
    if (greediestConstructor == null) {
      throw new BeanCreationException("No public constructor found for " + clazz.getName());
    }
    if (ambiguous) {
      throw new BeanCreationException(
          "Ambiguous public constructors with " + greediestConstructor.getParameterCount()
              + " parameters in " + clazz.getName() + "; annotate one with @Autowired");
    }
    return greediestConstructor;
  }
}
