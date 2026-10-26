package dev.bartmilo.dicontainer;

import static org.junit.jupiter.api.Assertions.*;

import dev.bartmilo.dicontainer.exceptions.BeanRegistrationException;
import dev.bartmilo.dicontainer.exceptions.NoSuchBeanDefinitionException;
import dev.bartmilo.dicontainer.resources.TestDatabase;
import dev.bartmilo.dicontainer.resources.TestDeepRepository;
import dev.bartmilo.dicontainer.resources.TestDeepService;
import dev.bartmilo.dicontainer.resources.TestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MiniContextTest {
  private MiniContext context;

  @BeforeEach
  public void init() {
    this.context = new MiniContext();
  }

  @Test
  public void shouldThrowNoSuchBeanDefinitionExceptionWhenBeanNotRegistered() {
    assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(TestRepository.class));
  }

  @Test
  public void shouldRejectInterfaceDuringRegistration() {
    assertThrows(BeanRegistrationException.class, () -> context.register(Runnable.class));
  }

  @Test
  public void shouldResolveDeepDependencyChain() {
    context.register(TestDatabase.class);
    context.register(TestDeepRepository.class);
    context.register(TestDeepService.class);

    var service = context.getBean(TestDeepService.class);
    assertNotNull(service);
    assertEquals("db-data", service.getData());
  }
}
