# Okaeri Platform (WIP) | Cli


- **Utilities**
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

```java
public class ExampleCliApplication extends OkaeriCliApplication {

    // basic entrypoint inspired by Spring Boot
    public static void main(String[] args) {
        OkaeriCliApplication.run(ExampleCliApplication.class, args);
    }

    // run the app - for advanced platform usage see
    // the web-example (database, components, etc.)
    @Planned(ExecutionPhase.STARTUP)
    public void run(@Inject("args") String[] args) {
        this.log(Arrays.toString(args));
    }
}
```

### Default injectables

Generally available common instances, useful in almost every component (e.g. Logger):

| Inject Name | Type | Description |
|-|-|-|
| `dataFolder` | java.io.`File` | app working directory |
| `jarFile` | java.io.`File` | app jar file |
| `logger` | java.util.logging.`Logger` | global platform logger |
| `app` | eu.okaeri.platform.web.`OkaeriWebApplication` | injectable instance of platform app |

Internals mainly used inside of the platform, but available for manual use. For example Injector can be used to create instances of classes that are using @Inject(s):

| Inject Name | Type | Description |
|-|-|-|
| `injector` | eu.okaeri.injector.`Injector` | instance of `okaeri-injector` used internally |
| `creator` | eu.okaeri.platform.core.component.creator.`ComponentCreator` | instance of ComponentCreator used for registering components |
| `placeholders` | eu.okaeri.placeholders.`Placeholders` | placeholders instance used for i18n |

Overridable defaults used in various components:

```java
// example of i18nLocaleProvider override (in app's main class)
@Planned(ExecutionPhase.SETUP)
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
