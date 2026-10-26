package dev.bartmilo.dicontainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

public class UsageExampleTest {
  @Test
  void createsServiceWithExplicitInterfaceBinding() {
    MiniContext context = new MiniContext();
    context.register(GreetingService.class);
    context.register(MessageRepository.class, InMemoryMessageRepository.class);

    GreetingService service = context.getBean(GreetingService.class);
    assertEquals("Hello, Java!", service.greet());
    assertSame(context.getBean(MessageRepository.class), service.repository);

    context.refresh();
    assertSame(service, context.getBean(GreetingService.class));
  }

  public interface MessageRepository {
    String message();
  }

  public static class InMemoryMessageRepository implements MessageRepository {
    public InMemoryMessageRepository() {}

    @Override
    public String message() {
      return "Hello, Java!";
    }
  }

  public static class GreetingService {
    private final MessageRepository repository;

    public GreetingService(MessageRepository repository) {
      this.repository = repository;
    }

    public String greet() {
      return repository.message();
    }
  }
}
