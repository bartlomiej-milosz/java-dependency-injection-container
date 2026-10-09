package dev.bartmilo.minispringcore;

/** An educational, explicitly configured constructor injection container. Not thread-safe. */
public class MiniContext {
  private final BeanDefinitionRegistry registry = new BeanDefinitionRegistry();
  private final BeanFactory factory = new BeanFactory(registry);

  /** Registers a concrete class under its own type without constructing it. */
  public <T> void register(Class<T> clazz) {
    this.registry.register(clazz, clazz);
  }

  /**
   * Registers a compatible concrete implementation under a type.
   *
   * @throws NullPointerException if either argument is null
   * @throws dev.bartmilo.minispringcore.exceptions.BeanRegistrationException if the binding is
   *     invalid or the type is already registered
   */
  public <T> void register(Class<T> type, Class<? extends T> implementation) {
    this.registry.register(type, implementation);
  }

  /**
   * Creates all registered beans that are not already cached. Previously successful creations
   * remain cached if initialization fails; registration order is unspecified.
   */
  public void refresh() {
    // Loop through registered types and force creation
    for (Class<?> type : this.registry.getBeanDefinitions().keySet()) {
      this.factory.getBean(type);
    }
  }

  /**
   * Lazily resolves a registered type, caching one instance per registration key.
   *
   * @throws NullPointerException if the requested type is null
   */
  public <T> T getBean(Class<T> clazz) {
    return this.factory.getBean(clazz);
  }
}
