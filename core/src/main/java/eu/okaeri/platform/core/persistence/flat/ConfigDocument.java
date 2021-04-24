package eu.okaeri.platform.core.persistence.flat;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.platform.core.persistence.document.Document;
import lombok.Getter;

import java.io.File;
import java.util.Map;

public class ConfigDocument extends Document {

    @Getter private OkaeriConfig config;

    public ConfigDocument(OkaeriConfig config) {
        super(null);
        this.config = config;
    }

    @Override
    public Map<String, Object> getData() {
        throw new RuntimeException("getData not available for FlatDocument");
    }

    @Override
    public void setData(Map<String, Object> data) {
        throw new RuntimeException("setData not available for FlatDocument");
    }

    @Override
    public Document put(String key, Object value) {
        this.config.set(key, value);
        return this;
    }

    public ConfigDocument save() {
        this.config.save();
        return this;
    }

    public ConfigDocument save(File file) {
        this.config.save(file);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends OkaeriConfig> T into(Class<T> configClazz) {

        if (!configClazz.isInstance(this.config)) {
            this.config = ConfigManager.copy(this.config, configClazz);
        }

        return (T) this.config;
    }

    static class EmptyConfig extends OkaeriConfig {
    }
}
