package eu.okaeri.platform.web.meta.role;

import io.javalin.security.RouteRole;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class SimpleRouteRole implements RouteRole {

    @NonNull private final String name;

    public static final SimpleRouteRole ANYONE = new SimpleRouteRole("ANYONE");
    public static final SimpleRouteRole SUPERADMIN = new SimpleRouteRole("SUPERADMIN");
}
