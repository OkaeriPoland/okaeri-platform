package org.example.okaeriplatformtest;


import com.velocitypowered.api.command.CommandSource;
import eu.okaeri.commands.annotation.Command;
import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.commands.tasker.annotation.Chain;
import eu.okaeri.commands.velocity.annotation.Permission;
import eu.okaeri.commands.velocity.response.VelocityResponse;
import eu.okaeri.configs.exception.OkaeriException;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.tasker.core.chain.TaskerChain;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import static eu.okaeri.tasker.core.TaskerDsl.accept;
import static eu.okaeri.tasker.core.TaskerDsl.supply;

@Command(label = "opt")
public class ExampleCommand implements CommandService {

    private @Inject ExampleConfig config;
    private @Inject Logger logger;

    @Executor
    @Permission("okaeri.platform.reload")
    public VelocityResponse reload(CommandSource sender) {

        try {
            this.config.load();
        } catch (OkaeriException exception) {
            this.logger.error("Failed to reload the configuration", exception);
            return VelocityResponse.err("Error during reload! More information in the console.");
        }

        return VelocityResponse.ok("The configuration reloaded.");
    }

    @Executor
    @Permission("okaeri.platform.hello")
    public Component hello(CommandSource sender) {
        return Component.text("Hello from " + Thread.currentThread().getName() + "!");
    }

    @Executor
    @Permission("okaeri.platform.tasker")
    public TaskerChain<?> tasker(CommandSource sender, @Chain TaskerChain<?> chain) {
        return chain
            .$(supply(() -> Component.text("Hello from " + Thread.currentThread().getName() + "!")))
            .$(accept(sender::sendMessage));
    }
}
