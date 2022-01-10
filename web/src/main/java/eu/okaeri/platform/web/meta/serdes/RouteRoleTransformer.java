package eu.okaeri.platform.web.meta.serdes;

import eu.okaeri.configs.schema.GenericsPair;
import eu.okaeri.configs.serdes.BidirectionalTransformer;
import eu.okaeri.configs.serdes.SerdesContext;
import eu.okaeri.platform.web.meta.role.SimpleRouteRole;
import lombok.NonNull;

public class RouteRoleTransformer extends BidirectionalTransformer<String, SimpleRouteRole> {

    @Override
    public GenericsPair<String, SimpleRouteRole> getPair() {
        return this.genericsPair(String.class, SimpleRouteRole.class);
    }

    @Override
    public SimpleRouteRole leftToRight(@NonNull String roleName, @NonNull SerdesContext serdesContext) {
        return new SimpleRouteRole(roleName);
    }

    @Override
    public String rightToLeft(@NonNull SimpleRouteRole routeRole, @NonNull SerdesContext serdesContext) {
        return routeRole.getName();
    }
}
