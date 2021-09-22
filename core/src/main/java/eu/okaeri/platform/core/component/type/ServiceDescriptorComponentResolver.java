package eu.okaeri.platform.core.component.type;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.annotation.ServiceDescriptor;
import eu.okaeri.commands.meta.ServiceMeta;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.injector.Injector;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.component.creator.ComponentResolver;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import lombok.NonNull;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ServiceDescriptorComponentResolver implements ComponentResolver {

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return type.getAnnotation(ServiceDescriptor.class) != null;
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
            throw new IllegalArgumentException("Component of @ServiceDescriptor on type requires class to be a CommandService: " + manifest);
        }

        CommandService commandService = (CommandService) injector.createInstance(manifest.getType());
        ServiceMeta serviceMeta = ServiceMeta.of(commandService);
        this.commands.getRegistry().register(commandService);

        Map<String, String> commandMeta = new LinkedHashMap<>();
        commandMeta.put("label", serviceMeta.getLabel());

        if (!serviceMeta.getAliases().isEmpty()) {
            commandMeta.put("aliases", "[" + String.join(", ", serviceMeta.getAliases()) + "]");
        }

        if (!serviceMeta.getDescription().isEmpty()) {
            commandMeta.put("description", serviceMeta.getDescription());
        }

        String commandMetaString = commandMeta.entrySet().stream()
                .map(entry -> entry.getKey() + " = " + entry.getValue())
                .collect(Collectors.joining(", "));

        creator.log("Added command: " + commandService.getClass().getSimpleName() + " { " + commandMetaString + " }");
        creator.increaseStatistics("commands", 1);

        return commandService;
    }
}
