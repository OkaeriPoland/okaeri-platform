package eu.okaeri.platform.bukkit.persistence.inmemory;

import eu.okaeri.persistence.repository.DocumentRepository;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An in-memory cache handler for player documents that provides automatic
 * loading, saving, and lifecycle management.
 * <p>
 * This handler maintains player documents in memory while they are online,
 * automatically loading them on login and saving them on quit. A periodic
 * background task saves dirty documents at a configurable interval.
 * <p>
 * Key features:
 * <ul>
 *   <li>Automatic pre-loading during {@code AsyncPlayerPreLoginEvent}</li>
 *   <li>Automatic save and eviction on {@code PlayerQuitEvent}</li>
 *   <li>Periodic batch saving of dirty documents</li>
 *   <li>Automatic shutdown save on plugin disable</li>
 *   <li>Thread-safe operations with main-thread protection</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * public class MyPlugin extends JavaPlugin {
 *
 *     private InMemoryPlayerHandler<PlayerData> playerHandler;
 *
 *     @Override
 *     public void onEnable() {
 *         DocumentRepository<UUID, PlayerData> repository = ...;
 *         this.playerHandler = new InMemoryPlayerHandler<>(this, repository)
 *             .register(20L * 60); // save every minute
 *     }
 *
 *     public void addCoins(Player player, int amount) {
 *         this.playerHandler.modify(player.getUniqueId(), data -> {
 *             data.setCoins(data.getCoins() + amount);
 *         });
 *     }
 *
 *     public int getCoins(Player player) {
 *         return this.playerHandler.get(player).getCoins();
 *     }
 * }
 * }</pre>
 *
 * @param <T> the document type, must extend {@link TrackedDocument}
 * @see TrackedDocument
 */
public class InMemoryPlayerHandler<T extends TrackedDocument> {

    static final Logger LOGGER = Logger.getLogger(InMemoryPlayerHandler.class.getSimpleName());

    final Map<UUID, T> cache = new ConcurrentHashMap<>();
    final Plugin plugin;
    final DocumentRepository<UUID, T> repository;

    /**
     * Creates a new handler for the specified plugin and repository.
     *
     * @param plugin     the owning plugin instance
     * @param repository the document repository for persistence operations
     */
    public InMemoryPlayerHandler(@NonNull Plugin plugin, @NonNull DocumentRepository<UUID, T> repository) {
        this.plugin = plugin;
        this.repository = repository;
    }

    /**
     * Registers event listeners and starts the periodic save task.
     * <p>
     * This method registers:
     * <ul>
     *   <li>Login listener for pre-loading documents</li>
     *   <li>Quit listener for saving and evicting documents</li>
     *   <li>Plugin disable listener for shutdown saves</li>
     *   <li>Async timer task for periodic dirty document saves</li>
     * </ul>
     *
     * @param saveRate the interval between save cycles in ticks (20 ticks = 1 second)
     * @return this handler instance for chaining
     */
    public InMemoryPlayerHandler<T> register(long saveRate) {
        this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(this.plugin, new InMemoryPlayerTask<>(this), saveRate, saveRate);
        this.plugin.getServer().getPluginManager().registerEvents(new InMemoryPlayerListener<>(this), this.plugin);
        return this;
    }

    /**
     * Saves all dirty documents and clears the cache.
     * <p>
     * This method is automatically called on plugin disable but can be
     * invoked manually if needed. Runs synchronously on the calling thread.
     */
    public void shutdown() {
        this.saveAll();
        this.cache.clear();
    }

    /**
     * Saves all dirty documents in the cache.
     * <p>
     * Only documents marked as dirty are saved. The dirty flag is cleared
     * before saving to prevent duplicate saves if modified during the operation.
     */
    void saveAll() {
        for (T player : this.cache.values()) {
            if (!player.clearDirty()) {
                continue;
            }
            try {
                player.save();
            } catch (Exception exception) {
                LOGGER.log(Level.SEVERE, "Failed to save " + player.getClass().getSimpleName() + "/" + player.getPath().getValue(), exception);
            }
        }
    }

    /**
     * Gets a player's document, loading it from the repository if not cached.
     * <p>
     * If the document is not in cache, it will be loaded from the repository.
     * This operation blocks and must not be called from the main thread when
     * loading is required.
     *
     * @param playerId the player's unique identifier
     * @return the player's document, never null
     * @throws IllegalStateException if loading is required and called from the main thread
     */
    public T get(@NonNull UUID playerId) {
        return this.cache.computeIfAbsent(playerId, id -> {
            if (Bukkit.isPrimaryThread()) {
                throw new IllegalStateException("Cannot fetch in #get(UUID) from the main thread!");
            }
            return this.repository.findOrCreateByPath(id);
        });
    }

    /**
     * Gets a player's document, loading it from the repository if not cached.
     *
     * @param player the player
     * @return the player's document, never null
     * @throws IllegalStateException if loading is required and called from the main thread
     * @see #get(UUID)
     */
    public T get(@NonNull OfflinePlayer player) {
        return this.get(player.getUniqueId());
    }

    /**
     * Gets a player's document only if it is already cached.
     * <p>
     * This method never blocks and is safe to call from any thread,
     * including the main thread.
     *
     * @param playerId the player's unique identifier
     * @return the cached document, or {@code null} if not loaded
     */
    public T getIfCached(@NonNull UUID playerId) {
        return this.cache.get(playerId);
    }

    /**
     * Gets a player's document only if it is already cached.
     *
     * @param player the player
     * @return the cached document, or {@code null} if not loaded
     * @see #getIfCached(UUID)
     */
    public T getIfCached(@NonNull OfflinePlayer player) {
        return this.getIfCached(player.getUniqueId());
    }

    /**
     * Checks if a player's document is currently loaded in the cache.
     *
     * @param playerId the player's unique identifier
     * @return {@code true} if the document is cached, {@code false} otherwise
     */
    public boolean isLoaded(@NonNull UUID playerId) {
        return this.cache.containsKey(playerId);
    }

    /**
     * Checks if a player's document is currently loaded in the cache.
     *
     * @param player the player
     * @return {@code true} if the document is cached, {@code false} otherwise
     * @see #isLoaded(UUID)
     */
    public boolean isLoaded(@NonNull OfflinePlayer player) {
        return this.isLoaded(player.getUniqueId());
    }

    /**
     * Returns an unmodifiable view of all currently loaded documents.
     * <p>
     * The returned collection reflects the current cache state and may
     * change as players join and leave.
     *
     * @return unmodifiable collection of cached documents
     */
    public Collection<T> getLoaded() {
        return Collections.unmodifiableCollection(this.cache.values());
    }

    /**
     * Modifies a player's document and marks it as dirty.
     * <p>
     * The consumer is executed with the document, and the dirty flag is
     * automatically set after the consumer completes, ensuring changes
     * are persisted in the next save cycle.
     * <p>
     * Example:
     * <pre>{@code
     * handler.modify(playerId, data -> {
     *     data.setCoins(data.getCoins() + 100);
     *     data.setLastReward(System.currentTimeMillis());
     * });
     * }</pre>
     *
     * @param playerId the player's unique identifier
     * @param consumer the modification function
     * @return the modified document
     * @throws IllegalStateException if loading is required and called from the main thread
     */
    public T modify(@NonNull UUID playerId, @NonNull Consumer<T> consumer) {
        T player = this.get(playerId);
        consumer.accept(player);
        player.markDirty();
        return player;
    }

    /**
     * Modifies a player's document and marks it as dirty.
     *
     * @param player   the player
     * @param consumer the modification function
     * @return the modified document
     * @throws IllegalStateException if loading is required and called from the main thread
     * @see #modify(UUID, Consumer)
     */
    public T modify(@NonNull OfflinePlayer player, @NonNull Consumer<T> consumer) {
        return this.modify(player.getUniqueId(), consumer);
    }
}
