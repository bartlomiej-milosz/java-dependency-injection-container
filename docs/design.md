# Design notes

## Reading order

1. `MiniContext`: the small public facade for registration, lazy retrieval, and eager initialization.
2. `BeanDefinitionRegistry`: explicit bindings and registration validation.
3. `BeanFactory`: constructor selection, recursive resolution, cycle detection, and caching.
4. `annotations/Autowired` and `exceptions`: the runtime constructor annotation and failure categories.
5. The tests, starting with `UsageExampleTest`, then registration, constructor selection, and lifecycle behavior.

## From registration to an instance

`register(type, implementation)` checks non-null arguments, rejects an existing registration key,
checks assignability, and rejects interface or abstract implementations. Only then does it store
the definition. Registering a class alone binds that class to itself. Registration does not
invoke constructors. The definitions map is exposed as a live, read-only view, so callers
cannot bypass these checks.

`getBean(type)` requires an exact registration key. It first checks the definition, then returns
the cached object through `Class.cast` if one exists. Otherwise it marks that key as being
created and selects a public constructor. Exactly one public `@Autowired` constructor wins.
Multiple annotated constructors fail. With no annotation, the unique constructor with the
most parameters wins; a tie fails with a suggestion to use `@Autowired`. No public constructor
also fails. The factory never tries a shorter constructor when a dependency is missing.

Each constructor parameter is resolved by recursively calling `getBean` with its declared
type. This makes interface bindings explicit: the interface itself must be registered as a
key. Registering only its implementation does not register the interface automatically.
All required dependencies must be registered before creation starts, but registration order
does not matter.

Once dependencies resolve, reflection invokes the selected constructor. Only a successfully
created and checked object enters the cache. `InvocationTargetException` is unwrapped so a
constructor's original throwable is the cause of `BeanCreationException`. Missing definitions
and circular dependencies retain their own exception types.

## Cycles and failure cleanup

The factory tracks registration keys in a set for the active recursive creation path. Trying
to add an already active key throws `CircularDependencyException`. This handles self-dependency,
indirect cycles, and cycles through interface-to-implementation bindings. The message names
the key and implementation.

After adding a key, the factory encloses creation and caching in `try/finally`. The `finally`
block removes that key on success or failure. A call that detects a cycle does not remove the
ancestor's marker: its addition failed, and the ancestor owns that marker. This keeps cleanup
local and allows retries without stale cycle flags. For example, a missing dependency can be
registered after a failed retrieval, and the next retrieval can succeed.

## Decisions and tradeoffs

- A singleton belongs to a registration key within one context. Different keys can point to
  the same implementation and still receive separate instances. This is container caching,
  not a Singleton pattern enforced by a bean class. There are no aliases.
- `getBean` is lazy. `refresh` requests every registered key, creating only missing instances.
  Repeating it does not rebuild existing beans. Its iteration order is unspecified.
- Initialization is not transactional. If a constructor fails after its dependencies have
  been created, those successful dependencies stay cached. Unrelated beans may or may not
  have been initialized before the failure. Tests assert dependency ordering imposed by
  constructor injection, never `HashMap` iteration order.
- The three classes keep configuration, validation, and construction readable without new
  interfaces or a context state machine. The project uses no production dependencies.
- Null arguments cause `NullPointerException`; invalid or duplicate registrations cause
  `BeanRegistrationException`. Invalid construction causes `BeanCreationException`, missing
  keys cause `NoSuchBeanDefinitionException`, and cycles cause `CircularDependencyException`.
- The container is not thread-safe. It intentionally omits component scanning, field/setter
  injection, qualifiers, additional scopes, lifecycle callbacks, AOP/proxies, external
  configuration, and full Spring compatibility. It is an educational library.

## Design questions

- Why does constructor injection permit dependencies to be stored in final fields?
- What is the difference between an implementation class and a registration key?
- Why must cycle markers be removed in `finally`, and why is the cache updated only on success?
- Why is choosing a constructor with reflection order unsafe when parameter counts tie?
- What remains in the cache after a failed `refresh`, and why is rollback outside this design?
- What would require a different design to make this container thread-safe?
