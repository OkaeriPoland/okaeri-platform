package eu.okaeri.platform.web.persistence;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import eu.okaeri.commons.Numbers;
import eu.okaeri.persistence.PersistencePath;

import java.io.IOException;

public class PersistencePathSerializer extends StdSerializer<PersistencePath> {

    public PersistencePathSerializer() {
        this(PersistencePath.class);
    }

    protected PersistencePathSerializer(Class<PersistencePath> t) {
        super(t);
    }

    protected PersistencePathSerializer(JavaType type) {
        super(type);
    }

    protected PersistencePathSerializer(Class<?> t, boolean dummy) {
        super(t, dummy);
    }

    protected PersistencePathSerializer(StdSerializer<?> src) {
        super(src);
    }

    @Override
    public void serialize(PersistencePath persistencePath, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        String value = persistencePath.getValue();
        if (Numbers.isInteger(value)) {
            jsonGenerator.writeNumber(Long.parseLong(value));
        } else {
            jsonGenerator.writeString(value);
        }
    }
}
