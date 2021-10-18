package eu.okaeri.platform.core.serdes.range.section;

import eu.okaeri.commons.range.ShortRange;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;

public class ShortRangeSerializer implements ObjectSerializer<ShortRange> {

    @Override
    public boolean supports(Class<? super ShortRange> type) {
        return ShortRange.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(ShortRange range, SerializationData data) {
        data.add("min", range.getMin());
        data.add("max", range.getMax());
    }

    @Override
    public ShortRange deserialize(DeserializationData data, GenericsDeclaration genericsDeclaration) {

        short min = data.get("min", short.class);
        short max = data.get("max", short.class);

        return ShortRange.of(min, max);
    }
}
