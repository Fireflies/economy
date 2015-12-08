package ninja.smirking.economy.event;

import ninja.smirking.economy.EconomyData;
import ninja.smirking.economy.EconomyPlugin;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public final class PlayerListener implements Listener {
    private final ListeningExecutorService executor;
    private final EconomyPlugin plugin;

    public PlayerListener(EconomyPlugin plugin) {
        this.executor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool(new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("Economy Thread #%d",  threadNumber.getAndIncrement()));
            }
        }));
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        if (!plugin.getDataManager().isLoaded(event.getPlayer().getUniqueId())) {
            Futures.addCallback(executor.submit(() -> plugin.getDataManager().get(event.getPlayer().getUniqueId())), new FutureCallback<EconomyData>() {
                @Override
                public void onSuccess(EconomyData result) {
                    plugin.getLogger().log(Level.INFO, "Loaded {0}''s balance", new Object[] {
                        result.getUniqueId()
                    });
                }

                @Override
                public void onFailure(Throwable t) {
                    /*
                     * Do nothing, this shouldn't happen.
                     */
                }
            });
        }
    }
}
