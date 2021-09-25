package org.example.okaeriplatformtest;

import com.zaxxer.hikari.HikariConfig;
import eu.okaeri.commons.Strings;
import eu.okaeri.configs.json.simple.JsonSimpleConfigurer;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.injector.annotation.PostConstruct;
import eu.okaeri.persistence.PersistencePath;
import eu.okaeri.persistence.document.DocumentPersistence;
import eu.okaeri.persistence.flat.FlatPersistence;
import eu.okaeri.persistence.jdbc.H2Persistence;
import eu.okaeri.persistence.jdbc.MariaDbPersistence;
import eu.okaeri.persistence.redis.RedisPersistence;
import eu.okaeri.platform.core.annotation.Bean;
import eu.okaeri.platform.core.annotation.Register;
import eu.okaeri.platform.web.OkaeriWebApplication;
import eu.okaeri.platform.web.role.SimpleAccessManager;
import eu.okaeri.platform.web.role.SimpleRouteRole;
import eu.okaeri.platform.web.serdes.SerdesWeb;
import io.javalin.core.security.AccessManager;
import io.javalin.http.Context;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.example.okaeriplatformtest.config.TestConfig;
import org.example.okaeriplatformtest.persistence.Access;
import org.example.okaeriplatformtest.persistence.AccessRepository;
import org.example.okaeriplatformtest.persistence.UserRepository;
import org.example.okaeriplatformtest.route.IndexController;
import org.example.okaeriplatformtest.route.UserController;

import java.io.File;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;


@Register(TestConfig.class)
@Register(AccessRepository.class)
@Register(UserRepository.class)
@Register(IndexController.class)
@Register(UserController.class)
public class ExampleWebApplication extends OkaeriWebApplication {

    // basic entrypoint inspired by Spring Boot
    public static void main(String[] args) {
        OkaeriWebApplication.run(ExampleWebApplication.class, args);
    }

    // setup access manager for javalin
    // it will be automatically registered
    @Bean("accessManager")
    public AccessManager configureAccessManager(AccessRepository accessRepository) {
        // you can use your full custom manager implementation
        // or make use of the provided SimpleAccessManager
        return new SimpleAccessManager() {
            // custom method for resolving user roles
            @Override
            public Set<SimpleRouteRole> resolveRoles(Context context) {

                // extract bearer token from context
                // if not found, return default roles
                Optional<String> tokenOptional = this.extractBearerToken(context);
                if (tokenOptional.isEmpty()) {
                    return Set.of();
                }

                // if found, return user roles
                return accessRepository.findByToken(tokenOptional.get())
                        .map(Access::getRoles)
                        .orElse(Set.of());
            }
        };
    }

    // generate default access
    @PostConstruct
    public void generateDefaultAccess(AccessRepository accessRepository) {
        // access already exists
        if (accessRepository.count() > 0) {
            return;
        }
        // no access, generate default
        Access access = accessRepository.findOrCreateByPath(UUID.randomUUID());
        access.setToken(Strings.randomAlphanumeric(64));
        access.setRoles(Set.of(SimpleRouteRole.SUPERADMIN));
        access.save();
        // log info banner
        this.getLogger().info("=".repeat(88));
        this.getLogger().info("");
        this.getLogger().info("Your new access token: " + access.getToken());
        this.getLogger().info("");
        this.getLogger().info("=".repeat(88));
    }

    // built-in persistence utils
    // easy storage for e.g. player properties
    // see persistence/PlayerPersistence for details
    @Bean(value = "persistence", preload = true)
    public DocumentPersistence configurePersistence(@Inject("dataFolder") File dataFolder, TestConfig config) {

        // jdbc drivers may require initialization for jdbc urls to work
        try { Class.forName("org.mariadb.jdbc.Driver"); } catch (ClassNotFoundException ignored) { }
        try { Class.forName("org.h2.Driver"); } catch (ClassNotFoundException ignored) { }

        // remember that if plugin is not intended to have shared state
        // between multiple instances you must allow users to set persistence's
        // basePath manually or add some other possibility to differ keys
        // otherwise plugin usage would be limited to one instance per one redis
        PersistencePath basePath = PersistencePath.of(config.getStorage().getPrefix());

        // multiple backends are possible with an easy switch
        switch (config.getStorage().getBackend()) {
            case FLAT:
                // specify custom child dir in dataFolder or other custom location
                return new DocumentPersistence(new FlatPersistence(new File(dataFolder, "storage"), ".yml"), YamlSnakeYamlConfigurer::new, new SerdesWeb());
            case REDIS:
                // construct redis client based on your needs, e.g. using config
                RedisURI redisUri = RedisURI.create(config.getStorage().getUri());
                RedisClient redisClient = RedisClient.create(redisUri);
                // it is HIGHLY recommended to use json configurer for the redis backend
                // other formats may not be supported in the future, json has native support
                // on the redis side thanks to cjson available in lua scripting
                return new DocumentPersistence(new RedisPersistence(basePath, redisClient), JsonSimpleConfigurer::new, new SerdesWeb());
            case MYSQL:
                // setup hikari based on your needs, e.g. using config
                HikariConfig mariadbHikari = new HikariConfig();
                mariadbHikari.setJdbcUrl(config.getStorage().getUri());
                // it is REQUIRED to use json configurer for the mariadb backend
                return new DocumentPersistence(new MariaDbPersistence(basePath, mariadbHikari), JsonSimpleConfigurer::new, new SerdesWeb());
            case H2:
                // setup hikari based on your needs, e.g. using config
                HikariConfig jdbcHikari = new HikariConfig();
                jdbcHikari.setJdbcUrl(config.getStorage().getUri());
                //it is HIGHLY recommended to use json configurer for the jdbc backend
                return new DocumentPersistence(new H2Persistence(basePath, jdbcHikari), JsonSimpleConfigurer::new, new SerdesWeb());
            default:
                throw new RuntimeException("unsupported storage backend: " + config.getStorage().getBackend());
        }
    }
}
