package eu.okaeri.platform.core.serdes;

import eu.okaeri.commons.range.ShortRange;
import eu.okaeri.configs.schema.GenericsPair;
import eu.okaeri.configs.serdes.BidirectionalTransformer;
import eu.okaeri.configs.serdes.SerdesContext;

public class ShortRangeTransformer extends BidirectionalTransformer<String, ShortRange> {

    @Override
    public GenericsPair<String, ShortRange> getPair() {
        return this.genericsPair(String.class, ShortRange.class);
    }

    @Override
    public ShortRange leftToRight(String data, SerdesContext serdesContext) {
        ShortRange range = ShortRange.valueOf(data);
        if (range == null) {
            throw new RuntimeException("Invalid range: " + data);
        }
        return range;
    }

    @Override
    public String rightToLeft(ShortRange range, SerdesContext serdesContext) {
        return range.getMin() + " - " + range.getMax();
    }
}
