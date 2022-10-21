package eu.okaeri.platform.web.plan;

import com.fasterxml.jackson.databind.json.JsonMapper;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.persistence.document.Document;
import eu.okaeri.platform.core.plan.ExecutionTask;
import eu.okaeri.platform.web.OkaeriWebApplication;
import eu.okaeri.platform.web.meta.role.FallbackAccessManager;
import eu.okaeri.platform.web.persistence.DocumentMixIn;
import eu.okaeri.platform.web.persistence.OkaeriConfigMixIn;
import io.javalin.config.JavalinConfig;
import io.javalin.json.JavalinJackson;
import io.javalin.security.AccessManager;

import java.util.function.Consumer;

public class JavalinSetupTask implements ExecutionTask<OkaeriWebApplication> {

    @Override
    public void execute(OkaeriWebApplication platform) {

        // basic setup
        platform.getJavalin().updateConfig(config -> {
            // simple properties
            config.showJavalinBanner = false;
            // json mapper
            JsonMapper jsonMapper = new JsonMapper();
            jsonMapper.addMixIn(Document.class, DocumentMixIn.class);
            jsonMapper.addMixIn(OkaeriConfig.class, OkaeriConfigMixIn.class);
            config.jsonMapper(new JavalinJackson(jsonMapper));
            // access manager
            config.accessManager(platform.getInjector().getExact("accessManager", AccessManager.class).orElse(new FallbackAccessManager()));
        });

        // custom setup routine
        platform.getInjector().getExact("javalinConfigurer", Consumer.class).ifPresent(configurer -> {
            @SuppressWarnings("unchecked") Consumer<JavalinConfig> javalinConfigurer = (Consumer<JavalinConfig>) configurer;
            platform.getJavalin().updateConfig(javalinConfigurer);
        });
    }
}
