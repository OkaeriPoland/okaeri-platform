package eu.okaeri.platform.core.serdes.range.inline;

import eu.okaeri.commons.range.LongRange;
import eu.okaeri.configs.schema.GenericsPair;
import eu.okaeri.configs.serdes.BidirectionalTransformer;
import eu.okaeri.configs.serdes.SerdesContext;

public class LongRangeTransformer extends BidirectionalTransformer<String, LongRange> {

    @Override
    public GenericsPair<String, LongRange> getPair() {
        return this.genericsPair(String.class, LongRange.class);
    }

    @Override
    public LongRange leftToRight(String data, SerdesContext serdesContext) {
        LongRange range = LongRange.valueOf(data);
        if (range == null) {
            throw new RuntimeException("Invalid range: " + data);
        }
        return range;
    }

    @Override
    public String rightToLeft(LongRange range, SerdesContext serdesContext) {
        return range.getMin() + " - " + range.getMax();
    }
}
