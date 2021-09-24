package eu.okaeri.platform.web.persistence;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import eu.okaeri.persistence.PersistencePath;

import java.io.IOException;

public class PersistencePathDeserializer extends StdDeserializer<PersistencePath> {

    public PersistencePathDeserializer() {
        this(PersistencePath.class);
    }

    protected PersistencePathDeserializer(Class<?> vc) {
        super(vc);
    }

    protected PersistencePathDeserializer(JavaType valueType) {
        super(valueType);
    }

    protected PersistencePathDeserializer(StdDeserializer<?> src) {
        super(src);
    }

    @Override
    public PersistencePath deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        String value = jsonParser.readValueAs(String.class);
        return PersistencePath.of(value);
    }
}