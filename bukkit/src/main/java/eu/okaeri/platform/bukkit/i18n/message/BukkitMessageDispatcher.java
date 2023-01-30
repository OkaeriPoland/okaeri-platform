package eu.okaeri.platform.bukkit.i18n.message;

import eu.okaeri.commons.bukkit.UnsafeBukkitCommons;
import eu.okaeri.i18n.core.minecraft.bungee.BungeeMessage;
import eu.okaeri.i18n.message.Message;
import eu.okaeri.i18n.message.MessageDispatcher;
import eu.okaeri.platform.bukkit.i18n.BI18n;
import eu.okaeri.platform.core.placeholder.PlaceholdersFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class BukkitMessageDispatcher implements MessageDispatcher<Message> {

    private final BI18n i18n;
    private final String key;
    private final PlaceholdersFactory placeholdersFactory;
    private final Map<String, Object> fields = new LinkedHashMap<>();

    private BukkitMessageTarget target = BukkitMessageTarget.CHAT;
    private int titleFadeIn = 20;
    private int titleStay = 60;
    private int titleFadeOut = 20;

    public BukkitMessageDispatcher target(BukkitMessageTarget target) {
        this.target = target;
        return this;
    }

    public BukkitMessageDispatcher titleFadeIn(int duration) {
        this.titleFadeIn = duration;
        return this;
    }

    public BukkitMessageDispatcher titleStay(int duration) {
        this.titleStay = duration;
        return this;
    }

    public BukkitMessageDispatcher titleFadeOut(int duration) {
        this.titleFadeOut = duration;
        return this;
    }

    public BukkitMessageDispatcher titleData(int fadeIn, int stay, int fadeOut) {
        this.titleFadeIn = fadeIn;
        this.titleStay = stay;
        this.titleFadeOut = fadeOut;
        return this;
    }

    public BukkitMessageDispatcher with(@NonNull String field, Object value) {
        this.fields.put(field, value);
        return this;
    }

    @Override
    public BukkitMessageDispatcher sendTo(@NonNull Object entity) {
        if (entity instanceof CommandSender) {
            return this.sendTo(((CommandSender) entity));
        }
        throw new IllegalArgumentException("Unsupported entity type: " + entity.getClass());
    }

    public BukkitMessageDispatcher sendTo(@NonNull Collection<? extends CommandSender> receivers) {
        receivers.forEach(this::sendTo);
        return this;
    }

    public BukkitMessageDispatcher sendTo(@NonNull Stream<? extends CommandSender> receivers) {
        receivers.forEach(this::sendTo);
        return this;
    }

    public BukkitMessageDispatcher sendTo(@NonNull CommandSender receiver) {

        Message message = this.i18n.get(receiver, this.key);
        this.placeholdersFactory.provide(receiver).forEach(message::with);
        this.fields.forEach(message::with);

        // do not dispatch empty messages
        if (message.raw().isEmpty()) {
            return this;
        }

        // target is a non-player receiver
        if (!(receiver instanceof Player)) {
            receiver.sendMessage(message.apply());
            return this;
        }

        // chat for player
        if (this.target == BukkitMessageTarget.CHAT) {
            if (message instanceof BungeeMessage) {
                UnsafeBukkitCommons.sendComponent((Player) receiver, ((BungeeMessage) message).component(), UnsafeBukkitCommons.ChatTarget.CHAT);
            } else {
                receiver.sendMessage(message.apply());
            }
            return this;
        }

        // action bar for player
        if (this.target == BukkitMessageTarget.ACTION_BAR) {
            UnsafeBukkitCommons.sendMessage(((Player) receiver), message.apply(), UnsafeBukkitCommons.ChatTarget.ACTION_BAR);
            return this;
        }

        // title for player
        if (this.target == BukkitMessageTarget.TITLE) {
            String[] parts = message.apply().split("\n", 2);
            String title = parts[0];
            String subtitle = parts.length > 1 ? parts[1] : "";
            UnsafeBukkitCommons.sendTitle(((Player) receiver), title, subtitle, this.titleFadeIn, this.titleStay, this.titleFadeOut);
            return this;
        }

        throw new IllegalArgumentException("Unsupported target: " + this.target);
    }

    public BukkitMessageDispatcher sendToAllPlayers() {
        return this.sendTo(Bukkit.getOnlinePlayers());
    }

    public BukkitMessageDispatcher sendToPlayersThatCanSee(@NonNull Player player) {
        return this.sendTo(new ArrayList<>(Bukkit.getOnlinePlayers()).stream()
            .filter(onlinePlayer -> onlinePlayer.canSee(player))
            .collect(Collectors.toList()));
    }

    public BukkitMessageDispatcher sendToPlayersWithPermission(@NonNull String permission) {
        return this.sendTo(new ArrayList<>(Bukkit.getOnlinePlayers()).stream()
            .filter(player -> player.hasPermission(permission))
            .collect(Collectors.toList()));
    }

    public BukkitMessageDispatcher sendToConsole() {
        return this.sendTo(Bukkit.getConsoleSender());
    }
}
