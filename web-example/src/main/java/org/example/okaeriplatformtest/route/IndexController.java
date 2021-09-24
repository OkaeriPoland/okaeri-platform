package org.example.okaeriplatformtest.route;

import eu.okaeri.platform.core.annotation.Component;
import eu.okaeri.platform.web.annotation.Handler;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;

@Component
public class IndexController {

    @Handler(path = "/", type = HandlerType.GET)
    public void index(Context context) {
        context.result("Hello World!");
    }
}
