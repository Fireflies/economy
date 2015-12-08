package ninja.smirking.economy.storage;

import ninja.smirking.economy.EconomyData;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.UUID;

/**
 * Not really a DAO but didn't really want to name the interface something else for a single implementation.
 * 
 * @author Connor Spencer Harries
 */
public class MemoryEconomyDataDao implements EconomyDataDao {
    private final Cache<UUID, EconomyData> cache;

    public MemoryEconomyDataDao() {
        this.cache = CacheBuilder.newBuilder().maximumSize(500).build();
    }

    @Override
    public boolean isLoaded(UUID uniqueId) {
        return cache.getIfPresent(uniqueId) != null;
    }

    @Override
    public EconomyData get(UUID uniqueId) {
        EconomyData data = cache.getIfPresent(uniqueId);
        if (data == null) {
            data = new EconomyData(uniqueId, 0.0D);
            cache.put(uniqueId, data);
        }
        return data;
    }

    @Override
    public void save(EconomyData data) {
        // NOP
    }

    @Override
    public void saveAll() {
        // NOP
    }

    @Override
    public void close() {
        cache.invalidateAll();
    }
}
