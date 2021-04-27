package eu.okaeri.platform.persistence.flat;

import eu.okaeri.platform.persistence.PersistenceCollection;
import eu.okaeri.platform.persistence.PersistenceEntity;
import eu.okaeri.platform.persistence.PersistencePath;
import eu.okaeri.platform.persistence.raw.RawPersistence;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FlatPersistence extends RawPersistence {

    private final Function<Path, PersistenceEntity<String>> pathToEntityMapper = path -> {
        String name = path.getFileName().toString();
        PersistencePath persistencePath = PersistencePath.of(name.substring(0, name.length() - FlatPersistence.this.getFileSuffix().length()));
        return new PersistenceEntity<>(persistencePath, this.fileToString(path.toFile()));
    };

    @Getter private final PersistencePath basePath;
    @Getter private final String fileSuffix;

    public FlatPersistence(File basePath, String fileSuffix) {
        super(PersistencePath.of(basePath), false, true);
        this.basePath = PersistencePath.of(basePath);
        this.fileSuffix = fileSuffix;
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void registerCollection(PersistenceCollection collection) {

        File collectionFile = this.getBasePath().sub(collection).toFile();
        collectionFile.mkdirs();

        super.registerCollection(collection);
    }

    @Override
    public String read(PersistenceCollection collection, PersistencePath path) {
        this.checkCollectionRegistered(collection);
        File file = this.toFullPath(collection, path).toFile();
        return this.fileToString(file);
    }

    @Override
    public Map<PersistencePath, String> readAll(PersistenceCollection collection) {
        return this.streamAll(collection).collect(Collectors.toMap(PersistenceEntity::getPath, PersistenceEntity::getValue));
    }

    @Override
    @SneakyThrows
    public Stream<PersistenceEntity<String>> streamAll(PersistenceCollection collection) {

        this.checkCollectionRegistered(collection);
        Path collectionFile = this.getBasePath().sub(collection).toPath();

        return Files.list(collectionFile).map(this.pathToEntityMapper);
    }

    @Override
    public boolean exists(PersistenceCollection collection, PersistencePath path) {
        this.checkCollectionRegistered(collection);
        return this.toFullPath(collection, path).toFile().exists();
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean write(PersistenceCollection collection, PersistencePath path, String raw) {
        this.checkCollectionRegistered(collection);
        File file = this.toFullPath(collection, path).toFile();
        File parentFile = file.getParentFile();
        if (parentFile != null) parentFile.mkdirs();
        this.writeToFile(file, raw);
        return true;
    }

    @Override
    public PersistencePath convertPath(PersistencePath path) {
        return path.append(this.fileSuffix);
    }

    @Override
    public boolean delete(PersistenceCollection collection, PersistencePath path) {
        this.checkCollectionRegistered(collection);
        return this.toFullPath(collection, path).toFile().delete();
    }

    @Override
    @SneakyThrows
    public boolean deleteAll(PersistenceCollection collection) {

        this.checkCollectionRegistered(collection);
        File collectionFile = this.getBasePath().sub(collection).toFile();

        return collectionFile.exists() && (this.delete(collectionFile) > 0);
    }

    @Override
    public long deleteAll() {

        File[] files = this.getBasePath().toFile().listFiles();
        if (files == null) {
            return 0;
        }

        return Arrays.stream(files)
                .filter(file -> this.getKnownCollections().keySet().contains(file.getName()))
                .map(this::delete)
                .filter(deleted -> deleted > 0)
                .count();
    }

    @SneakyThrows
    private long delete(File file) {
        try (Stream<Path> walk = Files.walk(file.toPath())) {
            return walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .map(File::delete)
                    .filter(Predicate.isEqual(true))
                    .count();
        }
    }

    @Data
    @AllArgsConstructor
    private class Pair<L, R> {
        private L left;
        private R right;
    }

    private String fileToString(File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            return "";
        }
    }

    @SneakyThrows
    private void writeToFile(File file, String text) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(text);
        }
    }
}
