package dev.bartmilo.minispringcore;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MiniContext {
  // It holds the blueprints - the classes that we want to create
  private final List<Class<?>> componentClasses = new ArrayList<>();
  // Map a class to an instance (singleton)
  private final Map<Class<?>, Object> beanMap = new HashMap<>();

  public void register(Class<?> clazz) {
    this.componentClasses.add(clazz);
  }
  
  public <T> T getBean(Class<T> clazz) {
    if (beanMap.containsKey(clazz)) {
      return (T) beanMap.get(clazz);
    }
    T instance = this.getConstructorInstance(clazz);
    this.beanMap.put(clazz, instance);
    return instance;
  }

  private <T> T getConstructorInstance(Class<T> clazz) {
    try {
      Constructor<?> constructor = clazz.getConstructors()[0];
      Class<?>[] paramTypes = constructor.getParameterTypes();
      Object[] args = new Object[paramTypes.length];
      for (int i = 0; i < paramTypes.length; i++) {
        args[i] = this.getBean(paramTypes[i]);
      }
      return (T) constructor.newInstance(args);
    } catch (Exception e) {
      throw new BeanException(
          String.format(
              "Failed to instantiate bean: %s. Exception Message: %s",
              clazz.getName(), e.getMessage()));
    }
  }
}
