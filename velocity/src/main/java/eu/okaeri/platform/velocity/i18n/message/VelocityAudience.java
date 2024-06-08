package eu.okaeri.platform.velocity.i18n.message;

import eu.okaeri.i18n.message.Message;
import eu.okaeri.platform.core.i18n.message.Audience;
import eu.okaeri.platform.velocity.util.VelocityUnsafe;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class VelocityAudience extends Audience<Message> {

    protected VelocityAudience() {
        this(new ArrayList<>());
    }

    protected VelocityAudience(Collection<Object> targets) {
        super(targets);
    }

    public static VelocityAudience of(@NonNull Object... targets) {
        return (targets.length == 0)
            ? new VelocityAudience()
            : new VelocityAudience(new ArrayList<>(Arrays.asList(targets)));
    }

    public static VelocityAudience of(@NonNull Collection<Object> targets) {
        return new VelocityAudience(targets);
    }

    @Override
    public VelocityAudience andOf(@NonNull Object... targets) {
        this.targets.addAll(Arrays.asList(targets));
        return this;
    }

    @Override
    public VelocityAudience andOf(@NonNull Collection<Object> targets) {
        this.targets.addAll(targets);
        return this;
    }

    public static VelocityAudience ofAllPlayers() {
        return new VelocityAudience().andOfAllPlayers();
    }

    public VelocityAudience andOfAllPlayers() {
        this.targets.addAll(VelocityUnsafe.PROXY.getAllPlayers());
        return this;
    }

    public static VelocityAudience ofPlayersWithPermission(@NonNull String permission) {
        return new VelocityAudience().andOfPlayersWithPermission(permission);
    }

    public VelocityAudience andOfPlayersWithPermission(@NonNull String permission) {
        this.targets.addAll(new ArrayList<>(VelocityUnsafe.PROXY.getAllPlayers()).stream()
            .filter(player -> player.hasPermission(permission))
            .collect(Collectors.toList()));
        return this;
    }

    public static VelocityAudience ofConsole() {
        return new VelocityAudience().andOfConsole();
    }

    public VelocityAudience andOfConsole() {
        this.targets.add(VelocityUnsafe.PROXY.getConsoleCommandSource());
        return this;
    }
}
