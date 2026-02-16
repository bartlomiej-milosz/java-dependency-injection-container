package dev.bartmilo.minispringcore;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import dev.bartmilo.minispringcore.exceptions.BeanCreationException;
import dev.bartmilo.minispringcore.exceptions.CircularDependencyException;
import dev.bartmilo.minispringcore.exceptions.NoSuchBeanDefinitionException;

public class MiniContext {
  // Registered blueprints
  private final Set<Class<?>> componentClasses = new LinkedHashSet<>();
  // Circular dependency detection
  private final Set<Class<?>> beansCurrentlyInCreation = new LinkedHashSet<>();
  // Map a class to an instance
  private final Map<Class<?>, Object> beanMap = new HashMap<>();

  public void register(Class<?> clazz) {
    this.componentClasses.add(clazz);
  }

  public void refresh() {
    // Loop through registered classes and force creation
    for (Class<?> clazz : componentClasses) {
      this.getBean(clazz);
    }
  }

  public <T> T getBean(Class<T> clazz) {
    if (!this.componentClasses.contains(clazz)) {
      throw new NoSuchBeanDefinitionException(
          String.format("No bean named '%s' available", clazz.getName()));
    }
    if (beanMap.containsKey(clazz)) {
      return (T) beanMap.get(clazz);
    }
    if (!beansCurrentlyInCreation.add(clazz)) {
      throw new CircularDependencyException(
          String.format("Circular dependency detected while creating bean: %s", clazz.getName()));
    }
    try {
      T instance = this.createBean(clazz);
      this.beanMap.put(clazz, instance);
      return instance;
    } finally {
      beansCurrentlyInCreation.remove(clazz);
    }
  }

  private <T> T createBean(Class<T> clazz) {
    try {
      Constructor<?> constructor = clazz.getConstructors()[0];
      Class<?>[] paramTypes = constructor.getParameterTypes();
      Object[] args = new Object[paramTypes.length];
      for (int i = 0; i < paramTypes.length; i++) {
        args[i] = this.getBean(paramTypes[i]);
      }
      return (T) constructor.newInstance(args);
    } catch (Exception e) {
      throw new BeanCreationException(
          String.format("Error creating bean with name '%s'", clazz.getName()), e);
    }
  }
}
