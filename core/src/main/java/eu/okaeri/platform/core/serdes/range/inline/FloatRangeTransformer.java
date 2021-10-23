package eu.okaeri.platform.core.serdes.range.inline;

import eu.okaeri.commons.range.FloatRange;
import eu.okaeri.configs.schema.GenericsPair;
import eu.okaeri.configs.serdes.BidirectionalTransformer;
import eu.okaeri.configs.serdes.SerdesContext;

public class FloatRangeTransformer extends BidirectionalTransformer<String, FloatRange> {

    @Override
    public GenericsPair<String, FloatRange> getPair() {
        return this.genericsPair(String.class, FloatRange.class);
    }

    @Override
    public FloatRange leftToRight(String data, SerdesContext serdesContext) {
        FloatRange range = FloatRange.valueOf(data);
        if (range == null) {
            throw new RuntimeException("Invalid range: " + data);
        }
        return range;
    }

    @Override
    public String rightToLeft(FloatRange range, SerdesContext serdesContext) {
        return range.getMin() + "-" + range.getMax();
    }
}
