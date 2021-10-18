package eu.okaeri.platform.core.serdes.range.section;

import eu.okaeri.commons.range.LongRange;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;

public class LongRangeSerializer implements ObjectSerializer<LongRange> {

    @Override
    public boolean supports(Class<? super LongRange> type) {
        return LongRange.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(LongRange range, SerializationData data) {
        data.add("min", range.getMin());
        data.add("max", range.getMax());
    }

    @Override
    public LongRange deserialize(DeserializationData data, GenericsDeclaration genericsDeclaration) {

        long min = data.get("min", long.class);
        long max = data.get("max", long.class);

        return LongRange.of(min, max);
    }
}
