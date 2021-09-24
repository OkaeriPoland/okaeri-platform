package org.example.okaeriplatformtest.command;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.annotation.ServiceDescriptor;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.meta.ServiceMeta;
import eu.okaeri.commands.registry.OkaeriCommandsRegistry;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.injector.annotation.Inject;

import java.util.List;
import java.util.stream.Collectors;

@ServiceDescriptor(label = "help", aliases = "v", description = "displays software help")
public class HelpCommand implements CommandService {

    @Inject private Commands commands;

    @Executor(pattern = "")
    public String self() {

        OkaeriCommandsRegistry registry = (OkaeriCommandsRegistry) this.commands.getRegistry();
        List<ServiceMeta> services = registry.getRegisteredCommands().stream()
                .map(CommandMeta::getService)
                .distinct()
                .collect(Collectors.toList());

        StringBuilder output = new StringBuilder();
        output.append("Example Application Help\n\n");

        int separation = 2 * services.stream()
                .map(ServiceMeta::getLabel)
                .mapToInt(String::length)
                .max()
                .orElse(8);

        for (ServiceMeta service : services) {
            output.append("\t");
            String label = service.getLabel();
            String description = service.getDescription().trim();
            output.append(String.format("%-" + separation + "s %s", label, description));
            output.append("\n");
        }

        output.append("\nStatistics\n\n");
        output.append("\tcommands:\t").append(services.size());

        return output.toString();
    }
}
