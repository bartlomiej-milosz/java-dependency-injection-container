package dev.bartmilo.minispringcore;

import org.junit.jupiter.api.Test;
import dev.bartmilo.minispringcore.resources.*;
import dev.bartmilo.minispringcore.exceptions.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;

public class MiniContextTest {
  private MiniContext context;

  @BeforeEach
  public void init() {
    this.context = new MiniContext();
  }

  @Test
  public void shouldInitializeAllRegisteredClassesAfterRefresh() {
    context.register(TestRepository.class);
    context.register(TestService.class);
    context.refresh();
    assertNotNull(context.getBean(TestRepository.class));
    assertNotNull(context.getBean(TestService.class));
  }

  @Test
  public void shouldThrowNoSuchBeanDefinitionExceptionWhenBeanNotRegistered() {
    assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(TestRepository.class));
  }

  @Test
  public void shouldThrowNoSuchBeanDefinitionExceptionWhenDependencyIsMissing() {
    // TestService depends on TestRepository, but only TestService is registered
    context.register(TestService.class);
    assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(TestService.class));
  }

  @Test
  public void shouldThrowBeanCreationExceptionWhenConstructorThrowsException() {
    context.register(TestFailingConstructorBean.class);
    assertThrows(BeanCreationException.class,
        () -> context.getBean(TestFailingConstructorBean.class));
  }

  @Test
  public void shouldThrowBeanCreationExceptionForInterface() {
    assertThrows(BeanRegistrationException.class, () -> context.register(Runnable.class));
  }

  @Test
  public void shouldReturnSameInstanceByDefault() {
    context.register(TestRepository.class);
    var ref1 = context.getBean(TestRepository.class);
    var ref2 = context.getBean(TestRepository.class);
    assertNotNull(ref1);
    assertNotNull(ref2);
    assertSame(ref1, ref2, "Beans should be singletons (same instance)");
  }

  @Test
  public void shouldInjectDependencies() {
    context.register(TestRepository.class);
    context.register(TestService.class);
    var testService = context.getBean(TestService.class);
    assertNotNull(testService);
    assertNotNull(testService.getTestRepository(), "Dependency should be injected");
    assertSame(testService.getTestRepository(), context.getBean(TestRepository.class),
        "Injected dependency should be the singleton instance");
  }

  @Test
  public void shouldThrowCircularDependencyExceptionWhenCycleExists() {
    context.register(TestCircularA.class);
    context.register(TestCircularB.class);

    assertThrows(CircularDependencyException.class, () -> context.getBean(TestCircularA.class));
  }

  @Test
  public void shouldResolveInterfaceToImplementation() {
    context.register(TestRepositoryInterface.class, TestRepositoryImpl.class);
    var bean = context.getBean(TestRepositoryInterface.class);
    assertNotNull(bean);
    assertTrue(bean instanceof TestRepositoryImpl);
    assertEquals("data from impl", bean.getData());
  }

  @Test
  public void shouldSelectGreediestConstructor() {
    context.register(TestRepository.class);
    context.register(MultiConstructorBean.class);
    var bean = context.getBean(MultiConstructorBean.class);
    assertNotNull(bean);
    assertNotNull(bean.getRepository(),
        "Should have picked the constructor with TestRepository param");
    assertEquals("repo-only", bean.getValue());
  }

  @Test
  public void shouldThrowExceptionWhenRegisteringInterfaceDirectly() {
    assertThrows(BeansException.class, () -> context.register(TestRepositoryInterface.class));
  }
}
