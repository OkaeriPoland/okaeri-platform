package eu.okaeri.platform.persistence;

import lombok.*;

import java.io.File;
import java.util.Arrays;
import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PersistencePath {

    public static final String SEPARATOR = ":";

    public static PersistencePath of(File file) {
        return new PersistencePath(file.getPath().replace(File.separator, SEPARATOR));
    }

    public static PersistencePath of(UUID uuid) {
        return new PersistencePath(String.valueOf(uuid));
    }

    public static PersistencePath of(String path) {
        return new PersistencePath(path);
    }

    public PersistencePath sub(UUID sub) {
        return this.sub(String.valueOf(sub));
    }

    public PersistencePath sub(PersistencePath sub) {
        return this.sub(sub.getValue());
    }

    public PersistencePath sub(String sub) {
        String separator = (sub.startsWith(SEPARATOR)) ? "" : SEPARATOR;
        return this.append(separator + sub);
    }

    public PersistencePath append(String element) {
        return PersistencePath.of(this.value + element);
    }

    public PersistencePath removeStart(String part) {
        return this.value.startsWith(part) ? PersistencePath.of(this.value.substring(part.length())) : this;
    }

    public PersistencePath removeStart(PersistencePath path) {
        return this.removeStart(path.getValue());
    }

    public PersistencePath group() {
        String[] parts = this.value.split(SEPARATOR);
        return PersistencePath.of(String.join(SEPARATOR, Arrays.copyOfRange(parts, 0, parts.length - 1)));
    }

    public File toFile() {
        return new File(this.value.replace(SEPARATOR, File.separator));
    }

    public String toSqlIdentifier() {
        String identifier = this.value.replace(SEPARATOR, "_");
        if (!identifier.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            throw new IllegalArgumentException("identifier '" + identifier + "' cannot be used as sql identifier");
        }
        return identifier;
    }

    private String value;
}
