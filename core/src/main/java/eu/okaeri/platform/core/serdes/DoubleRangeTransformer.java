package eu.okaeri.platform.core.serdes;

import eu.okaeri.commons.range.DoubleRange;
import eu.okaeri.configs.schema.GenericsPair;
import eu.okaeri.configs.serdes.BidirectionalTransformer;
import eu.okaeri.configs.serdes.SerdesContext;

public class DoubleRangeTransformer extends BidirectionalTransformer<String, DoubleRange> {

    @Override
    public GenericsPair<String, DoubleRange> getPair() {
        return this.genericsPair(String.class, DoubleRange.class);
    }

    @Override
    public DoubleRange leftToRight(String data, SerdesContext serdesContext) {
        DoubleRange range = DoubleRange.valueOf(data);
        if (range == null) {
            throw new RuntimeException("Invalid range: " + data);
        }
        return range;
    }

    @Override
    public String rightToLeft(DoubleRange range, SerdesContext serdesContext) {
        return range.getMin() + " - " + range.getMax();
    }
}
