package ninja.smirking.economy;

import ninja.smirking.economy.event.PlayerListener;
import ninja.smirking.economy.storage.EconomyDataDao;
import ninja.smirking.economy.storage.MemoryEconomyDataDao;
import ninja.smirking.economy.storage.MysqlEconomyDataDao;
import ninja.smirking.economy.vault.EconomyImpl;

import java.util.logging.Level;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class EconomyPlugin extends JavaPlugin {
    public static final long SAVE_INTERVAL = Long.getLong("economy.save-interval", 600L);

    private EconomyDataDao dao;

    @Override
    public void onDisable() {
        if (dao != null) {
            try {
                dao.close();
            } catch (Exception ex) {
                getLogger().log(Level.SEVERE, "An exception was unhandled whilst closing the DAO: ", new Object[]{
                    ex
                });
            }
        }
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (getConfig().getBoolean("memory", false)) {
            dao = new MemoryEconomyDataDao();
        } else {
            try {
                dao = new MysqlEconomyDataDao(this);
            } catch (Exception ex) {
                ex.printStackTrace();
                getPluginLoader().disablePlugin(this);
                return;
            }
        }

        if (getServer().getPluginManager().isPluginEnabled("Vault")) {
            getServer().getServicesManager().register(Economy.class, new EconomyImpl(this), this, ServicePriority.Highest);
        }

        getServer().getServicesManager().register(EconomyDataDao.class, dao, this, ServicePriority.Highest);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    public EconomyDataDao getDataManager() {
        return dao;
    }
}
