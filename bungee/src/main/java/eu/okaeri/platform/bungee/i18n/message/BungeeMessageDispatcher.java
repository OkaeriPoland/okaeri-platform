package eu.okaeri.platform.bungee.i18n.message;

import eu.okaeri.i18n.message.Message;
import eu.okaeri.i18n.message.MessageDispatcher;
import eu.okaeri.placeholders.Placeholders;
import eu.okaeri.placeholders.context.PlaceholderContext;
import eu.okaeri.placeholders.message.CompiledMessage;
import eu.okaeri.platform.bungee.i18n.BI18n;
import eu.okaeri.platform.core.placeholder.PlaceholdersFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class BungeeMessageDispatcher implements MessageDispatcher<Message> {

    private final BI18n i18n;
    private final String key;
    private final Placeholders placeholders;
    private final PlaceholdersFactory placeholdersFactory;
    private final Map<String, Object> fields = new LinkedHashMap<>();

    private BungeeMessageTarget target = BungeeMessageTarget.CHAT;
    private int titleFadeIn = 20;
    private int titleStay = 60;
    private int titleFadeOut = 20;

    public BungeeMessageDispatcher target(BungeeMessageTarget target) {
        this.target = target;
        return this;
    }

    public BungeeMessageDispatcher titleFadeIn(int duration) {
        this.titleFadeIn = duration;
        return this;
    }

    public BungeeMessageDispatcher titleStay(int duration) {
        this.titleStay = duration;
        return this;
    }

    public BungeeMessageDispatcher titleFadeOut(int duration) {
        this.titleFadeOut = duration;
        return this;
    }

    public BungeeMessageDispatcher titleData(int fadeIn, int stay, int fadeOut) {
        this.titleFadeIn = fadeIn;
        this.titleStay = stay;
        this.titleFadeOut = fadeOut;
        return this;
    }

    public BungeeMessageDispatcher with(@NonNull String field, Object value) {
        this.fields.put(field, value);
        return this;
    }

    @Override
    public BungeeMessageDispatcher sendTo(@NonNull Object entity) {
        if (entity instanceof CommandSender) {
            return this.sendTo(((CommandSender) entity));
        }
        throw new IllegalArgumentException("Unsupported entity type: " + entity.getClass());
    }

    public BungeeMessageDispatcher sendTo(@NonNull Collection<? extends CommandSender> receivers) {
        receivers.forEach(this::sendTo);
        return this;
    }

    public BungeeMessageDispatcher sendTo(@NonNull Stream<? extends CommandSender> receivers) {
        receivers.forEach(this::sendTo);
        return this;
    }

    public BungeeMessageDispatcher sendTo(@NonNull CommandSender receiver) {

        CompiledMessage compiled = this.i18n.get(receiver, this.key).compiled();
        PlaceholderContext context = PlaceholderContext.of(this.placeholders, compiled);
        this.placeholdersFactory.provide(receiver).forEach(context::with);
        this.fields.forEach(context::with);
        String contents = context.apply();

        // do not dispatch empty messages
        if (contents.isEmpty()) {
            return this;
        }

        // target is chat or receiver is not a player
        if (this.target == BungeeMessageTarget.CHAT || !(receiver instanceof ProxiedPlayer)) {
            receiver.sendMessage(TextComponent.fromLegacyText(contents));
            return this;
        }

        // action bar for player
        if (this.target == BungeeMessageTarget.ACTION_BAR) {
            ProxiedPlayer player = (ProxiedPlayer) receiver;
            player.sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(contents));
            return this;
        }

        // title for player
        if (this.target == BungeeMessageTarget.TITLE) {

            String[] parts = contents.split("\n", 2);
            String title = parts[0];
            String subtitle = parts.length > 1 ? parts[1] : "";

            ProxiedPlayer player = (ProxiedPlayer) receiver;
            Title bungeeTitle = ProxyServer.getInstance().createTitle()
                .title(TextComponent.fromLegacyText(title))
                .subTitle(TextComponent.fromLegacyText(subtitle))
                .fadeIn(this.titleFadeIn)
                .stay(this.titleStay)
                .fadeOut(this.titleFadeOut);
            player.sendTitle(bungeeTitle);
            return this;
        }

        throw new IllegalArgumentException("Unsupported target: " + this.target);
    }

    public BungeeMessageDispatcher sendToAllPlayers() {
        return this.sendTo(ProxyServer.getInstance().getPlayers());
    }

    public BungeeMessageDispatcher sendToPlayersWithPermission(@NonNull String permission) {
        return this.sendTo(new ArrayList<>(ProxyServer.getInstance().getPlayers()).stream()
            .filter(player -> player.hasPermission(permission))
            .collect(Collectors.toList()));
    }

    public BungeeMessageDispatcher sendToConsole() {
        return this.sendTo(ProxyServer.getInstance().getConsole());
    }
}
