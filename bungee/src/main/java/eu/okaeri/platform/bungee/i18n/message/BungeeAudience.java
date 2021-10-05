package eu.okaeri.platform.bungee.i18n.message;

import eu.okaeri.platform.core.i18n.message.Audience;
import lombok.NonNull;
import net.md_5.bungee.api.ProxyServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class BungeeAudience extends Audience {

    protected BungeeAudience() {
        this(new ArrayList<>());
    }

    protected BungeeAudience(Collection<Object> targets) {
        super(targets);
    }

    public static BungeeAudience of(@NonNull Object... targets) {
        return (targets.length == 0)
                ? new BungeeAudience()
                : new BungeeAudience(new ArrayList<>(Arrays.asList(targets)));
    }

    public static BungeeAudience of(@NonNull Collection<Object> targets) {
        return new BungeeAudience(targets);
    }

    @Override
    public BungeeAudience andOf(@NonNull Object... targets) {
        this.targets.addAll(Arrays.asList(targets));
        return this;
    }

    @Override
    public BungeeAudience andOf(@NonNull Collection<Object> targets) {
        this.targets.addAll(targets);
        return this;
    }

    public static BungeeAudience ofAllPlayers() {
        return new BungeeAudience().andOfAllPlayers();
    }

    public BungeeAudience andOfAllPlayers() {
        this.targets.addAll(ProxyServer.getInstance().getPlayers());
        return this;
    }

    public static BungeeAudience ofPlayersWithPermission(@NonNull String permission) {
        return new BungeeAudience().andOfPlayersWithPermission(permission);
    }

    public BungeeAudience andOfPlayersWithPermission(@NonNull String permission) {
        this.targets.addAll(new ArrayList<>(ProxyServer.getInstance().getPlayers()).stream()
                .filter(player -> player.hasPermission(permission))
                .collect(Collectors.toList()));
        return this;
    }

    public static BungeeAudience ofConsole() {
        return new BungeeAudience().andOfConsole();
    }

    public BungeeAudience andOfConsole() {
        this.targets.add(ProxyServer.getInstance().getConsole());
        return this;
    }
}
