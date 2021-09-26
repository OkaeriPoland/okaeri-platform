package eu.okaeri.platform.web.meta.role;

import io.javalin.core.security.AccessManager;
import io.javalin.core.security.RouteRole;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpCode;
import lombok.NonNull;

import java.util.Set;

public class FallbackAccessManager implements AccessManager {

    @Override
    public void manage(@NonNull Handler handler, @NonNull Context context, @NonNull Set<RouteRole> permittedRoles) throws Exception {

        // general access handlers
        if (permittedRoles.contains(SimpleRouteRole.ANYONE)) {
            handler.handle(context);
            return;
        }

        // return error
        this.handleUnauthorized(context);
    }

    public void handleUnauthorized(Context context) {
        context.status(HttpCode.UNAUTHORIZED).result(HttpCode.UNAUTHORIZED.getMessage());
    }
}
