package org.example.okaeriplatformtest;

import eu.okaeri.platform.cli.OkaeriCliApplication;
import eu.okaeri.platform.core.annotation.Register;
import org.example.okaeriplatformtest.command.HelpCommand;
import org.example.okaeriplatformtest.command.VersionCommand;


@Register(HelpCommand.class)
@Register(VersionCommand.class)
public class ExampleApplication extends OkaeriCliApplication {

    public static void main(String[] args) {
        OkaeriCliApplication.run(ExampleApplication.class, args);
    }
}
