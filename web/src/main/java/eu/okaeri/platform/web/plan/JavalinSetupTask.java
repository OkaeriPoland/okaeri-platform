package eu.okaeri.platform.web.plan;

import com.fasterxml.jackson.databind.json.JsonMapper;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.injector.Injectable;
import eu.okaeri.persistence.document.Document;
import eu.okaeri.platform.core.plan.ExecutionTask;
import eu.okaeri.platform.web.OkaeriWebApplication;
import eu.okaeri.platform.web.meta.role.FallbackAccessManager;
import eu.okaeri.platform.web.persistence.DocumentMixIn;
import eu.okaeri.platform.web.persistence.OkaeriConfigMixIn;
import io.javalin.core.JavalinConfig;
import io.javalin.core.security.AccessManager;
import io.javalin.plugin.json.JavalinJackson;

import java.util.function.Consumer;

public class JavalinSetupTask implements ExecutionTask<OkaeriWebApplication> {

    @Override
    @SuppressWarnings("unchecked")
    public void execute(OkaeriWebApplication platform) {

        // basic setup
        JavalinConfig.applyUserConfig(platform.getJavalin(), platform.getJavalin()._conf, config -> {
            // simple properties
            config.showJavalinBanner = false;
            // json mapper
            JsonMapper jsonMapper = new JsonMapper();
            jsonMapper.addMixIn(Document.class, DocumentMixIn.class);
            jsonMapper.addMixIn(OkaeriConfig.class, OkaeriConfigMixIn.class);
            config.jsonMapper(new JavalinJackson(jsonMapper));
            // access manager
            AccessManager accessManager = platform.getInjector().getExact("accessManager", AccessManager.class)
                    .map(Injectable::getObject)
                    .orElse(new FallbackAccessManager());
            config.accessManager(accessManager);
        });

        // custom setup routine
        platform.getInjector().getExact("javalinConfigurer", Consumer.class).ifPresent(configurerInject -> {
            Consumer<JavalinConfig> configurer = configurerInject.getObject();
            JavalinConfig.applyUserConfig(platform.getJavalin(), platform.getJavalin()._conf, configurer);
        });
    }
}
