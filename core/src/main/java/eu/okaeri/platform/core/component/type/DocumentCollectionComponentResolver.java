package eu.okaeri.platform.core.component.type;

import eu.okaeri.injector.Injector;
import eu.okaeri.persistence.PersistenceCollection;
import eu.okaeri.persistence.document.DocumentPersistence;
import eu.okaeri.persistence.repository.DocumentRepository;
import eu.okaeri.persistence.repository.RepositoryDeclaration;
import eu.okaeri.persistence.repository.annotation.DocumentCollection;
import eu.okaeri.platform.core.annotation.DependsOn;
import eu.okaeri.platform.core.component.ComponentHelper;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.component.creator.ComponentResolver;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import lombok.NonNull;

import java.lang.reflect.Method;
import java.util.Arrays;

public class DocumentCollectionComponentResolver implements ComponentResolver {

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return type.getAnnotation(DocumentCollection.class) != null;
    }

    @Override
    public boolean supports(@NonNull Method method) {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object make(@NonNull ComponentCreator creator, @NonNull BeanManifest manifest, @NonNull Injector injector) {

        Class<?> manifestType = manifest.getType();
        if (!DocumentRepository.class.isAssignableFrom(manifestType)) {
            throw new IllegalArgumentException("Component of @DocumentCollection on type requires class to be a DocumentRepository: " + manifest);
        }

        long start = System.currentTimeMillis();
        DependsOn dependsOnPersistence = Arrays.stream(manifestType.getAnnotationsByType(DependsOn.class))
                .filter(on -> on.type().isAssignableFrom(DocumentPersistence.class))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("No @DependsOn for DocumentPersistence found on " + manifestType));

        DocumentPersistence persistence = (DocumentPersistence) injector.getExact(dependsOnPersistence.name(), dependsOnPersistence.type())
                .orElseThrow(() -> new IllegalArgumentException("No " + dependsOnPersistence.name() + " of " + dependsOnPersistence.type() + " found to create " + manifestType));

        PersistenceCollection collection = PersistenceCollection.of(manifestType);
        persistence.registerCollection(collection);

        Class<? extends DocumentRepository<?, ?>> repositoryType = (Class<? extends DocumentRepository<?, ?>>) manifestType;
        RepositoryDeclaration<? extends DocumentRepository<?, ?>> repositoryDeclaration = RepositoryDeclaration.of(repositoryType);
        manifest.setName(BeanManifest.nameClass(manifestType));
        DocumentRepository<?, ?> proxy = repositoryDeclaration.newProxy(persistence, collection, manifestType.getClassLoader());

        long took = System.currentTimeMillis() - start;
        creator.log(ComponentHelper.buildComponentMessage()
                .type("Added persistence repository")
                .name(manifestType.getSimpleName())
                .took(took)
                .meta("dependsOn", dependsOnPersistence.name() + "->" + dependsOnPersistence.type().getSimpleName())
                .build());
        creator.increaseStatistics("persistenceRepositories", 1);

        return proxy;
    }
}
