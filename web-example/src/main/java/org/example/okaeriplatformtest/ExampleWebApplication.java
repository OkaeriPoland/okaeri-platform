package org.example.okaeriplatformtest;

import com.zaxxer.hikari.HikariConfig;
import eu.okaeri.configs.json.simple.JsonSimpleConfigurer;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.persistence.PersistencePath;
import eu.okaeri.persistence.document.DocumentPersistence;
import eu.okaeri.persistence.flat.FlatPersistence;
import eu.okaeri.persistence.jdbc.H2Persistence;
import eu.okaeri.persistence.jdbc.MariaDbPersistence;
import eu.okaeri.persistence.redis.RedisPersistence;
import eu.okaeri.platform.core.annotation.Bean;
import eu.okaeri.platform.core.annotation.Register;
import eu.okaeri.platform.web.OkaeriWebApplication;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.example.okaeriplatformtest.config.TestConfig;
import org.example.okaeriplatformtest.persistence.UserRepository;
import org.example.okaeriplatformtest.route.IndexController;
import org.example.okaeriplatformtest.route.UserController;

import java.io.File;


@Register(TestConfig.class)
@Register(UserRepository.class)
@Register(IndexController.class)
@Register(UserController.class)
public class ExampleWebApplication extends OkaeriWebApplication {

    // basic entrypoint inspired by Spring Boot
    public static void main(String[] args) {
        OkaeriWebApplication.run(ExampleWebApplication.class, args);
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
                return new DocumentPersistence(new FlatPersistence(new File(dataFolder, "storage"), ".yml"), YamlSnakeYamlConfigurer::new);
            case REDIS:
                // construct redis client based on your needs, e.g. using config
                RedisURI redisUri = RedisURI.create(config.getStorage().getUri());
                RedisClient redisClient = RedisClient.create(redisUri);
                // it is HIGHLY recommended to use json configurer for the redis backend
                // other formats may not be supported in the future, json has native support
                // on the redis side thanks to cjson available in lua scripting
                return new DocumentPersistence(new RedisPersistence(basePath, redisClient), JsonSimpleConfigurer::new);
            case MYSQL:
                // setup hikari based on your needs, e.g. using config
                HikariConfig mariadbHikari = new HikariConfig();
                mariadbHikari.setJdbcUrl(config.getStorage().getUri());
                // it is REQUIRED to use json configurer for the mariadb backend
                return new DocumentPersistence(new MariaDbPersistence(basePath, mariadbHikari), JsonSimpleConfigurer::new);
            case H2:
                // setup hikari based on your needs, e.g. using config
                HikariConfig jdbcHikari = new HikariConfig();
                jdbcHikari.setJdbcUrl(config.getStorage().getUri());
                //it is HIGHLY recommended to use json configurer for the jdbc backend
                return new DocumentPersistence(new H2Persistence(basePath, jdbcHikari), JsonSimpleConfigurer::new);
            default:
                throw new RuntimeException("unsupported storage backend: " + config.getStorage().getBackend());
        }
    }
}
