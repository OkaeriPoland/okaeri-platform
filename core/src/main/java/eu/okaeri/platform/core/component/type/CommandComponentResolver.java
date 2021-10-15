package eu.okaeri.platform.core.component.type;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.annotation.Command;
import eu.okaeri.commands.meta.ServiceMeta;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.injector.Injector;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.component.ComponentHelper;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.component.creator.ComponentResolver;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import lombok.NonNull;

import java.lang.reflect.Method;

public class CommandComponentResolver implements ComponentResolver {

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return type.getAnnotation(Command.class) != null;
    }

    @Override
    public boolean supports(@NonNull Method method) {
        return false;
    }

    @Inject
    private Commands commands;

    @Override
    public Object make(@NonNull ComponentCreator creator, @NonNull BeanManifest manifest, @NonNull Injector injector) {

        if (!CommandService.class.isAssignableFrom(manifest.getType())) {
            throw new IllegalArgumentException("Component of @Command on type requires class to be a CommandService: " + manifest);
        }

        long start = System.currentTimeMillis();
        CommandService commandService = (CommandService) injector.createInstance(manifest.getType());
        ServiceMeta serviceMeta = ServiceMeta.of(null, commandService);
        this.commands.registerCommand(commandService);

        long took = System.currentTimeMillis() - start;
        creator.log(ComponentHelper.buildComponentMessage()
                .type("Added command")
                .name(commandService.getClass().getSimpleName())
                .took(took)
                .meta("label", serviceMeta.getLabel())
                .meta("aliases", serviceMeta.getAliases())
                .meta("description", serviceMeta.getDescription())
                .build());
        creator.increaseStatistics("commands", 1);

        return commandService;
    }
}
