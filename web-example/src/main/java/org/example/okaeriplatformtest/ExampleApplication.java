package org.example.okaeriplatformtest;

import eu.okaeri.platform.core.annotation.Register;
import eu.okaeri.platform.web.OkaeriWebApplication;
import org.example.okaeriplatformtest.route.IndexController;


@Register(IndexController.class)
public class ExampleApplication extends OkaeriWebApplication {

    public static void main(String[] args) {
        OkaeriWebApplication.run(ExampleApplication.class, args);
    }
}
