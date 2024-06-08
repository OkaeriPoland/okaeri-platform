package eu.okaeri.platform.velocity.i18n.message;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import eu.okaeri.i18n.message.Message;
import eu.okaeri.i18n.message.MessageDispatcher;
import eu.okaeri.placeholders.Placeholders;
import eu.okaeri.placeholders.context.PlaceholderContext;
import eu.okaeri.platform.core.placeholder.PlaceholdersFactory;
import eu.okaeri.platform.velocity.i18n.BI18n;
import eu.okaeri.platform.velocity.util.VelocityUnsafe;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class VelocityMessageDispatcher implements MessageDispatcher<Message> {

    private final BI18n i18n;
    private final String key;
    private final Placeholders placeholders;
    private final PlaceholdersFactory placeholdersFactory;
    private final Map<String, Object> fields = new LinkedHashMap<>();

    private VelocityMessageTarget target = VelocityMessageTarget.CHAT;
    private int titleFadeIn = 20;
    private int titleStay = 60;
    private int titleFadeOut = 20;

    public VelocityMessageDispatcher target(VelocityMessageTarget target) {
        this.target = target;
        return this;
    }

    public VelocityMessageDispatcher titleFadeIn(int duration) {
        this.titleFadeIn = duration;
        return this;
    }

    public VelocityMessageDispatcher titleStay(int duration) {
        this.titleStay = duration;
        return this;
    }

    public VelocityMessageDispatcher titleFadeOut(int duration) {
        this.titleFadeOut = duration;
        return this;
    }

    public VelocityMessageDispatcher titleData(int fadeIn, int stay, int fadeOut) {
        this.titleFadeIn = fadeIn;
        this.titleStay = stay;
        this.titleFadeOut = fadeOut;
        return this;
    }

    public VelocityMessageDispatcher with(@NonNull String field, Object value) {
        this.fields.put(field, value);
        return this;
    }

    private VelocityMessageDispatcher with(@NonNull Map<String, Object> fields) {
        this.fields.putAll(fields);
        return this;
    }

    public VelocityMessageDispatcher with(@NonNull PlaceholderContext context) {
        context.getFields().forEach((k, p) -> this.fields.put(k, p.getValue()));
        return this;
    }

    @Override
    public VelocityMessageDispatcher sendTo(@NonNull Object entity) {
        if (entity instanceof CommandSource) {
            return this.sendTo(entity);
        }
        throw new IllegalArgumentException("Unsupported entity type: " + entity.getClass());
    }

    public VelocityMessageDispatcher sendTo(@NonNull Collection<? extends CommandSource> receivers) {
        receivers.forEach(this::sendTo);
        return this;
    }

    public VelocityMessageDispatcher sendTo(@NonNull Stream<? extends CommandSource> receivers) {
        receivers.forEach(this::sendTo);
        return this;
    }

    public VelocityMessageDispatcher sendTo(@NonNull CommandSource receiver) {

        Message message = this.i18n.get(receiver, this.key);
        this.placeholdersFactory.provide(receiver).forEach(message::with);
        this.fields.forEach(message::with);

        // do not dispatch empty messages
        if (message.raw().isEmpty()) {
            return this;
        }

        // target is chat or receiver is not a player
        if (this.target == VelocityMessageTarget.CHAT || !(receiver instanceof Player)) {
//            if (message instanceof BungeeMessage) { FIXME: adventure support
//                receiver.sendMessage(((BungeeMessage) message).component());
//            } else {
                receiver.sendMessage(LegacyComponentSerializer.legacySection().deserialize(message.apply()));
//            }
            return this;
        }

        // action bar for player
        if (this.target == VelocityMessageTarget.ACTION_BAR) {
            Player player = (Player) receiver;
            player.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(message.apply()));
            return this;
        }

        // title for player
        if (this.target == VelocityMessageTarget.TITLE) {

            String[] parts = message.apply().split("\n", 2);
            String title = parts[0];
            String subtitle = parts.length > 1 ? parts[1] : "";

            Player player = (Player) receiver;
            Title velocityTitle = Title.title(
                LegacyComponentSerializer.legacySection().deserialize(title),
                LegacyComponentSerializer.legacySection().deserialize(subtitle),
                Title.Times.times(Duration.ofMillis(this.titleFadeIn * 50L), Duration.ofMillis(this.titleStay * 50L), Duration.ofMillis(this.titleFadeOut * 50L))
            );
            player.showTitle(velocityTitle);
            return this;
        }

        throw new IllegalArgumentException("Unsupported target: " + this.target);
    }

    public VelocityMessageDispatcher sendToAllPlayers() {
        return this.sendTo(VelocityUnsafe.PROXY.getAllPlayers());
    }

    public VelocityMessageDispatcher sendToPlayersWithPermission(@NonNull String permission) {
        return this.sendTo(new ArrayList<>(VelocityUnsafe.PROXY.getAllPlayers()).stream()
            .filter(player -> player.hasPermission(permission))
            .collect(Collectors.toList()));
    }

    public VelocityMessageDispatcher sendToConsole() {
        return this.sendTo(VelocityUnsafe.PROXY.getConsoleCommandSource());
    }
}
