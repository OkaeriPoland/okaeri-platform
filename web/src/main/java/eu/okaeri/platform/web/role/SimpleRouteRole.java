package eu.okaeri.platform.web.role;

import io.javalin.core.security.RouteRole;
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
