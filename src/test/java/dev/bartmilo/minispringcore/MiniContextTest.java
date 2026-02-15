package dev.bartmilo.minispringcore;

import org.junit.jupiter.api.Test;
import dev.bartmilo.minispringcore.exceptions.BeanCreationException;
import dev.bartmilo.minispringcore.exceptions.NoSuchBeanDefinitionException;
import dev.bartmilo.minispringcore.resources.FailingConstructorBean;
import dev.bartmilo.minispringcore.resources.TestRepository;
import dev.bartmilo.minispringcore.resources.TestService;
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
  public void shouldThrowBeanCreationExceptionWhenDependencyIsMissing() {
    // TestService depends on TestRepository, but only TestService is registered
    context.register(TestService.class);
    BeanCreationException exception =
        assertThrows(BeanCreationException.class, () -> context.getBean(TestService.class));
    // The cause should be the NoSuchBeanDefinitionException for the missing repository
    assertTrue(exception.getCause() instanceof NoSuchBeanDefinitionException);
  }

  @Test
  public void shouldThrowBeanCreationExceptionWhenConstructorThrowsException() {
    context.register(FailingConstructorBean.class);
    assertThrows(BeanCreationException.class, () -> context.getBean(FailingConstructorBean.class));
  }

  @Test
  public void shouldThrowBeanCreationExceptionForInterface() {
    context.register(Runnable.class);
    assertThrows(BeanCreationException.class, () -> context.getBean(Runnable.class));
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
}
