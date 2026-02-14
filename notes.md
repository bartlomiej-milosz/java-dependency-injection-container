# Spring Framework Learning Notes

## 1. Core Concepts: The "Why" of Spring

### Inversion of Control (IoC)
**Theory:** In traditional programming, your custom code calls a library. In IoC, the "framework" calls your code. You give up control of creating and managing objects.
**Analogy:**
*   **Traditional:** You drive a car to work. You control the route, speed, and when to stop.
*   **IoC:** You call a Taxi. You just specify the destination (Dependency), and the driver (Container) takes you there.

### Dependency Injection (DI)
**Theory:** It is a specific pattern to implement IoC. Instead of an object finding its dependencies (e.g., `new EmailService()`), they are "injected" into it.
**Benefit:** Decoupling. Your code becomes easier to test because you can inject mock objects.

---

## 2. Configuration Styles (From "Manual" to "Magic")

We are learning Spring by starting with the raw mechanics and adding layers of convenience ("magic") one by one.

### Level 1: Manual Registration (The Mechanics)
*   **What:** You manually list every class you want Spring to manage.
*   **Pros:** Zero magic. You understand exactly how the container works.
*   **Cons:** Extremely verbose for large apps.

```java
var context = new AnnotationConfigApplicationContext();
context.register(EmailService.class);        // Register Component
context.register(NotificationService.class); // Register Component
context.refresh();                           // Trigger Creation & Injection
```

### Level 2: Functional Registration (The "Pure Java" Way)
*   **What:** You provide a Lambda (function) that allows YOU to instantiate the object, but Spring manages the lifecycle (Singleton, etc.).
*   **Pros:** No reflection bloat, very fast startup, explicit control.
*   **Cons:** You have to write the `new` keyword and fetch dependencies manually.
*   **Critical Rule:** Always use `context.getBean()` inside the lambda to preserve singleton behavior! Do NOT use `new` for dependencies.

```java
context.registerBean(NotificationService.class, () -> {
    // Correct: Ask Spring for the existing EmailService
    var emailService = context.getBean(EmailService.class);
    return new NotificationService(emailService);
});
```

### Level 3: Java Configuration (The Professional Standard)
*   **What:** A centralized class annotated with `@Configuration`. Methods annotated with `@Bean` return objects.
*   **Pros:** Organized, explicit, powerful (can use if-statements, environment variables).
*   **Cons:** Still requires writing a method for every bean.

```java
@Configuration
public class AppConfig {
    @Bean
    public NotificationService notificationService(EmailService emailService) {
        return new NotificationService(emailService);
    }
}
```

### Level 4: Component Scanning (The Rapid Development Way)
*   **What:** You just annotate classes with `@Component` and tell Spring where to look (`@ComponentScan`).
*   **Pros:** Fastest to write. You just create a class and it "just works".
*   **Cons:** "Magic". It's harder to see exactly what beans exist just by reading one file.

---

## 3. The Spring Container (ApplicationContext)
This is the runtime engine of Spring.
1.  **Registry:** It holds "Bean Definitions" (Recipes).
2.  **Factory:** It creates instances from those recipes.
3.  **Cache:** It holds Singleton instances (created once, reused everywhere).

When you call `context.refresh()`, the container looks at all the recipes it has collected (from Manual, Functional, or Config classes) and starts baking the beans.
