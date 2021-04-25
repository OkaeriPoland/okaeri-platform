package eu.okaeri.platform.persistence;

import java.util.Collection;

public interface Persistence<T> {

    // basic group "ExamplePlugin:" -> "example_plugin:player:USER_IDENTIFIER"
    PersistencePath getBasePath();

    // check if element exists
    // important as it is advised that read returns empty objects
    // instead of throwing an exception
    boolean exists(PersistencePath collection, PersistencePath path);

    // read saved object at key, full path = "{this.path}:{path}"
    T read(PersistencePath collection, PersistencePath path);

    // read all saved objects in the path
    Collection<? extends T> readAll(PersistencePath collection);

    // write object to exact key, full path = "{this.path}:{path}"
    boolean write(PersistencePath collection, PersistencePath path, T object);
}
