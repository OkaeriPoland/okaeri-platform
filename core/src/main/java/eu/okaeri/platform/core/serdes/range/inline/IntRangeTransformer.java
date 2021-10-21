package eu.okaeri.platform.core.serdes.range.inline;

import eu.okaeri.commons.range.IntRange;
import eu.okaeri.configs.schema.GenericsPair;
import eu.okaeri.configs.serdes.BidirectionalTransformer;
import eu.okaeri.configs.serdes.SerdesContext;

public class IntRangeTransformer extends BidirectionalTransformer<String, IntRange> {

    @Override
    public GenericsPair<String, IntRange> getPair() {
        return this.genericsPair(String.class, IntRange.class);
    }

    @Override
    public IntRange leftToRight(String data, SerdesContext serdesContext) {
        IntRange range = IntRange.valueOf(data);
        if (range == null) {
            throw new RuntimeException("Invalid range: " + data);
        }
        return range;
    }

    @Override
    public String rightToLeft(IntRange range, SerdesContext serdesContext) {
        return range.getMin() + ":" + range.getMax();
    }
}
