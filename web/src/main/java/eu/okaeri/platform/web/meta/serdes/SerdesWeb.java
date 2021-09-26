package eu.okaeri.platform.web.meta.serdes;

import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.configs.serdes.SerdesRegistry;

public class SerdesWeb implements OkaeriSerdesPack {

    @Override
    public void register(SerdesRegistry registry) {
        registry.register(new RouteRoleTransformer());
    }
}
