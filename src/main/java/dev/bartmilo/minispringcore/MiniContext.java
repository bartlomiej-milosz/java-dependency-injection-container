package dev.bartmilo.minispringcore;

public class MiniContext {
  private final BeanDefinitionRegistry registry = new BeanDefinitionRegistry();
  private final BeanFactory factory = new BeanFactory(registry);

  public <T> void register(Class<T> clazz) {
    this.registry.register(clazz, clazz);
  }

  public <T> void register(Class<T> type, Class<? extends T> implementation) {
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
