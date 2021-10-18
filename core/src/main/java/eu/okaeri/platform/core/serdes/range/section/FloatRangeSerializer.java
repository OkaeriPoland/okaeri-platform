package eu.okaeri.platform.core.serdes.range.section;

import eu.okaeri.commons.range.FloatRange;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;

public class FloatRangeSerializer implements ObjectSerializer<FloatRange> {

    @Override
    public boolean supports(Class<? super FloatRange> type) {
        return FloatRange.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(FloatRange range, SerializationData data) {
        data.add("min", range.getMin());
        data.add("max", range.getMax());
    }

    @Override
    public FloatRange deserialize(DeserializationData data, GenericsDeclaration genericsDeclaration) {

        float min = data.get("min", float.class);
        float max = data.get("max", float.class);

        return FloatRange.of(min, max);
    }
}
