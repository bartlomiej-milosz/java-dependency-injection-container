# Mini Spring Core

A small educational dependency injection container written in Java. It explores Java Core,
reflection, constructor injection, recursion, failure handling, and behavior testing.
It is a learning project inspired by Spring's core ideas, with a deliberately limited API.

## Requirements and build

- JDK 25, available through `JAVA_HOME` or `PATH`.
- Maven Wrapper is included and pins Maven 3.10.0; a separate Maven installation is unnecessary.
- The first wrapper run needs internet access to download Maven and build/test dependencies.
  On Unix-like systems, downloading Maven requires `curl` or `wget` and archive extraction tools.

Run from the repository root:

```bash
./mvnw --version
./mvnw --batch-mode clean verify
./mvnw --batch-mode test
./mvnw --batch-mode -Dtest=UsageExampleTest test
```

The last command runs the executable example below. On Windows, use `mvnw.cmd` instead of
`./mvnw`. The project has no production dependencies; JUnit 5 is a test dependency.
GitHub Actions is configured to run `clean verify` on Temurin 25 for pushes to `master`
and pull requests targeting `master`.

## Complete usage example

This JUnit example is included as
[`UsageExampleTest`](src/test/java/dev/bartmilo/minispringcore/UsageExampleTest.java).
The package provides access to `MiniContext`; the remaining imports and declarations are
included below. Constructor injection works even though the service is registered first.

```java
package dev.bartmilo.minispringcore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

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
```

## Container contract

Registration is explicit. `register(Concrete.class)` binds a class to itself;
`register(Interface.class, Implementation.class)` binds an exact type to a compatible concrete
implementation. The generic signature checks compatibility at compile time, and runtime
validation remains in place. Registering an implementation alone does not register its
interfaces. Register all needed types before requesting beans; their registration order
does not matter.

Only public constructors participate in selection:

1. Exactly one constructor annotated with
   `dev.bartmilo.minispringcore.annotations.Autowired` is selected, even if other constructors
   are longer or tied.
2. Multiple public `@Autowired` constructors cause `BeanCreationException`.
3. Without `@Autowired`, the unique public constructor with the most parameters is selected.
4. A tie between the longest constructors causes `BeanCreationException` suggesting `@Autowired`.
5. No public constructor causes `BeanCreationException`.

The selected constructor's parameter types are resolved recursively. A missing dependency
never causes fallback to a shorter constructor.

`getBean(type)` creates a bean lazily and reuses its instance on later calls. `refresh()`
eagerly requests every registered type. It does not rebuild existing instances, and repeated
calls reuse the cache. Initialization order is unspecified. If initialization fails, previously
successful beans, including dependencies created before a constructor failed, can remain cached.
There is no transactional rollback.

Singleton scope means **one instance per registration key within a context**. Two different
registered types pointing to the same implementation receive separate instances. Separate
contexts also have separate instances. The bean class does not need to implement the Singleton
pattern; there is no alias or cross-key sharing mechanism.

## Errors

- `BeanRegistrationException`: incompatible binding, interface/abstract implementation, or
  duplicate registration, including repetition of the same implementation. Rejection leaves
  the previous definition and cached instance intact.
- `NoSuchBeanDefinitionException`: an exact requested type or constructor dependency is missing.
- `BeanCreationException`: ambiguous/missing public constructor, reflection failure, or a
  constructor that throws. The original constructor throwable is preserved as the cause.
- `CircularDependencyException`: an active registration key is requested again during creation,
  including self-dependency and cycles through interface bindings.
- `NullPointerException`: null arguments to registration/retrieval, or a null registry supplied
  to `BeanFactory`.

Failed beans are not cached, and active creation markers are removed in `finally`. Retrieval
can be retried, for example after registering a previously missing dependency. The registry's
public definitions view is read-only.

## Architecture and tests

- `MiniContext` exposes registration, retrieval, and eager initialization.
- `BeanDefinitionRegistry` validates and stores type-to-implementation definitions.
- `BeanFactory` selects constructors, recursively resolves parameters, invokes constructors,
  detects cycles, and caches successful instances by registration key.

Flow: register definitions → request a type → reuse cache or select a constructor → resolve
its dependencies → construct → cache. See [design notes](docs/design.md) for reading order,
tradeoffs, failure cleanup, interview prompts, and the AI-assisted preparation disclosure.

JUnit tests cover valid and invalid registrations, duplicates and nulls, all constructor
selection rules, missing dependencies without fallback, dependency chains and sharing,
context/key isolation, lazy/eager creation, repeated refresh, registration order, partial
initialization failure, cycles, constructor causes, retry cleanup, and the complete example.
Tests use small real classes and observable constructor counters, without mocking or assumptions
about `HashMap` iteration order. Surefire reports are generated under `target/surefire-reports`.

## Limitations

The container is not thread-safe and does not provide component scanning, field/setter injection,
qualifiers, additional scopes, lifecycle callbacks, AOP/proxies, or XML/YAML configuration.
It is not fully compatible with Spring and is intended for studying a small constructor
injection container.

## License

Project code is licensed under the [MIT License](LICENSE).
The generated Maven Wrapper scripts retain their Apache License 2.0 notices.
