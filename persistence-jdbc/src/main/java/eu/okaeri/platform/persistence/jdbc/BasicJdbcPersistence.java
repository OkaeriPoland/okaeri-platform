package eu.okaeri.platform.persistence.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.platform.persistence.PersistenceCollection;
import eu.okaeri.platform.persistence.PersistencePath;
import eu.okaeri.platform.persistence.config.ConfigConfigurerProvider;
import eu.okaeri.platform.persistence.config.ConfigDocument;
import eu.okaeri.platform.persistence.config.ConfigPersistence;
import lombok.Getter;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class BasicJdbcPersistence extends ConfigPersistence {

    @Getter private HikariDataSource dataSource;

    public BasicJdbcPersistence(PersistencePath basePath, HikariConfig hikariConfig, ConfigConfigurerProvider configurerProvider, OkaeriSerdesPack... serdesPacks) {
        super(basePath, configurerProvider, serdesPacks);
        this.connect(hikariConfig);
    }

    @SneakyThrows
    private void connect(HikariConfig hikariConfig) {
        do {
            try {
                this.dataSource = new HikariDataSource(hikariConfig);
            } catch (Exception exception) {
                if (exception.getCause() != null) {
                    System.out.println("Cannot connect with database (waiting 30s): " + exception.getMessage() + " caused by " + exception.getCause().getMessage());
                } else {
                    System.out.println("Cannot connect with database (waiting 30s): " + exception.getMessage());
                }
                Thread.sleep(30_000);
            }
        } while (this.dataSource == null);
    }

    @Override
    public void registerCollection(PersistenceCollection collection) {

        String sql = "create table if not exists `" + this.table(collection) + "` (" +
                "`key` varchar(" + collection.getKeyLength() + ") unique primary key not null," +
                "`value` json not null)" +
                "engine = InnoDB character set = utf8mb4;";

        try (Connection connection = this.dataSource.getConnection()) {
            connection.createStatement().execute(sql);
        } catch (SQLException exception) {
            throw new RuntimeException("cannot register collection", exception);
        }

        super.registerCollection(collection);
    }

    @Override
    public ConfigDocument read(ConfigDocument document, PersistenceCollection collection, PersistencePath path) {

        String sql = "select `value` from `" + this.table(collection) + "` where `key` = ? limit 1";

        try (Connection connection = this.dataSource.getConnection()) {
            PreparedStatement prepared = connection.prepareStatement(sql);
            prepared.setString(1, path.getValue());
            ResultSet resultSet = prepared.executeQuery();
            if (resultSet.next()) {
                document.load(resultSet.getString("value"));
            }
        } catch (SQLException exception) {
            throw new RuntimeException("cannot read " + path + " from " + collection, exception);
        }

        return document;
    }

    @Override
    public Collection<ConfigDocument> readAll(PersistenceCollection collection) {

        String sql = "select `key`, `value` from `" + this.table(collection) + "`";
        List<ConfigDocument> list = new ArrayList<>();

        try (Connection connection = this.dataSource.getConnection()) {

            PreparedStatement prepared = connection.prepareStatement(sql);
            ResultSet resultSet = prepared.executeQuery();

            while (resultSet.next()) {
                String key = resultSet.getString("key");
                String value = resultSet.getString("value");
                PersistencePath path = PersistencePath.of(key);
                list.add((ConfigDocument) this.createDocument(collection, path).load(value));
            }
        } catch (SQLException exception) {
            throw new RuntimeException("cannot read all from " + collection, exception);
        }

        return list;
    }

    @Override
    public boolean exists(PersistenceCollection collection, PersistencePath path) {

        String sql = "select 1 from `" + this.table(collection) + "` where `key` = ? limit 1";

        try (Connection connection = this.dataSource.getConnection()) {
            PreparedStatement prepared = connection.prepareStatement(sql);
            prepared.setString(1, path.getValue());
            ResultSet resultSet = prepared.executeQuery();
            return resultSet.next();
        } catch (SQLException exception) {
            throw new RuntimeException("cannot check if " + path + " exists in " + collection, exception);
        }
    }

    @Override
    public boolean write(PersistenceCollection collection, PersistencePath path, ConfigDocument document) {

        String sql = "insert into `" + this.table(collection) + "` (`key`, `value`) values (?, ?) on duplicate key update `value` = ?";

        try (Connection connection = this.dataSource.getConnection()) {
            PreparedStatement prepared = connection.prepareStatement(sql);
            String saved = document.saveToString();
            prepared.setString(1, path.getValue());
            prepared.setString(2, saved);
            prepared.setString(3, saved);
            return prepared.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new RuntimeException("cannot write " + path + " to " + collection, exception);
        }
    }

    @Override
    public boolean delete(PersistenceCollection collection, PersistencePath path) {

        String sql = "delete from `" + this.table(collection) + "` where `key` = ?";

        try (Connection connection = this.dataSource.getConnection()) {
            PreparedStatement prepared = connection.prepareStatement(sql);
            prepared.setString(1, path.getValue());
            return prepared.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new RuntimeException("cannot delete " + path + " from " + collection, exception);
        }
    }

    @Override
    public boolean deleteAll(PersistenceCollection collection) {

        String sql = "truncate table `" + this.table(collection) + "`";

        try (Connection connection = this.dataSource.getConnection()) {
            PreparedStatement prepared = connection.prepareStatement(sql);
            return prepared.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new RuntimeException("cannot truncate " + collection, exception);
        }
    }

    @Override
    public long deleteAll() {
        return this.getKnownCollections().values().stream()
                .map(this::deleteAll)
                .filter(Predicate.isEqual(true))
                .count();
    }

    private String table(PersistenceCollection collection) {
        return this.getBasePath().sub(collection).toSqlIdentifier();
    }
}
