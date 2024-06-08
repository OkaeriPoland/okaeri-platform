# Okaeri Platform (WIP)

![License](https://img.shields.io/github/license/OkaeriPoland/okaeri-platform)
![Total lines](https://img.shields.io/tokei/lines/github/OkaeriPoland/okaeri-platform)
![Repo size](https://img.shields.io/github/repo-size/OkaeriPoland/okaeri-platform)
![Contributors](https://img.shields.io/github/contributors/OkaeriPoland/okaeri-platform)
[![Discord](https://img.shields.io/discord/589089838200913930)](https://discord.gg/hASN5eX)

Whole ecosystems built on top of the best okaeri packages.

- Based on dependency injection/beans/components schema:
  - `@Component` registration with `@Register` or `@Scan`
  - automatic field/constructor injection and bean creation

- Lots of platform specific utilities:
  - register platform specific services as components
  - use commons for the most boring tasks and cleaner code

## Real-life use cases
- [okaeri-poly](https://github.com/OkaeriPoland/okaeri-poly): Minecraft scripting plugin with the support for Groovy, JavaScript, and Python — a great addition to traditional plugins and a rapid prototyping tool
- [okaeri-minecraft](https://github.com/OkaeriPoland/okaeri-minecraft): Minecraft plugins built for various [okaeri services](https://www.okaeri.eu), based around integrating Minecraft servers with the WEB APIs

## Supported platforms
```shell
# platform
─ core
  └─ minecraft
     ├─ bukkit
     ├─ velocity
     └─ bungee
  └─ standalone
     ├─ web
     └─ cli
# extensions
─ ext-scheduler-quartz
```
- **Minecraft**
  - [bukkit](https://github.com/OkaeriPoland/okaeri-platform/tree/master/bukkit): your platform of choice for best Spigot/Paper development experience (*beta - possible breaking changes*)
  - [velocity](https://github.com/OkaeriPoland/okaeri-platform/tree/master/velocity): your platform of choice unified Velocity development experience (*alpha - missing features, possible breaking changes*)
  - [bungee](https://github.com/OkaeriPoland/okaeri-platform/tree/master/bungee): your platform of choice for best BungeeCord development experience (*beta - possible breaking changes*)
- **Standalone**
  - [web](https://github.com/OkaeriPoland/okaeri-platform/tree/master/web): best used as web side of other platforms but can be used fully standalone (*beta - basic features, possible breaking changes*)
  - [cli](https://github.com/OkaeriPoland/okaeri-platform/tree/master/cli): allows access to platform features with minimal fuss, useful for bots or other standalone tools (*beta - possible breaking changes*)

## Documentation
Okaeri Platform is a private use first project, not a public benefit project, and should be treated as such, the documentation might be non-existing or lacking.
Feel free to explore README files of supported platforms for some basic guidance and [Real-life use cases](https://github.com/OkaeriPoland/okaeri-platform?tab=readme-ov-file#real-life-use-cases) projects.
You can search for or make any okaeri-platform related questions in [discussions](https://github.com/OkaeriPoland/okaeri-platform/discussions).

## Recommendations
It is highly recommended to use `-parameters` compiler flag for better overall feature support.

### Maven (Java)
```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.8.1</version>
      <configuration>
        <compilerArgs>
          <arg>-parameters</arg>
        </compilerArgs>
      </configuration>
    </plugin>
  </plugins>
</build>
```
### Maven (Kotlin)
```xml
 <build>
  <plugins>
    <plugin>
      <groupId>org.jetbrains.kotlin</groupId>
      <artifactId>kotlin-maven-plugin</artifactId>
      <version>${kotlin.version}</version>
      <!-- ... -->
      <configuration>
        <!-- ... -->
        <args>
          <arg>-java-parameters</arg>
        </args>
      </configuration>
    </plugin>
  </plugins>
</build>
```

### Gradle (Java)
```groovy
compileJava {
    options.compilerArgs << '-parameters' 
}
```
### Gradle (Kotlin)
```groovy
compileKotlin {
    kotlinOptions {
        javaParameters = true
    }
}
```
