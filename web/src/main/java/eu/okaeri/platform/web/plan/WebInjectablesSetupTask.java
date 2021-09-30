package eu.okaeri.platform.web.plan;

import eu.okaeri.configs.serdes.commons.SerdesCommons;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import eu.okaeri.persistence.document.ConfigurerProvider;
import eu.okaeri.placeholders.Placeholders;
import eu.okaeri.platform.core.plan.ExecutionTask;
import eu.okaeri.platform.web.OkaeriWebApplication;
import eu.okaeri.platform.web.i18n.SystemLocaleProvider;
import eu.okaeri.platform.web.meta.serdes.SerdesWeb;

import java.util.Objects;

public class WebInjectablesSetupTask implements ExecutionTask<OkaeriWebApplication> {

    @Override
    public void execute(OkaeriWebApplication platform) {

        platform.registerInjectable("dataFolder", platform.getDataFolder());
        platform.registerInjectable("jarFile", platform.getJarFile());
        platform.registerInjectable("logger", platform.getLogger());
        platform.registerInjectable("app", platform);

        platform.registerInjectable("javalin", platform.getJavalin());
        platform.registerInjectable("jetty", Objects.requireNonNull(platform.getJavalin().jettyServer()));

        platform.registerInjectable("placeholders", Placeholders.create(true));

        platform.registerInjectable("defaultConfigurerProvider", (ConfigurerProvider) YamlSnakeYamlConfigurer::new);
        platform.registerInjectable("defaultConfigurerSerdes", new Class[]{SerdesCommons.class, SerdesWeb.class});
        platform.registerInjectable("i18nLocaleProvider", new SystemLocaleProvider());
    }
}
