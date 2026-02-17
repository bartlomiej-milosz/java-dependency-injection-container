package dev.bartmilo.minispringcore;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import dev.bartmilo.minispringcore.exceptions.BeanRegistrationException;

public class BeanDefinitionRegistry {
  // Registered blueprints (interface/type -> concrete implementation)
  private final Map<Class<?>, Class<?>> beanDefinitions = new HashMap<>();

  public void register(Class<?> type, Class<?> implementation) {
    if (implementation.isInterface() || Modifier.isAbstract(implementation.getModifiers())) {
      throw new BeanRegistrationException(
          "Cannot register interface or abstract class as implementation: "
              + implementation.getName());
    }
    this.beanDefinitions.put(type, implementation);
  }

  public Map<Class<?>, Class<?>> getBeanDefinitions() {
    return this.beanDefinitions;
  }
}
