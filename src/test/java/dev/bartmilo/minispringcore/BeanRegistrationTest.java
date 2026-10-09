package dev.bartmilo.minispringcore;

import static org.junit.jupiter.api.Assertions.*;

import dev.bartmilo.minispringcore.exceptions.BeanRegistrationException;
import dev.bartmilo.minispringcore.exceptions.NoSuchBeanDefinitionException;
import org.junit.jupiter.api.Test;

class BeanRegistrationTest {
  @Test
  void bindsInterfaceToCompatibleImplementation() {
    MiniContext context = new MiniContext();
    context.register(Repository.class, RepositoryImpl.class);
    assertInstanceOf(RepositoryImpl.class, context.getBean(Repository.class));
  }

  @Test
  @SuppressWarnings({"rawtypes", "unchecked"})
  void rejectsIncompatibleImplementationEvenThroughRawTypes() {
    MiniContext context = new MiniContext();
    Class rawType = Repository.class;
    BeanRegistrationException error =
        assertThrows(
            BeanRegistrationException.class, () -> context.register(rawType, String.class));
    assertTrue(error.getMessage().contains(Repository.class.getName()));
    assertTrue(error.getMessage().contains(String.class.getName()));
    assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(Repository.class));
  }

  @Test
  void rejectsInterfaceImplementation() {
    MiniContext context = new MiniContext();
    assertThrows(
        BeanRegistrationException.class,
        () -> context.register(Repository.class, Repository.class));
  }

  @Test
  void rejectsAbstractImplementation() {
    MiniContext context = new MiniContext();
    assertThrows(
        BeanRegistrationException.class,
        () -> context.register(Repository.class, AbstractRepository.class));
    assertThrows(BeanRegistrationException.class, () -> context.register(AbstractRepository.class));
  }

  @Test
  void duplicateBeforeCreationPreservesOriginalDefinition() {
    MiniContext context = new MiniContext();
    context.register(Repository.class, RepositoryImpl.class);
    assertThrows(
        BeanRegistrationException.class,
        () -> context.register(Repository.class, RepositoryImpl.class));
    BeanRegistrationException error =
        assertThrows(
            BeanRegistrationException.class,
            () -> context.register(Repository.class, OtherRepository.class));
    assertTrue(error.getMessage().contains(Repository.class.getName()));
    assertInstanceOf(RepositoryImpl.class, context.getBean(Repository.class));
  }

  @Test
  void duplicateAfterCreationPreservesDefinitionAndInstance() {
    BeanDefinitionRegistry registry = new BeanDefinitionRegistry();
    BeanFactory factory = new BeanFactory(registry);
    registry.register(Repository.class, RepositoryImpl.class);
    Repository original = factory.getBean(Repository.class);
    assertThrows(
        BeanRegistrationException.class,
        () -> registry.register(Repository.class, OtherRepository.class));
    assertThrows(
        BeanRegistrationException.class,
        () -> registry.register(Repository.class, RepositoryImpl.class));
    assertEquals(RepositoryImpl.class, registry.getBeanDefinitions().get(Repository.class));
    assertSame(original, factory.getBean(Repository.class));
  }

  @Test
  void rejectsNullArgumentsWithoutRegisteringAnything() {
    MiniContext context = new MiniContext();
    assertThrows(NullPointerException.class, () -> context.register(null));
    assertThrows(NullPointerException.class, () -> context.register(null, RepositoryImpl.class));
    assertThrows(NullPointerException.class, () -> context.register(Repository.class, null));
    assertThrows(NullPointerException.class, () -> context.getBean(null));
    assertThrows(NullPointerException.class, () -> new BeanFactory(null));
    BeanDefinitionRegistry registry = new BeanDefinitionRegistry();
    assertThrows(NullPointerException.class, () -> registry.register(null, RepositoryImpl.class));
    assertThrows(NullPointerException.class, () -> registry.register(Repository.class, null));
    assertTrue(registry.getBeanDefinitions().isEmpty());
    assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(Repository.class));
    context.register(Repository.class, RepositoryImpl.class);
    assertNotNull(context.getBean(Repository.class));
  }

  public interface Repository {}

  public static class RepositoryImpl implements Repository {}

  public static class OtherRepository implements Repository {}

  public abstract static class AbstractRepository implements Repository {}
}
