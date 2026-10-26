# Java Dependency Injection Container

An educational dependency injection library built in plain Java, inspired by
Spring's constructor injection model. It resolves explicitly registered dependencies
through reflection and caches one instance per registered type within a context.

The project focuses on Java Core: generics, reflection, annotations, recursive
dependency resolution, and exception handling. It has **no runtime dependencies**.

## Features

- Explicit class registration and interface-to-implementation bindings.
- Constructor injection with optional `@Autowired` selection.
- Lazy creation through `getBean()` and eager initialization through `refresh()`.
- Singleton caching per registration key and context.
- Circular dependency detection and descriptive creation errors.
- JUnit tests for successful resolution, invalid configuration, and failure recovery.

## Build and run the example

**Requirements:** JDK 25 and a shell. The included Maven Wrapper pins Maven 3.10.0;
a separate Maven installation is unnecessary. The first build needs internet access
to download Maven and dependencies. On macOS/Linux, the wrapper also needs `curl`
or `wget` and archive extraction tools.

From the repository root:

```bash
./mvnw --batch-mode clean verify
```

This checks Java formatting, runs the tests, and builds
`target/java-dependency-injection-container-1.0.jar`. The JAR is a library, without
an application entry point; it is not intended for `java -jar`.

Run the executable usage example on its own:

```bash
./mvnw --batch-mode -Dtest=UsageExampleTest test
```

The example is a JUnit test: a successful run reports a passing test rather than
printing a greeting. On Windows, use `mvnw.cmd` instead of `./mvnw`.

## Usage

The following fragment uses the public types declared in
[`UsageExampleTest`](src/test/java/dev/bartmilo/dicontainer/UsageExampleTest.java).
That file contains the complete imports, constructors, and assertions.

```java
MiniContext context = new MiniContext();
context.register(GreetingService.class);
context.register(MessageRepository.class, InMemoryMessageRepository.class);

GreetingService service = context.getBean(GreetingService.class);
System.out.println(service.greet()); // Hello, Java!
```

`GreetingService` receives a `MessageRepository` through its public constructor.
The container resolves that interface to `InMemoryMessageRepository` and injects
the cached instance. The service can be registered before its dependency: all
required bindings must exist when resolution begins, but registration order does
not matter.

The entry point is `dev.bartmilo.dicontainer.MiniContext`; the constructor
annotation is `dev.bartmilo.dicontainer.annotations.Autowired`.

## Container contract

| Operation | Behavior |
| --- | --- |
| `register(Concrete.class)` | Registers a concrete class under its own type, without creating it. |
| `register(Type.class, Implementation.class)` | Binds an exact type to a compatible concrete implementation. |
| `getBean(Type.class)` | Resolves dependencies recursively and caches the successfully created instance. |
| `refresh()` | Requests every registered type, creating only instances missing from the cache. |

Bindings are exact: registering an implementation does not automatically register
its interfaces. Duplicate registrations are rejected, even for the same
implementation, without replacing the existing definition or instance.

Only public constructors participate in selection:

1. A single `@Autowired` constructor takes precedence.
2. Multiple public `@Autowired` constructors are rejected.
3. Without the annotation, the unique constructor with the most parameters wins.
4. A tie or the absence of a public constructor causes a creation error.

A missing dependency does not trigger fallback to a shorter constructor.

**Singleton scope is one instance per registration key within a context.** Two
keys bound to the same implementation receive separate instances. Different
contexts also have separate caches.

`refresh()` reuses existing instances and has no defined initialization order
beyond dependency requirements. Initialization is not transactional: if creation
fails, successfully created dependencies and other beans can remain cached.
Failed beans are not cached, and creation markers are cleared so retrieval can
be retried after correcting a missing registration.

### Errors

| Exception | Cause |
| --- | --- |
| `BeanRegistrationException` | Duplicate or incompatible binding, or an interface/abstract implementation. |
| `NoSuchBeanDefinitionException` | Missing registration for the requested type or a constructor dependency. |
| `BeanCreationException` | Constructor selection or invocation failure; an original constructor throwable is preserved as the cause. |
| `CircularDependencyException` | A registration key is encountered again on the active creation path. |
| `NullPointerException` | Null registration/retrieval arguments or a null registry passed to `BeanFactory`. |

## Architecture

| Component | Responsibility |
| --- | --- |
| [`MiniContext`](src/main/java/dev/bartmilo/dicontainer/MiniContext.java) | Public facade for registration, retrieval, and eager initialization. |
| [`BeanDefinitionRegistry`](src/main/java/dev/bartmilo/dicontainer/BeanDefinitionRegistry.java) | Validates bindings and exposes a read-only definitions view. |
| [`BeanFactory`](src/main/java/dev/bartmilo/dicontainer/BeanFactory.java) | Selects constructors, resolves dependencies, detects cycles, and caches instances. |

Resolution follows a small sequence: check the registration and cache → select a
constructor → resolve its parameters → instantiate → cache. Dependencies follow
the same sequence recursively.

See [design notes](docs/design.md) for the reading order, ownership of cycle
markers, failure cleanup, and design questions.

## Tests and code quality

JUnit tests cover registration validation, constructor selection, dependency
chains and sharing, context/key isolation, lazy/eager creation, partial failures,
constructor causes, retries, and cycles through both classes and interfaces.
The usage example is part of the test suite. Tests use real fixture classes and
observable behavior rather than mocking the container.

```bash
./mvnw --batch-mode test
./mvnw --batch-mode spotless:check
```

Spotless enforces Google Java Format with 2-space block indentation during Maven's
`validate` phase. To apply formatting locally:

```bash
./mvnw --batch-mode spotless:apply
```

GitHub Actions runs `./mvnw --batch-mode clean verify` on Temurin 25 for pushes to
`main` and pull requests targeting `main`. Test reports are written to
`target/surefire-reports`.

## Scope and limitations

This is an educational library with a deliberately small API. It is not thread-safe
and does not provide component scanning, field/setter injection, qualifiers,
additional scopes, lifecycle callbacks, AOP/proxies, or external configuration.
Its behavior is documented here; it does not aim for full Spring compatibility.

## License

Project code is licensed under the [MIT License](LICENSE).
The generated Maven Wrapper scripts retain their Apache License 2.0 notices.
