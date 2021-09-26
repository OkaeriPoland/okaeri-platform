package eu.okaeri.platform.web.meta.role;

import io.javalin.core.security.AccessManager;
import io.javalin.core.security.RouteRole;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpCode;
import lombok.NonNull;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public abstract class SimpleAccessManager implements AccessManager {

    @Override
    public void manage(@NonNull Handler handler, @NonNull Context context, @NonNull Set<RouteRole> permittedRoles) throws Exception {

        // general access handlers
        if (permittedRoles.contains(SimpleRouteRole.ANYONE)) {
            handler.handle(context);
            return;
        }

        // fetch context roles
        Set<SimpleRouteRole> routeRoles = this.resolveRoles(context);

        // superadmin role, skip all
        if (routeRoles.contains(SimpleRouteRole.SUPERADMIN)) {
            handler.handle(context);
            return;
        }

        // check standard handler roles
        for (SimpleRouteRole role : routeRoles) {
            // check if any of user roles is allowed
            if (permittedRoles.contains(role)) {
                handler.handle(context);
                return;
            }
        }

        // return error
        this.handleUnauthorized(context);
    }

    public void handleUnauthorized(Context context) {
        context.status(HttpCode.UNAUTHORIZED).result(HttpCode.UNAUTHORIZED.getMessage());
    }

    public Optional<String> extractBearerToken(Context context) {

        String authorizationHeader = context.header("Authorization");
        if (authorizationHeader == null) {
            return Optional.empty();
        }

        String[] parts = authorizationHeader.split(" ", 2);
        if (parts.length != 2) {
            return Optional.empty();
        }

        String type = parts[0];
        String token = parts[1];

        if (!type.toLowerCase(Locale.ROOT).equals("bearer")) {
            return Optional.empty();
        }

        return Optional.of(token);
    }

    public abstract Set<SimpleRouteRole> resolveRoles(Context context);
}
