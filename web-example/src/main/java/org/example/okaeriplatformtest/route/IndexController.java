package org.example.okaeriplatformtest.route;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.annotation.Component;
import eu.okaeri.platform.web.annotation.GetHandler;
import io.javalin.http.Context;
import org.example.okaeriplatformtest.ExampleWebApplication;
import org.example.okaeriplatformtest.config.TestConfig;

@Component
public class IndexController {

    // custom handler annotation for registering javalin
    // handlers in components with injector support
    @GetHandler(path = "/")
    public void index(Context context, TestConfig config, @Inject("app") ExampleWebApplication app) {
        context.result("Running " + app.getClass().getSimpleName() + " v" + config.getVersion());
    }
}
