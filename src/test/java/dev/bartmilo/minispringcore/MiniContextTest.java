package dev.bartmilo.minispringcore;

import org.junit.jupiter.api.Test;
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
