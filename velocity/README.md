# Okaeri Platform (WIP) | Bungee

- **Utilities**:
    - `PlatformScheduler`: alternative to BukkitScheduler with simplified methods and no plugin argument in methods
    - [okaeri-commons](https://github.com/OkaeriPoland/okaeri-commons) (core):
        - `CacheMap`, `Cached`, `Enums`, `Numbers`, `Strings` and others
    - [okaeri-tasker](https://github.com/OkaeriPoland/okaeri-tasker):
        - `Tasker`: fluent api for BukkitScheduler `tasker.newChain().async(() -> "hi").acceptSync((msg) -> ...).execute()` (see example)
- **Dependency Injection**:
    - [okaeri-injector](https://github.com/OkaeriPoland/okaeri-injector): "Probably the most basic and simplest DI possible with just ~13kB in size."
- **Configs**:
    - [okaeri-configs-yaml-bungee](https://github.com/OkaeriPoland/okaeri-configs/tree/master/yaml-bukkit): config library built on top of the Bungee's YamlConfiguration (see [okaeri-configs](https://github.com/OkaeriPoland/okaeri-configs))
    - [okaeri-configs-validator-okaeri](https://github.com/OkaeriPoland/okaeri-configs/tree/master/validator-okaeri): simple bean validator (see [okaeri-validator](https://github.com/OkaeriPoland/okaeri-validator))
- **Commands**:
    - [okaeri-commands-bungee](https://github.com/OkaeriPoland/okaeri-commands/tree/master/bungee): annotation based command framework (see [okaeri-commands](https://github.com/OkaeriPoland/okaeri-commands))
    - [okaeri-commands-injector](https://github.com/OkaeriPoland/okaeri-commands/tree/master/injector): okaeri-injector integration for the best ecosystem experience
- **Messages (i18n)**
    - [okaeri-i18n](https://github.com/OkaeriPoland/okaeri-i18n): translation library with compile time key checking (getters possible instead of string keys),
      built using amazing [okaeri-placeholders](https://github.com/OkaeriPoland/okaeri-placeholders) with pluralization for 143 locales, subfields, default values, boolean/floating point formatting,
      placeholder arguments (`{player.healthBar(20)}`, `{top.guild(1)}`, `{top.guild(1,kills)}`)
- **Persistence**
    - [okaeri-persistence](https://github.com/OkaeriPoland/okaeri-persistence): Object Document Mapping (ODM) library allowing to focus on data instead of the storage layer. Integrates seamlessly
      with configuration (okaeri-configs) objects and allows to store, index, and filter them with almost no additional work needed. Supports multiple backends like files, mysql, redis with just an easy switch.

### Example

See [bungee-example](https://github.com/OkaeriPoland/okaeri-platform/tree/master/bungee-example) for the repository/dependency
and the shading guide. Project in the example directory represents at least basic usage of every component.
