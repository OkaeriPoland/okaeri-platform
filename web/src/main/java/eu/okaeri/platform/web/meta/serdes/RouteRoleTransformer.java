package eu.okaeri.platform.web.meta.serdes;

import eu.okaeri.configs.schema.GenericsPair;
import eu.okaeri.configs.serdes.SerdesContext;
import eu.okaeri.configs.serdes.TwoSideObjectTransformer;
import eu.okaeri.platform.web.meta.role.SimpleRouteRole;

public class RouteRoleTransformer extends TwoSideObjectTransformer<String, SimpleRouteRole> {

    @Override
    public GenericsPair<String, SimpleRouteRole> getPair() {
        return this.genericsPair(String.class, SimpleRouteRole.class);
    }

    @Override
    public SimpleRouteRole leftToRight(String roleName, SerdesContext serdesContext) {
        return new SimpleRouteRole(roleName);
    }

    @Override
    public String rightToLeft(SimpleRouteRole routeRole, SerdesContext serdesContext) {
        return routeRole.getName();
    }
}
