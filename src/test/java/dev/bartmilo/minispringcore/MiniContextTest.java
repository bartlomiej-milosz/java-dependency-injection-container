package dev.bartmilo.minispringcore;

import org.junit.jupiter.api.Test;
import dev.bartmilo.minispringcore.resources.TestRepository;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;

public class MiniContextTest {
  private MiniContext context;

  @BeforeEach
  public void init() {
    this.context = new MiniContext();
  }

  @Test
  public void testThatByDefaultContextReturnsSingletonObject() {
    context.register(TestRepository.class);
    var ref1 = context.getBean(TestRepository.class);
    var ref2 = context.getBean(TestRepository.class);
    assertNotNull(ref1);
    assertNotNull(ref2);
    assertEquals(ref1, ref2);
  }
}
