package eu.okaeri.platform.web.meta.serdes;

import eu.okaeri.configs.serdes.OkaeriSerdes;
import eu.okaeri.configs.serdes.SerdesRegistry;

public class SerdesWeb implements OkaeriSerdes {

    @Override
    public void register(SerdesRegistry registry) {
        registry.register(new RouteRoleTransformer());
    }
}
