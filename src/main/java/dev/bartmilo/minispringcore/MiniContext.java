package dev.bartmilo.minispringcore;

public class MiniContext {
  private final BeanDefinitionRegistry registry = new BeanDefinitionRegistry();
  private final BeanFactory factory = new BeanFactory(registry);

  public void register(Class<?> clazz) {
    this.registry.register(clazz, clazz);
  }

  public void register(Class<?> type, Class<?> implementation) {
    this.registry.register(type, implementation);
  }

  public void refresh() {
    // Loop through registered types and force creation
    for (Class<?> type : this.registry.getBeanDefinitions().keySet()) {
      this.factory.getBean(type);
    }
  }

  public <T> T getBean(Class<T> clazz) {
    return this.factory.getBean(clazz);
  }
}
