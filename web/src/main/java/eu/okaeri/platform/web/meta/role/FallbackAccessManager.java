package eu.okaeri.platform.web.meta.role;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import io.javalin.security.AccessManager;
import io.javalin.security.RouteRole;
import lombok.NonNull;

import java.util.Set;

public class FallbackAccessManager implements AccessManager {

    @Override
    public void manage(@NonNull Handler handler, @NonNull Context context, @NonNull Set<? extends RouteRole> permittedRoles) throws Exception {

        // general access handlers
        if (permittedRoles.contains(SimpleRouteRole.ANYONE)) {
            handler.handle(context);
            return;
        }

        // return error
        this.handleUnauthorized(context);
    }

    public void handleUnauthorized(Context context) {
        context.status(HttpStatus.UNAUTHORIZED).result(HttpStatus.UNAUTHORIZED.getMessage());
    }
}
