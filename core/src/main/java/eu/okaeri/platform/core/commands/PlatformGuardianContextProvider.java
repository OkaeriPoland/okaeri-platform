package eu.okaeri.platform.core.commands;

import eu.okaeri.acl.guardian.GuardianContext;
import eu.okaeri.commands.guard.context.DefaultGuardianContextProvider;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.meta.InvocationMeta;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.platform.core.OkaeriPlatform;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class PlatformGuardianContextProvider extends DefaultGuardianContextProvider {

    private final OkaeriPlatform platform;

    @Override
    @SneakyThrows
    public GuardianContext provide(@NonNull InvocationMeta invocationMeta) {

        // add bindings: method parameters
        GuardianContext guardianContext = super.provide(invocationMeta);
        Set<String> alreadyPresent = new HashSet<>(guardianContext.getData().keySet());

        // add bindings: class fields
        CommandMeta command = invocationMeta.getInvocation().getCommand();
        if (command != null) {
            CommandService implementor = command.getService().getImplementor();

            // add bindings: this
            if (alreadyPresent.add("this")) {
                guardianContext.with("this", implementor);
            }

            // add declared fields
            for (Field declaredField : implementor.getClass().getDeclaredFields()) {
                if (alreadyPresent.contains(declaredField.getName())) {
                    continue;
                }
                declaredField.setAccessible(true);
                alreadyPresent.add(declaredField.getName());
                guardianContext.with(declaredField.getName(), declaredField.get(implementor));
            }

            // add fields (accessible from superclasses)
            for (Field field : implementor.getClass().getFields()) {
                if (alreadyPresent.contains(field.getName())) {
                    continue;
                }
                field.setAccessible(true);
                alreadyPresent.add(field.getName());
                guardianContext.with(field.getName(), field.get(implementor));
            }
        }

        // add bindings: command context
        if (alreadyPresent.add("commandContext")) {
            guardianContext.with("commandContext", invocationMeta.getCommand());
        }

        // add bindings: injector
        this.platform.getInjector().stream()
            .filter(injectable -> !injectable.getName().isEmpty()) // no unnamed
            .filter(injectable -> !alreadyPresent.contains(injectable.getName())) // no overriding super
            .filter(injectable -> !alreadyPresent.contains(injectable.getName())) // no overriding class local
            .forEach(injectable -> guardianContext.with(injectable.getName(), injectable.getObject()));

        return guardianContext;
    }
}
