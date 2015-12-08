package ninja.smirking.economy;

import com.google.common.base.Objects;

import java.util.UUID;

public final class EconomyData {
    private final UUID uniqueId;

    private volatile boolean dirty;
    private double balance;

    public EconomyData(UUID uniqueId, double balance) {
        this.uniqueId = uniqueId;
        this.balance = balance;
        this.dirty = true;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public synchronized double getBalance() {
        return balance;
    }

    public synchronized void setBalance(double balance) {
        this.balance = balance;
        setDirty(true);
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != getClass()) {
            return false;
        }

        if (o == this) {
            return true;
        }

        EconomyData that = (EconomyData) o;
        return Objects.equal(uniqueId, that.uniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uniqueId);
    }
}
