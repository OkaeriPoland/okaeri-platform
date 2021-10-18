package eu.okaeri.platform.core.serdes.range.section;

import eu.okaeri.commons.range.ByteRange;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;

public class ByteRangeSerializer implements ObjectSerializer<ByteRange> {

    @Override
    public boolean supports(Class<? super ByteRange> type) {
        return ByteRange.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(ByteRange range, SerializationData data) {
        data.add("min", range.getMin());
        data.add("max", range.getMax());
    }

    @Override
    public ByteRange deserialize(DeserializationData data, GenericsDeclaration genericsDeclaration) {

        byte min = data.get("min", byte.class);
        byte max = data.get("max", byte.class);

        return ByteRange.of(min, max);
    }
}
