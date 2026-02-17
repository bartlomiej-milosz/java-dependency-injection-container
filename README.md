# Mini Spring Core

A lightweight, educational Dependency Injection (DI) container built from scratch in Java. This project demonstrates the core mechanics behind the Spring Framework, demystifying how Inversion of Control (IoC), Bean Instantiation, and Dependency Injection work under the hood.

> "To understand the framework, you must build the framework."

## Features

-   **Dependency Injection (DI)**: Automatically resolves and injects dependencies via constructors.
-   **Annotation Support (`@Autowired`)**: Detects and prioritizes constructors annotated with `@Autowired`.
-   **Singleton Scope**: Manages beans as singletons, ensuring only one instance per context.
-   **Circular Dependency Detection**: Detects infinite loops in dependency graphs (A -> B -> A) and throws a descriptive exception.
-   **Interface Binding**: Automatically binds interfaces to their concrete implementations.
-   **Modular Architecture**: Follows SRP with a clear separation between `BeanDefinitionRegistry`, `BeanFactory`, and the `MiniContext` facade.

## Architecture

The project mimics the internal structure of the Spring Framework:

1.  **`MiniContext` (Facade)**: The public entry point. It coordinates the Registry and Factory.
2.  **`BeanDefinitionRegistry` (The Menu)**: Stores bean definitions (Class -> Implementation).
3.  **`BeanFactory` (The Kitchen)**: Handles the complex logic of instantiation, recursion, and singleton caching.

## Usage

### 1. Simple Registration
```java
MiniContext context = new MiniContext();
context.register(UserRepository.class); // Register dependencies first
context.register(UserService.class);    // Register dependents
context.refresh();                      // Trigger instantiation

UserService service = context.getBean(UserService.class);
```

### 2. Interface Binding
```java
// Register the interface AND the implementation
context.register(PaymentService.class, StripePaymentService.class);

// Request the interface, get the implementation
PaymentService service = context.getBean(PaymentService.class);
```

### 3. Using `@Autowired`
For beans with multiple constructors, use `@Autowired` to tell the container which one to use:

```java
public class OrderService {
    
    // Default constructor (ignored)
    public OrderService() {}

    @Autowired
    public OrderService(OrderRepository repo) {
        this.repo = repo;
    }
}
```

## Testing
The project includes a comprehensive JUnit 5 test suite covering:
-   Basic dependency injection.
-   Deep dependency chains (Service -> Repo -> Database).
-   Circular dependency scenarios.
-   Registration of interfaces vs implementations.
-   `@Autowired` vs "Greediest Constructor" selection strategies.

Run tests with Maven:
```bash
mvn test
```

## Key Learnings
This project explores advanced Java concepts:
-   **Java Reflection API**: Used for runtime class analysis, constructor discovery, and object instantiation.
-   **Recursion**: Used to resolve dependency trees of arbitrary depth.
-   **Exception Handling**: Custom hierarchy (`BeansException`, `NoSuchBeanDefinitionException`, `BeanCreationException`).
-   **Design Patterns**: Facade, Singleton, Registry, Factory.

## License
MIT License - feel free to use this for educational purposes!
