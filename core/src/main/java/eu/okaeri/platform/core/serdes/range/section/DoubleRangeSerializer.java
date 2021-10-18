package eu.okaeri.platform.core.serdes.range.section;

import eu.okaeri.commons.range.DoubleRange;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;

public class DoubleRangeSerializer implements ObjectSerializer<DoubleRange> {

    @Override
    public boolean supports(Class<? super DoubleRange> type) {
        return DoubleRange.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(DoubleRange range, SerializationData data) {
        data.add("min", range.getMin());
        data.add("max", range.getMax());
    }

    @Override
    public DoubleRange deserialize(DeserializationData data, GenericsDeclaration genericsDeclaration) {

        double min = data.get("min", double.class);
        double max = data.get("max", double.class);

        return DoubleRange.of(min, max);
    }
}
