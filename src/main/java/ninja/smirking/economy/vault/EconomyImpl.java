package ninja.smirking.economy.vault;

import ninja.smirking.economy.EconomyData;
import ninja.smirking.economy.EconomyPlugin;

import com.google.common.collect.ImmutableList;

import java.text.DecimalFormat;
import java.util.List;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * @author Connor Spencer Harries
 */
public class EconomyImpl implements Economy {
    private static final DecimalFormat FORMATTER = new DecimalFormat("#.00");

    static {
        FORMATTER.setMinimumFractionDigits(2);
        FORMATTER.setMinimumIntegerDigits(2);
    }

    private final EconomyPlugin plugin;

    public EconomyImpl(EconomyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }

    @Override
    public String getName() {
        return "Economy";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return FORMATTER.getMinimumFractionDigits();
    }

    @Override
    public String format(double v) {
        return FORMATTER.format(v);
    }

    @Override
    public String currencyNamePlural() {
        return "dollars";
    }

    @Override
    public String currencyNameSingular() {
        return "dollar";
    }

    @Override
    public boolean hasAccount(String s) {
        Player player = plugin.getServer().getPlayerExact(s);
        return player != null && hasAccount(player);
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        return plugin.getDataManager().get(offlinePlayer.getUniqueId()) != null;
    }

    @Override
    public boolean hasAccount(String s, String s1) {
        return hasAccount(s);
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer, String s) {
        return hasAccount(offlinePlayer);
    }

    @Override
    public double getBalance(String s) {
        Player player = plugin.getServer().getPlayerExact(s);
        if (player == null) {
            return 0.0D;
        }
        return getBalance(player);
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer) {
        return plugin.getDataManager().get(offlinePlayer.getUniqueId()).getBalance();
    }

    @Override
    public double getBalance(String s, String s1) {
        return getBalance(s);
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer, String s) {
        return getBalance(offlinePlayer);
    }

    @Override
    public boolean has(String s, double v) {
        Player player = plugin.getServer().getPlayerExact(s);
        return player != null && has(player, v);
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, double v) {
        return Double.compare(plugin.getDataManager().get(offlinePlayer.getUniqueId()).getBalance(), v) >= 0;
    }

    @Override
    public boolean has(String s, String s1, double v) {
        return has(s, v);
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, String s, double v) {
        return has(offlinePlayer, v);
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, double v) {
        Player player = plugin.getServer().getPlayerExact(s);
        if (player == null) {
            return new EconomyResponse(0.0D, 0.0D, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "player is offline");
        }
        return withdrawPlayer(player, v);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double v) {
        EconomyData data = plugin.getDataManager().get(offlinePlayer.getUniqueId());
        if (has(offlinePlayer, v)) {
            data.setBalance(data.getBalance() - v);
            return new EconomyResponse(v, data.getBalance(), EconomyResponse.ResponseType.SUCCESS, "");
        }
        return new EconomyResponse(v, data.getBalance(), EconomyResponse.ResponseType.FAILURE, "player does not have enough");
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, String s1, double v) {
        return withdrawPlayer(s, v);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, String s, double v) {
        return withdrawPlayer(offlinePlayer, v);
    }

    @Override
    public EconomyResponse depositPlayer(String s, double v) {
        Player player = plugin.getServer().getPlayerExact(s);
        if (player == null) {
            return new EconomyResponse(0.0D, 0.0D, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "player is offline");
        }
        return depositPlayer(player, v);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double v) {
        EconomyData data = plugin.getDataManager().get(offlinePlayer.getUniqueId());
        data.setBalance(data.getBalance() + v);
        return new EconomyResponse(v, data.getBalance(), EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse depositPlayer(String s, String s1, double v) {
        return depositPlayer(s, v);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, String s, double v) {
        return depositPlayer(offlinePlayer, v);
    }

    @Override
    public EconomyResponse createBank(String s, String s1) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "banks are not supported");
    }

    @Override
    public EconomyResponse createBank(String s, OfflinePlayer offlinePlayer) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "banks are not supported");
    }

    @Override
    public EconomyResponse deleteBank(String s) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "banks are not supported");
    }

    @Override
    public EconomyResponse bankBalance(String s) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "banks are not supported");
    }

    @Override
    public EconomyResponse bankHas(String s, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "banks are not supported");
    }

    @Override
    public EconomyResponse bankWithdraw(String s, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "banks are not supported");
    }

    @Override
    public EconomyResponse bankDeposit(String s, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "banks are not supported");
    }

    @Override
    public EconomyResponse isBankOwner(String s, String s1) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "banks are not supported");
    }

    @Override
    public EconomyResponse isBankOwner(String s, OfflinePlayer offlinePlayer) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "banks are not supported");
    }

    @Override
    public EconomyResponse isBankMember(String s, String s1) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "banks are not supported");
    }

    @Override
    public EconomyResponse isBankMember(String s, OfflinePlayer offlinePlayer) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "banks are not supported");
    }

    @Override
    public List<String> getBanks() {
        return ImmutableList.of();
    }

    @Override
    public boolean createPlayerAccount(String s) {
        Player player = plugin.getServer().getPlayerExact(s);
        if (player != null) {
            return createPlayerAccount(player);
        }
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
        return plugin.getDataManager().get(offlinePlayer.getUniqueId()) != null;
    }

    @Override
    public boolean createPlayerAccount(String s, String s1) {
        return createPlayerAccount(s);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String s) {
        return createPlayerAccount(offlinePlayer);
    }
}
