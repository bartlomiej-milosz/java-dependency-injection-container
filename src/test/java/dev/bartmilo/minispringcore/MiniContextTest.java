package dev.bartmilo.minispringcore;

import org.junit.jupiter.api.Test;
import dev.bartmilo.minispringcore.resources.TestDatabase;
import dev.bartmilo.minispringcore.resources.TestDeepRepository;
import dev.bartmilo.minispringcore.resources.TestDeepService;
import dev.bartmilo.minispringcore.resources.TestRepository;
import dev.bartmilo.minispringcore.exceptions.BeanRegistrationException;
import dev.bartmilo.minispringcore.exceptions.NoSuchBeanDefinitionException;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;

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
