package eu.okaeri.platform.bukkit.i18n.message;

import eu.okaeri.platform.core.i18n.message.Audience;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class BukkitAudience extends Audience {

    protected BukkitAudience() {
        this(new ArrayList<>());
    }

    protected BukkitAudience(Collection<Object> targets) {
        super(targets);
    }

    public static BukkitAudience of(@NonNull Object... targets) {
        return (targets.length == 0)
                ? new BukkitAudience()
                : new BukkitAudience(new ArrayList<>(Arrays.asList(targets)));
    }

    public static BukkitAudience of(@NonNull Collection<Object> targets) {
        return new BukkitAudience(targets);
    }

    @Override
    public BukkitAudience andOf(@NonNull Object... targets) {
        this.targets.addAll(Arrays.asList(targets));
        return this;
    }

    @Override
    public BukkitAudience andOf(@NonNull Collection<Object> targets) {
        this.targets.addAll(targets);
        return this;
    }

    public static BukkitAudience ofAllPlayers() {
        return new BukkitAudience().andOfAllPlayers();
    }

    public BukkitAudience andOfAllPlayers() {
        this.targets.addAll(Bukkit.getOnlinePlayers());
        return this;
    }

    public static BukkitAudience ofPlayersThatCanSee(@NonNull Player player) {
        return new BukkitAudience().andOffPlayersThatCanSee(player);
    }

    public BukkitAudience andOffPlayersThatCanSee(@NonNull Player player) {
        this.targets.addAll(new ArrayList<>(Bukkit.getOnlinePlayers()).stream()
                .filter(onlinePlayer -> onlinePlayer.canSee(player))
                .collect(Collectors.toList()));
        return this;
    }

    public static BukkitAudience ofPlayersWithPermission(@NonNull String permission) {
        return new BukkitAudience().andOfPlayersWithPermission(permission);
    }

    public BukkitAudience andOfPlayersWithPermission(@NonNull String permission) {
        this.targets.addAll(new ArrayList<>(Bukkit.getOnlinePlayers()).stream()
                .filter(player -> player.hasPermission(permission))
                .collect(Collectors.toList()));
        return this;
    }

    public static BukkitAudience ofConsole() {
        return new BukkitAudience().andOfConsole();
    }

    public BukkitAudience andOfConsole() {
        this.targets.add(Bukkit.getConsoleSender());
        return this;
    }
}
