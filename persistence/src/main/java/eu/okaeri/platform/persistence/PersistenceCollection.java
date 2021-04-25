package eu.okaeri.platform.persistence;

import lombok.*;

@Getter
@ToString
public class PersistenceCollection extends PersistencePath {

    public static PersistenceCollection of(String path) {
        return new PersistenceCollection(path, 255);
    }

    public static PersistenceCollection of(String path, int keyLength) {
        return PersistenceCollection.of(path).keyLength(keyLength);
    }

    private PersistenceCollection(String value, int keyLength) {
        super(value);
        this.keyLength = keyLength;
    }

    private int keyLength;

    public PersistenceCollection keyLength(int keyLength) {
        if ((keyLength < 1) || (keyLength > 255)) throw new IllegalArgumentException("key length should be between 1 and 255");
        this.keyLength = keyLength;
        return this;
    }
}
