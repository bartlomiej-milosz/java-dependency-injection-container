package dev.bartmilo.minispringcore;

import static org.junit.jupiter.api.Assertions.*;

import dev.bartmilo.minispringcore.exceptions.BeanCreationException;
import dev.bartmilo.minispringcore.exceptions.CircularDependencyException;
import dev.bartmilo.minispringcore.exceptions.NoSuchBeanDefinitionException;
import dev.bartmilo.minispringcore.resources.TestCircularA;
import dev.bartmilo.minispringcore.resources.TestCircularB;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BeanLifecycleTest {
  @BeforeEach
  void resetCounters() {
    CountingRepository.created = 0;
    FailingConsumer.attempts = 0;
  }

  @Test
  void registrationDoesNotConstructBeans() {
    MiniContext context = new MiniContext();
    context.register(CountingRepository.class);
    assertEquals(0, CountingRepository.created);
    CountingRepository bean = context.getBean(CountingRepository.class);
    assertEquals(1, CountingRepository.created);
    assertSame(bean, context.getBean(CountingRepository.class));
    assertEquals(1, CountingRepository.created);
  }

  @Test
  void refreshConstructsAllBeansBeforeGetBeanAndDoesNotRebuildSingletons() {
    MiniContext context = new MiniContext();
    context.register(CountingRepository.class);
    context.register(Repository.class, CountingRepository.class);
    assertEquals(0, CountingRepository.created);
    context.refresh();
    assertEquals(2, CountingRepository.created);
    context.refresh();
    assertEquals(2, CountingRepository.created);
    CountingRepository concrete = context.getBean(CountingRepository.class);
    Repository bound = context.getBean(Repository.class);
    assertNotSame(concrete, bound);
    assertSame(concrete, context.getBean(CountingRepository.class));
    assertSame(bound, context.getBean(Repository.class));
    assertEquals(2, CountingRepository.created);
  }

  @Test
  void differentConsumersShareDependencyInstance() {
    MiniContext context = new MiniContext();
    context.register(CountingRepository.class);
    context.register(Consumer.class);
    context.register(OtherConsumer.class);
    assertSame(
        context.getBean(Consumer.class).repository,
        context.getBean(OtherConsumer.class).repository);
    assertSame(
        context.getBean(CountingRepository.class), context.getBean(Consumer.class).repository);
    assertEquals(1, CountingRepository.created);
  }

  @Test
  void contextsHaveSeparateInstances() {
    MiniContext first = new MiniContext();
    MiniContext second = new MiniContext();
    first.register(CountingRepository.class);
    second.register(CountingRepository.class);
    assertNotSame(
        first.getBean(CountingRepository.class), second.getBean(CountingRepository.class));
    assertEquals(2, CountingRepository.created);
  }

  @Test
  void sameImplementationUnderDifferentKeysHasSeparateInstancesWithoutRefresh() {
    MiniContext context = new MiniContext();
    context.register(Repository.class, CountingRepository.class);
    context.register(CountingRepository.class);
    Repository bound = context.getBean(Repository.class);
    CountingRepository concrete = context.getBean(CountingRepository.class);
    assertNotSame(bound, concrete);
    assertSame(bound, context.getBean(Repository.class));
    assertSame(concrete, context.getBean(CountingRepository.class));
  }

  @Test
  void registrationOrderDoesNotAffectDependencyResolution() {
    MiniContext context = new MiniContext();
    context.register(Consumer.class);
    context.register(CountingRepository.class);
    assertSame(
        context.getBean(CountingRepository.class), context.getBean(Consumer.class).repository);
  }

  @Test
  void failedRefreshKeepsSuccessfullyConstructedDependency() {
    MiniContext context = new MiniContext();
    context.register(FailingConsumer.class);
    context.register(CountingRepository.class);
    assertThrows(BeanCreationException.class, context::refresh);
    // The dependency must be constructed before FailingConsumer can throw,
    // regardless of which registration refresh visits first.
    assertEquals(1, CountingRepository.created);
    assertEquals(1, FailingConsumer.attempts);
    CountingRepository repository = context.getBean(CountingRepository.class);
    assertEquals(1, CountingRepository.created);
    assertThrows(BeanCreationException.class, context::refresh);
    assertEquals(2, FailingConsumer.attempts);
    assertSame(repository, context.getBean(CountingRepository.class));
    assertEquals(1, CountingRepository.created);
  }

  @Test
  void constructorFailurePreservesOriginalCauseAndDoesNotCacheFailedBean() {
    MiniContext context = new MiniContext();
    context.register(CountingRepository.class);
    context.register(FailingConsumer.class);
    BeanCreationException first =
        assertThrows(BeanCreationException.class, () -> context.getBean(FailingConsumer.class));
    assertSame(FailingConsumer.FAILURE, first.getCause());
    assertTrue(first.getMessage().contains(FailingConsumer.class.getName()));
    BeanCreationException second =
        assertThrows(BeanCreationException.class, () -> context.getBean(FailingConsumer.class));
    assertSame(FailingConsumer.FAILURE, second.getCause());
    assertEquals(2, FailingConsumer.attempts);
    assertEquals(1, CountingRepository.created);
  }

  @Test
  void canRetryAfterRegisteringMissingDependency() {
    MiniContext context = new MiniContext();
    context.register(Consumer.class);
    for (int attempt = 0; attempt < 2; attempt++) {
      assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(Consumer.class));
    }
    context.register(CountingRepository.class);
    Consumer consumer = context.getBean(Consumer.class);
    assertSame(context.getBean(CountingRepository.class), consumer.repository);
    assertSame(consumer, context.getBean(Consumer.class));
  }

  @Test
  void detectsSelfDependencyAndCleansCreationState() {
    MiniContext context = new MiniContext();
    context.register(SelfDependent.class);
    for (int attempt = 0; attempt < 2; attempt++) {
      CircularDependencyException error =
          assertThrows(
              CircularDependencyException.class, () -> context.getBean(SelfDependent.class));
      assertTrue(error.getMessage().contains(SelfDependent.class.getName()));
    }
    context.register(CountingRepository.class);
    assertNotNull(context.getBean(CountingRepository.class));
  }

  @Test
  void detectsIndirectCycleOnRepeatedAttempts() {
    MiniContext context = new MiniContext();
    context.register(TestCircularA.class);
    context.register(TestCircularB.class);
    assertThrows(CircularDependencyException.class, () -> context.getBean(TestCircularA.class));
    assertThrows(CircularDependencyException.class, () -> context.getBean(TestCircularB.class));
  }

  @Test
  void detectsCycleThroughInterfaceBinding() {
    MiniContext context = new MiniContext();
    context.register(CyclicRepository.class, CyclicRepositoryImpl.class);
    context.register(CyclicService.class);
    CircularDependencyException error =
        assertThrows(
            CircularDependencyException.class, () -> context.getBean(CyclicRepository.class));
    assertTrue(error.getMessage().contains(CyclicRepository.class.getName()));
    assertTrue(error.getMessage().contains(CyclicRepositoryImpl.class.getName()));
    assertThrows(CircularDependencyException.class, () -> context.getBean(CyclicService.class));
  }

  @Test
  void definitionsViewCannotBypassRegistrationValidation() {
    BeanDefinitionRegistry registry = new BeanDefinitionRegistry();
    registry.register(Repository.class, CountingRepository.class);
    Map<Class<?>, Class<?>> definitions = registry.getBeanDefinitions();
    assertThrows(
        UnsupportedOperationException.class, () -> definitions.put(Repository.class, String.class));
    assertThrows(UnsupportedOperationException.class, definitions::clear);
    assertThrows(
        UnsupportedOperationException.class,
        () -> definitions.entrySet().iterator().next().setValue(String.class));
    assertEquals(CountingRepository.class, registry.getBeanDefinitions().get(Repository.class));
    assertInstanceOf(CountingRepository.class, new BeanFactory(registry).getBean(Repository.class));
  }

  public interface Repository {}

  public static class CountingRepository implements Repository {
    static int created;

    public CountingRepository() {
      created++;
    }
  }

  public static class Consumer {
    final CountingRepository repository;

    public Consumer(CountingRepository repository) {
      this.repository = repository;
    }
  }

  public static class OtherConsumer {
    final CountingRepository repository;

    public OtherConsumer(CountingRepository repository) {
      this.repository = repository;
    }
  }

  public static class FailingConsumer {
    static final IllegalStateException FAILURE = new IllegalStateException("Constructor failed");
    static int attempts;

    public FailingConsumer(CountingRepository repository) {
      attempts++;
      throw FAILURE;
    }
  }

  public static class SelfDependent {
    public SelfDependent(SelfDependent self) {}
  }

  public interface CyclicRepository {}

  public static class CyclicRepositoryImpl implements CyclicRepository {
    public CyclicRepositoryImpl(CyclicService service) {}
  }

  public static class CyclicService {
    public CyclicService(CyclicRepository repository) {}
  }
}
