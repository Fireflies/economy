package ninja.smirking.economy.storage;

import ninja.smirking.economy.EconomyData;

import java.util.UUID;

/**
 * Interface for communicating with the database, filesystem or whatever is holding economy data.
 *
 * @author Connor Spencer Harries
 */
public interface EconomyDataDao {
    boolean isLoaded(UUID uniqueId);
    EconomyData get(UUID uniqueId);
    void save(EconomyData data);
    void saveAll();

    default void close() {

    }
}
