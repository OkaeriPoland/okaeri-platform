package eu.okaeri.platform.persistence.config;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.exception.OkaeriException;
import lombok.Getter;
import lombok.Setter;

public class ConfigDocument extends OkaeriConfig {

    @Getter @Setter private ConfigDocumentSaver saver;
    private ConfigDocument cachedInto = this;

    @Override
    public OkaeriConfig save() throws OkaeriException {
        if (this.saver == null) throw new IllegalArgumentException("cannot #save() without saver");
        this.saver.save(this);
        return this;
    }

    @Override
    public OkaeriConfig saveDefaults() throws OkaeriException {
        throw new RuntimeException("saveDefaults not available for ConfigPersistenceEntity");
    }

    @SuppressWarnings("unchecked")
    public <T extends ConfigDocument> T into(Class<T> configClazz) {

        if (!configClazz.isInstance(this.cachedInto)) {
            T newEntity = ConfigManager.copy(this.cachedInto, configClazz);
            newEntity.setSaver(this.cachedInto.getSaver());
            this.cachedInto = newEntity;
        }

        return (T) this.cachedInto;
    }
}
