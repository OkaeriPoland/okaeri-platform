package eu.okaeri.platform.bukkit.persistence;

import eu.okaeri.configs.exception.OkaeriException;
import eu.okaeri.persistence.document.Document;
import org.bukkit.Bukkit;

/**
 * A thread-safe {@link Document} implementation for Bukkit that prevents
 * blocking database operations from being executed on the main server thread.
 * <p>
 * Calling {@link #save()} from the primary thread will throw an
 * {@link IllegalStateException} to protect against server lag caused
 * by synchronous I/O operations.
 * <p>
 * Example usage:
 * <pre>{@code
 * public class PlayerData extends BukkitDocument {
 *     private int coins;
 *     private String rank;
 * }
 *
 * // Save must be called from an async context
 * Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
 *     playerData.save();
 * });
 * }</pre>
 */
public class BukkitDocument extends Document {

    /**
     * Saves the document to the configured persistence backend.
     * <p>
     * This method must be called from an asynchronous thread.
     * Calling from the main server thread will throw an exception.
     *
     * @return this document instance for chaining
     * @throws OkaeriException       if the save operation fails
     * @throws IllegalStateException if called from the main server thread
     */
    @Override
    public Document save() throws OkaeriException {
        if (Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Cannot execute #save() from the main thread!");
        }
        return super.save();
    }
}
