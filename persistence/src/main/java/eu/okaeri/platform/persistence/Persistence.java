package eu.okaeri.platform.persistence;

import java.util.Collection;

public interface Persistence<T> {

    // basic group "ExamplePlugin:" -> "example_plugin:player:USER_IDENTIFIER"
    PersistencePath getBasePath();

    // check if element exists
    // important as it is advised that read returns empty objects
    // instead of throwing an exception
    boolean exists(PersistenceCollection collection, PersistencePath path);

    // read saved object at key
    T read(PersistenceCollection collection, PersistencePath path);

    // read all saved objects in the path
    Collection<? extends T> readAll(PersistenceCollection collection);

    // write object to exact key
    boolean write(PersistenceCollection collection, PersistencePath path, T object);

    // delete single
    boolean delete(PersistenceCollection collection, PersistencePath path);

    // delete all from collection
    boolean deleteAll(PersistenceCollection collection);

    // delete all - purge all collections
    long deleteAll();
}
