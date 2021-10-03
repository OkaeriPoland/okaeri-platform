# Okaeri Platform (WIP)

![License](https://img.shields.io/github/license/OkaeriPoland/okaeri-platform)
![Total lines](https://img.shields.io/tokei/lines/github/OkaeriPoland/okaeri-platform)
![Repo size](https://img.shields.io/github/repo-size/OkaeriPoland/okaeri-platform)
![Contributors](https://img.shields.io/github/contributors/OkaeriPoland/okaeri-platform)
[![Discord](https://img.shields.io/discord/589089838200913930)](https://discord.gg/hASN5eX)

Whole ecosystems built on top of the best okaeri packages.

- Based on dependency injection/beans/components schema:
  - subcomponent registration with Register(component class) annotation
  - automatic field injections and bean creation

- Lots of platform specific utilities:
  - register platform specific services as components
  - use commons for the most boring tasks and cleaner code

## Supported platforms
- **Minecraft**
  - [bukkit](https://github.com/OkaeriPoland/okaeri-platform/tree/master/bukkit): your platform of choice for best Spigot/Paper development experience (*beta - possible breaking changes*)
  - [bungee](https://github.com/OkaeriPoland/okaeri-platform/tree/master/bungee): your platform of choice for best BungeeCord development experience (*alpha - missing features, possible breaking changes*)
- **Standalone**
  - [web](https://github.com/OkaeriPoland/okaeri-platform/tree/master/web): best used as web side of other platforms but can be used fully standalone (*beta - basic features, possible breaking changes*)

## Documentation
We believe Okaeri Platform is best learned by examples, so feel free to explore README files of supported platforms and linked example projects. 
This approach also allows us to keep examples always up-to-date. You can also ask/search for any related questions in [discussions](https://github.com/OkaeriPoland/okaeri-platform/discussions).

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
