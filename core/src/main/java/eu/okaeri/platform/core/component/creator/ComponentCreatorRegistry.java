package eu.okaeri.platform.core.component.creator;

import eu.okaeri.injector.Injector;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import eu.okaeri.platform.core.component.manifest.BeanSource;
import eu.okaeri.platform.core.component.type.GenericComponentResolver;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ComponentCreatorRegistry {

    private static final Logger LOGGER = Logger.getLogger(ComponentCreatorRegistry.class.getName());

    @NonNull private final Injector injector;
    private final List<ComponentResolver> componentResolvers = new ArrayList<>();
    private final Set<Class<?>> dynamicTypes = new HashSet<>();
    private final Set<Class<? extends Annotation>> dynamicAnnotations = new HashSet<>();

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

    public ComponentCreatorRegistry registerDynamicAnnotation(Class<? extends Annotation> type) {
        this.dynamicAnnotations.add(type);
        return this;
    }

    public boolean isDynamicType(Class<?> dynamicType) {
        return this.dynamicTypes.contains(dynamicType);
    }

    public boolean isDynamicAnnotation(Class<? extends Annotation> annotationType) {
        return this.dynamicTypes.contains(annotationType);
    }

    @SuppressWarnings({"Convert2MethodRef"})
    public boolean isDynamicParameter(Parameter parameter) {

        // check type
        if (this.isDynamicType(parameter.getType())) {
            return true;
        }

        // check if any of annotations is marked as dynamic
        return this.dynamicAnnotations.stream()
            .map(type -> parameter.getAnnotation(type))
            .anyMatch(Objects::nonNull);
    }

    public boolean supports(Class<?> type) {
        return this.componentResolvers.stream().anyMatch(resolver -> resolver.supports(type));
    }

    public boolean supports(Method method) {
        return this.componentResolvers.stream().anyMatch(resolver -> resolver.supports(method));
    }

    public Optional<Object> make(ComponentCreator creator, BeanManifest manifest) {

        if (manifest.getSource() == BeanSource.COMPONENT) {
            return this.pickResolver(manifest, this.componentResolvers.stream()
                    .filter(resolver -> resolver.supports(manifest.getType()))
                    .collect(Collectors.toList()))
                .map(resolver -> resolver.make(creator, manifest, this.injector));
        }

        if (manifest.getSource() == BeanSource.METHOD) {
            return this.pickResolver(manifest, this.componentResolvers.stream()
                    .filter(resolver -> resolver.supports(manifest.getMethod()))
                    .collect(Collectors.toList()))
                .map(resolver -> resolver.make(creator, manifest, this.injector));
        }

        throw new IllegalArgumentException("Unsupported manifest source: " + manifest.getSource());
    }

    protected Optional<ComponentResolver> pickResolver(@NonNull BeanManifest manifest, @NonNull List<ComponentResolver> resolvers) {

        if (resolvers.isEmpty()) {
            return Optional.empty();
        }

        if (resolvers.stream().filter(resolver -> !(resolver instanceof GenericComponentResolver)).count() > 1) {

            Object display = manifest.getSource() == BeanSource.METHOD
                ? manifest.getMethod()
                : manifest.getType();

            LOGGER.warning("Component can have a single resolver only. Multiple found (using first) for the manifest " + display + ": "
                + resolvers.stream()
                .map(Object::getClass)
                .map(Class::getSimpleName)
                .collect(Collectors.joining(", ")));
        }

        return Optional.of(resolvers.get(0));
    }
}
