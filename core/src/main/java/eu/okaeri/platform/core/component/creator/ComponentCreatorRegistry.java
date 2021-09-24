package eu.okaeri.platform.core.component.creator;

import eu.okaeri.injector.Injector;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import eu.okaeri.platform.core.component.manifest.BeanSource;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.util.*;

@RequiredArgsConstructor
public class ComponentCreatorRegistry {

    @NonNull private final Injector injector;
    private final List<ComponentResolver> componentResolvers = new ArrayList<>();
    private final Set<Class<?>> dynamicTypes = new HashSet<>();

    public ComponentCreatorRegistry register(Class<? extends ComponentResolver> componentResolverType) {
        return this.register(this.injector.createInstance(componentResolverType));
    }

    public ComponentCreatorRegistry register(ComponentResolver componentResolver) {
        this.componentResolvers.add(componentResolver);
        return this;
    }

    public ComponentCreatorRegistry registerDynamicType(Class<?> type) {
        this.dynamicTypes.add(type);
        return this;
    }

    public boolean isDynamicType(Class<?> dynamicType) {
        return this.dynamicTypes.contains(dynamicType);
    }

    public boolean supports(Class<?> type) {
        return this.componentResolvers.stream().anyMatch(resolver -> resolver.supports(type));
    }

    public boolean supports(Method method) {
        return this.componentResolvers.stream().anyMatch(resolver -> resolver.supports(method));
    }

    public Optional<Object> make(ComponentCreator creator, BeanManifest manifest) {

        if (manifest.getSource() == BeanSource.COMPONENT) {
            return this.componentResolvers.stream()
                    .filter(resolver -> resolver.supports(manifest.getType()))
                    .map(resolver -> resolver.make(creator, manifest, this.injector))
                    .findAny();
        }

        if (manifest.getSource() == BeanSource.METHOD) {
            return this.componentResolvers.stream()
                    .filter(resolver -> resolver.supports(manifest.getMethod()))
                    .map(resolver -> resolver.make(creator, manifest, this.injector))
                    .findAny();
        }

        throw new IllegalArgumentException("Unsupported manifest source: " + manifest.getSource());
    }
}
