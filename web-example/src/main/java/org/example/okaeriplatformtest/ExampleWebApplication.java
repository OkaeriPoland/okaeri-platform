package org.example.okaeriplatformtest;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.zaxxer.hikari.HikariConfig;
import eu.okaeri.commons.Strings;
import eu.okaeri.configs.json.simple.JsonSimpleConfigurer;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.persistence.PersistencePath;
import eu.okaeri.persistence.document.DocumentPersistence;
import eu.okaeri.persistence.flat.FlatPersistence;
import eu.okaeri.persistence.jdbc.H2Persistence;
import eu.okaeri.persistence.jdbc.MariaDbPersistence;
import eu.okaeri.persistence.mongo.MongoPersistence;
import eu.okaeri.persistence.redis.RedisPersistence;
import eu.okaeri.platform.core.annotation.Bean;
import eu.okaeri.platform.core.annotation.Scan;
import eu.okaeri.platform.core.plan.ExecutionPhase;
import eu.okaeri.platform.core.plan.Planned;
import eu.okaeri.platform.web.OkaeriWebApplication;
import eu.okaeri.platform.web.meta.role.SimpleAccessManager;
import eu.okaeri.platform.web.meta.role.SimpleRouteRole;
import eu.okaeri.platform.web.meta.serdes.SerdesWeb;
import io.javalin.core.security.AccessManager;
import io.javalin.core.validation.JavalinValidation;
import io.javalin.http.Context;
import io.javalin.jetty.JettyServer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.example.okaeriplatformtest.config.TestConfig;
import org.example.okaeriplatformtest.persistence.Access;
import org.example.okaeriplatformtest.persistence.AccessRepository;

import java.io.File;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;


@Scan(value = "org.example.okaeriplatformtest", deep = true)
public class ExampleWebApplication extends OkaeriWebApplication {

    // basic entrypoint inspired by Spring Boot
    public static void main(String[] args) {
        OkaeriWebApplication.run(ExampleWebApplication.class, args);
    }

    // setup platform before any beans are executed
    // still supports basic injectables
    @Planned(ExecutionPhase.SETUP)
    public void setup() {
        // needed for uuid path parameters
        JavalinValidation.register(UUID.class, UUID::fromString);
    }

    // additional javalin setup with dependency aware method
    // called before Javalin#start and javalinConfigurer
    @Planned(ExecutionPhase.PRE_STARTUP)
    public void configureJetty(JettyServer jetty, TestConfig config) {
        // custom hostname and port
        jetty.setServerHost(config.getServer().getHostname());
        jetty.setServerPort(config.getServer().getPort());
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
    @Planned(ExecutionPhase.POST_STARTUP)
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
        this.log("=".repeat(88));
        this.log("");
        this.log("Your new access token: " + access.getToken());
        this.log("");
        this.log("=".repeat(88));
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
                File storagePath = new File(config.getStorage().getUri());
                return new DocumentPersistence(new FlatPersistence(storagePath, ".yml"), YamlSnakeYamlConfigurer::new, new SerdesWeb());
            case REDIS:
                // construct redis client based on your needs, e.g. using config
                RedisURI redisUri = RedisURI.create(config.getStorage().getUri());
                RedisClient redisClient = RedisClient.create(redisUri);
                // it is HIGHLY recommended to use json configurer for the redis backend
                // other formats may not be supported in the future, json has native support
                // on the redis side thanks to cjson available in lua scripting
                return new DocumentPersistence(new RedisPersistence(basePath, redisClient), JsonSimpleConfigurer::new, new SerdesWeb());
            case MONGO:
                // construct mongo client based on your needs, e.g. using config
                MongoClientURI mongoUri = new MongoClientURI(config.getStorage().getUri());
                MongoClient mongoClient = new MongoClient(mongoUri);
                // validate if uri contains database
                if (mongoUri.getDatabase() == null) {
                    throw new IllegalArgumentException("Mongo URI needs to specify the database: " + mongoUri.getURI());
                }
                // it is REQUIRED to use json configurer for the mongo backend
                return new MongoPersistence(basePath, mongoClient, mongoUri.getDatabase(), JsonSimpleConfigurer::new, new SerdesWeb());
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
