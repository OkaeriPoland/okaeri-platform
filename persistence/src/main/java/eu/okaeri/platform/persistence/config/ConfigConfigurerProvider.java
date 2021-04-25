package eu.okaeri.platform.persistence.config;

import eu.okaeri.configs.configurer.Configurer;

public interface ConfigConfigurerProvider {
    Configurer get();
}
