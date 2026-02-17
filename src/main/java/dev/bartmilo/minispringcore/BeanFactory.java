package dev.bartmilo.minispringcore;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
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
    this.registry = registry;
  }

  public <T> T getBean(Class<T> type) {
    if (!this.registry.getBeanDefinitions().containsKey(type)) {
      throw new NoSuchBeanDefinitionException(
          String.format("No bean named '%s' available", type.getName()));
    }
    if (beanMap.containsKey(type)) {
      return (T) beanMap.get(type);
    }
    Class<?> implementation = this.registry.getBeanDefinitions().get(type);
    if (!beansCurrentlyInCreation.add(implementation)) {
      throw new CircularDependencyException(String.format(
          "Circular dependency detected while creating bean: %s", implementation.getName()));
    }
    try {
      T instance = (T) this.createBean(implementation);
      this.beanMap.put(type, instance);
      return instance;
    } finally {
      beansCurrentlyInCreation.remove(implementation);
    }
  }

  private Object createBean(Class<?> clazz) {
    Constructor<?> constructor = getConstructorWithTheMostParameters(clazz);
    Object[] args = resolveDependencies(constructor);
    try {
      return constructor.newInstance(args);
    } catch (Exception e) {
      throw new BeanCreationException(
          String.format("Error creating bean with name '%s'", clazz.getName()), e);
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

  private Constructor<?> getConstructorWithTheMostParameters(Class<?> clazz) {
    // Find the "greediest" constructor (the one with the most parameters)
    Constructor<?> constructor = null;
    for (Constructor<?> c : clazz.getConstructors()) {
      if (constructor == null || c.getParameterCount() > constructor.getParameterCount()) {
        constructor = c;
      }
    }
    if (constructor == null) {
      throw new BeanCreationException("No public constructor found for " + clazz.getName());
    }
    return constructor;
  }
}
