package dev.bartmilo.minispringcore;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import dev.bartmilo.minispringcore.exceptions.BeanRegistrationException;

/** Validates and stores explicit type-to-implementation bindings. */
public class BeanDefinitionRegistry {
  // Registered blueprints (interface/type -> concrete implementation)
  private final Map<Class<?>, Class<?>> beanDefinitions = new HashMap<>();

  public <T> void register(Class<T> type, Class<? extends T> implementation) {
    Objects.requireNonNull(type, "Bean type must not be null");
    Objects.requireNonNull(implementation, "Bean implementation must not be null");
    if (beanDefinitions.containsKey(type)) {
      throw new BeanRegistrationException("Bean type is already registered: " + type.getName());
    }
    if (!type.isAssignableFrom(implementation)) {
      throw new BeanRegistrationException(
          "Implementation " + implementation.getName() + " is not assignable to " + type.getName());
    }
    if (implementation.isInterface() || Modifier.isAbstract(implementation.getModifiers())) {
      throw new BeanRegistrationException(
          "Cannot register interface or abstract class as implementation: "
              + implementation.getName());
    }
    this.beanDefinitions.put(type, implementation);
  }

  /** Returns a live, read-only view of the registered definitions. */
  public Map<Class<?>, Class<?>> getBeanDefinitions() {
    return Collections.unmodifiableMap(this.beanDefinitions);
  }
}
