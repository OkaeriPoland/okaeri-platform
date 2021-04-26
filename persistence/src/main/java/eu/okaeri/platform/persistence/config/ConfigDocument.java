package eu.okaeri.platform.persistence.config;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Exclude;
import eu.okaeri.configs.exception.OkaeriException;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString(exclude = "cachedInto")
public class ConfigDocument extends OkaeriConfig {

    @Exclude @Getter @Setter private ConfigDocumentSaver saver;
    @Exclude private ConfigDocument cachedInto = this;

    @Override
    public OkaeriConfig save() throws OkaeriException {
        if (this.saver == null) throw new IllegalArgumentException("cannot #save() without saver");
        this.saver.save(this);
        return this;
    }

    @Override
    public OkaeriConfig load() throws OkaeriException {
        throw new RuntimeException("load() not available for ConfigDocument, use one of the specific methods instead");
    }

    @Override
    public OkaeriConfig load(boolean update) throws OkaeriException {
        throw new RuntimeException("load(true) not available for ConfigDocument, use one of the specific methods instead");
    }

    @Override
    public OkaeriConfig saveDefaults() throws OkaeriException {
        throw new RuntimeException("saveDefaults() not available for ConfigDocument");
    }

    @SuppressWarnings("unchecked")
    public <T extends ConfigDocument> T into(Class<T> configClazz) {

        if (!configClazz.isInstance(this.cachedInto)) {
            T newEntity = ConfigManager.transformCopy(this.cachedInto, configClazz);
            newEntity.setSaver(this.cachedInto.getSaver());
            this.cachedInto = newEntity;
        }

        return (T) this.cachedInto;
    }
}
