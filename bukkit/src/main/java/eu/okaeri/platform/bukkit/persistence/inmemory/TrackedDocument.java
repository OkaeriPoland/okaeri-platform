package eu.okaeri.platform.bukkit.persistence.inmemory;

import eu.okaeri.configs.exception.OkaeriException;
import eu.okaeri.persistence.document.Document;
import eu.okaeri.platform.bukkit.persistence.BukkitDocument;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A {@link BukkitDocument} with built-in dirty tracking for efficient batch saving.
 * <p>
 * Documents are marked as "dirty" when modified, allowing handlers like
 * {@link InMemoryPlayerHandler} to save only changed documents during
 * periodic save cycles, reducing unnecessary database operations.
 * <p>
 * The dirty flag is thread-safe and is automatically cleared after a
 * successful {@link #save()} operation.
 * <p>
 * Example usage:
 * <pre>{@code
 * public class PlayerData extends TrackedDocument {
 *     private int coins;
 *
 *     public void addCoins(int amount) {
 *         this.coins += amount;
 *         this.markDirty();
 *     }
 * }
 * }</pre>
 *
 * @see InMemoryPlayerHandler
 */
public class TrackedDocument extends BukkitDocument {

    private transient final AtomicBoolean dirty = new AtomicBoolean();

    /**
     * Marks this document as dirty, indicating it has unsaved changes.
     * <p>
     * Call this method after modifying the document's state to ensure
     * changes are persisted during the next save cycle.
     * <p>
     * When using {@link InMemoryPlayerHandler#modify}, the dirty flag
     * is set automatically after the consumer completes.
     */
    public void markDirty() {
        this.dirty.set(true);
    }

    /**
     * Atomically clears the dirty flag and returns the previous value.
     *
     * @return {@code true} if the document was dirty, {@code false} otherwise
     */
    boolean clearDirty() {
        return this.dirty.getAndSet(false);
    }

    /**
     * Saves the document and clears the dirty flag on success.
     *
     * @return this document instance for chaining
     * @throws OkaeriException       if the save operation fails
     * @throws IllegalStateException if called from the main server thread
     */
    @Override
    public Document save() throws OkaeriException {
        Document result = super.save();
        this.dirty.set(false);
        return result;
    }
}
