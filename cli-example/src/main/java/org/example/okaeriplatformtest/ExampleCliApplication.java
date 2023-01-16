package org.example.okaeriplatformtest;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.cli.OkaeriCliApplication;
import eu.okaeri.platform.core.plan.ExecutionPhase;
import eu.okaeri.platform.core.plan.Planned;

import java.util.Arrays;


public class ExampleCliApplication extends OkaeriCliApplication {

    // basic entrypoint inspired by Spring Boot
    public static void main(String[] args) {
        OkaeriCliApplication.run(new ExampleCliApplication(), args);
    }

    // disabling logging of platform startup
    // advised when using cli application
    // for cli commands and not as bootstrapper
    // for applications like bots, etc.
    @Planned(ExecutionPhase.PRE_SETUP)
    public void setup() {
        // this.setVerbose(false);
    }

    // run the app - for advanced platform usage see
    // the web-example (database, components, etc.)
    @Planned(ExecutionPhase.STARTUP)
    public void run(@Inject("args") String[] args) {
        this.getLogger().info(Arrays.toString(args));
    }
}
