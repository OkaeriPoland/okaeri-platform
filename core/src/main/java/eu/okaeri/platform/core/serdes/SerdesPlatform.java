package eu.okaeri.platform.core.serdes;

import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.configs.serdes.SerdesRegistry;

public class SerdesPlatform implements OkaeriSerdesPack {

    @Override
    public void register(SerdesRegistry registry) {
        registry.register(new ByteRangeTransformer());
        registry.register(new DoubleRangeTransformer());
        registry.register(new FloatRangeTransformer());
        registry.register(new IntRangeTransformer());
        registry.register(new LongRangeTransformer());
        registry.register(new ShortRangeTransformer());
    }
}
