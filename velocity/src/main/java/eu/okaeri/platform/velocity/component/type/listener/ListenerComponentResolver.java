package eu.okaeri.platform.velocity.component.type.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import eu.okaeri.injector.Injector;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.annotation.Component;
import eu.okaeri.platform.core.annotation.Service;
import eu.okaeri.platform.core.component.ComponentHelper;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.component.creator.ComponentResolver;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import lombok.NonNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ListenerComponentResolver implements ComponentResolver {

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return ((type.getAnnotation(Service.class) != null) || (type.getAnnotation(Component.class) != null)) && Listener.class.isAssignableFrom(type);
    }

    @Override
    public boolean supports(@NonNull Method method) {
        return false;
    }

    private @Inject PluginContainer plugin;
    private @Inject ProxyServer proxy;

    @Override
    public Object make(@NonNull ComponentCreator creator, @NonNull BeanManifest manifest, @NonNull Injector injector) {

        long start = System.currentTimeMillis();
        Class<?> manifestType = manifest.getType();
        Object instance = injector.createInstance(manifestType);

        this.proxy.getEventManager().register(this.plugin, instance);

        long took = System.currentTimeMillis() - start;
        creator.log(ComponentHelper.buildComponentMessage()
            .type("Added listener")
            .name(instance.getClass().getSimpleName())
            .took(took)
            .meta("methods", Arrays.stream(instance.getClass().getDeclaredMethods())
                .filter(method -> method.getAnnotation(Subscribe.class) != null)
                .map(Method::getName)
                .collect(Collectors.toList()))
            .build());
        creator.increaseStatistics("listeners", 1);

        return instance;
    }
}
