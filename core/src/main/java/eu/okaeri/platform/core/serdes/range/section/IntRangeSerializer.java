package eu.okaeri.platform.core.serdes.range.section;

import eu.okaeri.commons.range.IntRange;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;

public class IntRangeSerializer implements ObjectSerializer<IntRange> {

    @Override
    public boolean supports(Class<? super IntRange> type) {
        return IntRange.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(IntRange range, SerializationData data) {
        data.add("min", range.getMin());
        data.add("max", range.getMax());
    }

    @Override
    public IntRange deserialize(DeserializationData data, GenericsDeclaration genericsDeclaration) {

        int min = data.get("min", int.class);
        int max = data.get("max", int.class);

        return IntRange.of(min, max);
    }
}
