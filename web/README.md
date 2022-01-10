# Okaeri Platform (WIP) | Web


- **Web & Utilities**
    - [tipsy/javalin](https://github.com/tipsy/javalin): A simple and modern Java and Kotlin web framework, integrated with platform using handler annotations
    - [FasterXML/jackson](https://github.com/FasterXML/jackson): common JSON parser library, with integrated mixins for [okaeri-persistence](https://github.com/OkaeriPoland/okaeri-persistence)
    - [okaeri-commons](https://github.com/OkaeriPoland/okaeri-commons): caching utilities like `CacheMap`, `Cached` / general utilities for `Enums`, `Numbers`, `Strings` and others
- **Dependency Injection**:
    - [okaeri-injector](https://github.com/OkaeriPoland/okaeri-injector): "Probably the most basic and simplest DI possible with just ~13kB in size."
- **Configs**:
    - [okaeri-configs-yaml-snakeyaml](https://github.com/OkaeriPoland/okaeri-configs/tree/master/yaml-bukkit): config library built on top of SnakeYAML (see [okaeri-configs](https://github.com/OkaeriPoland/okaeri-configs))
    - [okaeri-configs-validator-okaeri](https://github.com/OkaeriPoland/okaeri-configs/tree/master/validator-okaeri): simple bean validator (see [okaeri-validator](https://github.com/OkaeriPoland/okaeri-validator))
- **Commands**:
    - [okaeri-commands-core](https://github.com/OkaeriPoland/okaeri-commands/tree/master/bukkit): annotation based command framework (see [okaeri-commands](https://github.com/OkaeriPoland/okaeri-commands))
    - [okaeri-commands-injector](https://github.com/OkaeriPoland/okaeri-commands/tree/master/injector): okaeri-injector integration for the best ecosystem experience
- **Messages (i18n)**
    - [okaeri-i18n](https://github.com/OkaeriPoland/okaeri-i18n): translation library with compile time key checking (getters possible instead of string keys),
      built using amazing [okaeri-placeholders](https://github.com/OkaeriPoland/okaeri-placeholders) with pluralization for 143 locales, subfields, default values, boolean/floating point formatting,
      placeholder arguments (`{player.healthBar(20)}`, `{top.guild(1)}`, `{top.guild(1,kills)}`)
- **Persistence**
    - [okaeri-persistence](https://github.com/OkaeriPoland/okaeri-persistence): Object Document Mapping (ODM) library allowing to focus on data instead of the storage layer. Integrates seamlessly
      with configuration (okaeri-configs) objects and allows to store, index, and filter them with almost no additional work needed. Supports multiple backends like files, mysql, redis with just an easy switch.

### Example

See [web-example](https://github.com/OkaeriPoland/okaeri-platform/tree/master/web-example) for the repository/dependency.
Note the code below does not represent full source code of the example. Project in the example directory represents at least basic usage of every component.

```java
@Component
@Controller(path = "/user")
public class UserController {

    @Inject
    private UserRepository userRepository;

    @GetHandler(path = "/{id}", permittedRoles = {"USER_READ"})
    public void userGet(Context context, @PathParam("id") UUID id) {

        Optional<User> dataOptional = this.userRepository.findByPath(id);
        if (dataOptional.isEmpty()) {
            context.status(HttpCode.NOT_FOUND).json(Map.of("error", HttpCode.NOT_FOUND));
            return;
        }

        context.json(dataOptional.get());
    }

    @DeleteHandler(path = "/{id}", permittedRoles = {"USER_WRITE"})
    public void userDelete(Context context, @PathParam("id") UUID id) {
        context.json(Map.of("status", this.userRepository.deleteByPath(id)));
    }

    @GetHandler(permittedRoles = {"USER_READ"})
    public void userList(Context context) {
        context.json(this.userRepository.findAll());
    }

    @PutHandler(permittedRoles = {"USER_WRITE"})
    public void userPut(Context context) {
        context.json(this.userRepository.save(context.bodyAsClass(User.class)));
    }
}
```

### Default injectables

Generally available common instances, useful in almost every component (e.g. Logger):

| Inject Name | Type | Description |
|-|-|-|
| `dataFolder` | java.io.`File` | app working directory |
| `jarFile` | java.io.`File` | app jar file |
| `logger` | org.slf4j.`Logger` | global platform logger |
| `app` | eu.okaeri.platform.web.`OkaeriWebApplication` | injectable instance of platform app |
| `javalin` | io.javalin.`Javalin` | javalin instance |

Internals mainly used inside of the platform, but available for manual use. For example Injector can be used to create instances of classes that are using @Inject(s):

| Inject Name | Type | Description |
|-|-|-|
| `injector` | eu.okaeri.injector.`Injector` | instance of `okaeri-injector` used internally |
| `creator` | eu.okaeri.platform.core.component.creator.`ComponentCreator` | instance of ComponentCreator used for registering components |
| `placeholders` | eu.okaeri.placeholders.`Placeholders` | placeholders instance used for i18n |

Overridable defaults used in various components:

```java
// example of i18nLocaleProvider override (in app's main class)
@Planned(ExecutionPhase.PRE_SETUP)
public void setup(Injector injector) {
    injector.registerExclusive("i18nLocaleProvider", new MyLocaleProvider());
}
```

| Inject Name | Type | Description |
|-|-|-|
| `defaultConfigurerProvider` | eu.okaeri.persistence.document.`ConfigurerProvider` | default configuration provider for @Configuration, @Messages and others |
| `defaultConfigurerSerdes` | Class[] | list of default OkaeriSerdesPack(s) classes to be used with `defaultConfigurerProvider` |
| `defaultPlaceholdersFactory` | eu.okaeri.platform.core.placeholder.`DefaultPlaceholdersFactory` | default placeholders provider to be used e.g. in i18n |
| `i18nLocaleProvider` | eu.okaeri.i18n.provider.`LocaleProvider` | platform's locale provider for i18n components e.g. commands |

### Performance

The example web app with flat persistence loads in about 1000 ms on the AMD Ryzen 3600 system. 
Runtime overhead for most of the components is negligible. Resolving/calling of method handlers is highly cached.
