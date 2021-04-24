package eu.okaeri.platform.persistence;

import lombok.*;

import java.io.File;
import java.util.Arrays;
import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PersistencePath {

    public static final String SEPARATOR = ":";

    public static PersistencePath of(File file) {
        return new PersistencePath(file.getPath().replace(File.separator, SEPARATOR));
    }

    public static PersistencePath of(String path) {
        return new PersistencePath(path);
    }

    public PersistencePath sub(String sub) {
        return this.sub(PersistencePath.of(sub));
    }

    public PersistencePath sub(UUID sub) {
        return this.sub(String.valueOf(sub));
    }

    public PersistencePath sub(PersistencePath sub) {
        return this.append(SEPARATOR + sub.getValue());
    }

    public PersistencePath append(String element) {
        return PersistencePath.of(this.value + element);
    }

    public PersistencePath group() {
        String[] parts = this.value.split(SEPARATOR);
        return PersistencePath.of(String.join(SEPARATOR, Arrays.copyOfRange(parts, 0, parts.length - 1)));
    }

    public File toFile() {
        return new File(this.value.replace(SEPARATOR, File.separator));
    }

    private final String value;
}
