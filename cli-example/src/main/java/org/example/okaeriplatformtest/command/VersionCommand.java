package org.example.okaeriplatformtest.command;

import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.annotation.ServiceDescriptor;
import eu.okaeri.commands.service.CommandService;

@ServiceDescriptor(label = "version", aliases = "v", description = "displays software version")
public class VersionCommand implements CommandService {

    @Executor(pattern = "")
    public String self() {
        return "1.0";
    }
}
