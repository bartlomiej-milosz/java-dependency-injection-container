package dev.bartmilo.minispringcore;

import static org.junit.jupiter.api.Assertions.*;

import dev.bartmilo.minispringcore.annotations.Autowired;
import dev.bartmilo.minispringcore.exceptions.BeanCreationException;
import dev.bartmilo.minispringcore.exceptions.NoSuchBeanDefinitionException;
import dev.bartmilo.minispringcore.resources.TestPrivateConstructorBean;
import org.junit.jupiter.api.Test;

class ConstructorSelectionTest {
  @Test
  void injectsThroughSinglePublicConstructor() {
    MiniContext context = new MiniContext();
    context.register(Dependency.class);
    context.register(SingleConstructor.class);
    assertSame(
        context.getBean(Dependency.class), context.getBean(SingleConstructor.class).dependency);
  }

  @Test
  void selectsUniqueLongestConstructor() {
    MiniContext context = new MiniContext();
    context.register(Dependency.class);
    context.register(OtherDependency.class);
    context.register(LongestConstructor.class);
    assertEquals(2, context.getBean(LongestConstructor.class).selected);
  }

  @Test
  void autowiredTakesPrecedenceOverLongerConstructor() {
    MiniContext context = new MiniContext();
    context.register(Dependency.class);
    context.register(AnnotatedConstructor.class);
    assertEquals(1, context.getBean(AnnotatedConstructor.class).selected);
  }

  @Test
  void autowiredTakesPrecedenceOverTiedLongerConstructors() {
    MiniContext context = new MiniContext();
    context.register(AnnotatedWithTies.class);
    assertEquals(0, context.getBean(AnnotatedWithTies.class).selected);
  }

  @Test
  void rejectsMultipleAutowiredConstructors() {
    MiniContext context = new MiniContext();
    context.register(MultipleAnnotations.class);
    BeanCreationException error =
        assertThrows(BeanCreationException.class, () -> context.getBean(MultipleAnnotations.class));
    assertTrue(error.getMessage().contains(MultipleAnnotations.class.getName()));
    assertTrue(error.getMessage().contains("@Autowired"));
  }

  @Test
  void rejectsTiedLongestConstructorsAndSuggestsAnnotation() {
    MiniContext context = new MiniContext();
    context.register(TiedConstructors.class);
    BeanCreationException error =
        assertThrows(BeanCreationException.class, () -> context.getBean(TiedConstructors.class));
    assertTrue(error.getMessage().contains(TiedConstructors.class.getName()));
    assertTrue(error.getMessage().contains("@Autowired"));
  }

  @Test
  void rejectsBeanWithoutPublicConstructor() {
    MiniContext context = new MiniContext();
    context.register(TestPrivateConstructorBean.class);
    BeanCreationException error =
        assertThrows(
            BeanCreationException.class, () -> context.getBean(TestPrivateConstructorBean.class));
    assertTrue(error.getMessage().contains(TestPrivateConstructorBean.class.getName()));
  }

  @Test
  void missingDependencyDoesNotFallBackToShorterConstructor() {
    MiniContext context = new MiniContext();
    context.register(LongestConstructor.class);
    NoSuchBeanDefinitionException error =
        assertThrows(
            NoSuchBeanDefinitionException.class, () -> context.getBean(LongestConstructor.class));
    assertTrue(error.getMessage().contains(Dependency.class.getName()));
  }

  @Test
  void ignoresNonPublicAutowiredConstructor() {
    MiniContext context = new MiniContext();
    context.register(PrivateAnnotation.class);
    assertNotNull(context.getBean(PrivateAnnotation.class));
  }

  public static class Dependency {}

  public static class OtherDependency {}

  public static class SingleConstructor {
    final Dependency dependency;

    public SingleConstructor(Dependency dependency) {
      this.dependency = dependency;
    }
  }

  public static class LongestConstructor {
    final int selected;

    public LongestConstructor() {
      selected = 0;
    }

    public LongestConstructor(Dependency dependency) {
      selected = 1;
    }

    public LongestConstructor(Dependency dependency, OtherDependency other) {
      selected = 2;
    }
  }

  public static class AnnotatedConstructor {
    final int selected;

    @Autowired
    public AnnotatedConstructor(Dependency dependency) {
      selected = 1;
    }

    public AnnotatedConstructor(Dependency dependency, OtherDependency other) {
      selected = 2;
    }
  }

  public static class AnnotatedWithTies {
    final int selected;

    @Autowired
    public AnnotatedWithTies() {
      selected = 0;
    }

    public AnnotatedWithTies(Dependency dependency) {
      selected = 1;
    }

    public AnnotatedWithTies(OtherDependency other) {
      selected = 1;
    }
  }

  public static class MultipleAnnotations {
    @Autowired
    public MultipleAnnotations() {}

    @Autowired
    public MultipleAnnotations(Dependency dependency) {}
  }

  public static class TiedConstructors {
    public TiedConstructors() {}

    public TiedConstructors(Dependency dependency) {}

    public TiedConstructors(OtherDependency other) {}
  }

  public static class PrivateAnnotation {
    public PrivateAnnotation() {}

    @Autowired
    private PrivateAnnotation(Dependency dependency) {}
  }
}
