package eu.okaeri.platform.web.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.okaeri.configs.configurer.Configurer;
import eu.okaeri.configs.schema.ConfigDeclaration;

import java.nio.file.Path;
import java.util.logging.Logger;

public abstract class OkaeriConfigMixIn {
    @JsonIgnore private Logger logger;
    @JsonIgnore private Path bindFile;
    @JsonIgnore private Configurer configurer;
    @JsonIgnore private ConfigDeclaration declaration;
    @JsonIgnore private boolean removeOrphans;
}
