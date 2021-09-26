package org.example.okaeriplatformtest.role;

import eu.okaeri.platform.web.meta.role.SimpleRouteRole;

public interface ApiRole {
    SimpleRouteRole USER_READ = new SimpleRouteRole("USER_READ");
    SimpleRouteRole USER_WRITE = new SimpleRouteRole("USER_WRITE");
}
