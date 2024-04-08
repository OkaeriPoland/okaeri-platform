//package eu.okaeri.platform.velocity.i18n.message;
//
//import eu.okaeri.i18n.minecraft.bungee.BungeeMessage;
//import eu.okaeri.i18n.message.Message;
//import eu.okaeri.i18n.message.MessageDispatcher;
//import eu.okaeri.placeholders.Placeholders;
//import eu.okaeri.placeholders.context.PlaceholderContext;
//import eu.okaeri.platform.velocity.i18n.BI18n;
//import eu.okaeri.platform.core.placeholder.PlaceholdersFactory;
//import lombok.NonNull;
//import lombok.RequiredArgsConstructor;
//import net.md_5.bungee.api.ChatMessageType;
//import net.md_5.bungee.api.CommandSender;
//import net.md_5.bungee.api.ProxyServer;
//import net.md_5.bungee.api.Title;
//import net.md_5.bungee.api.chat.TextComponent;
//import net.md_5.bungee.api.connection.ProxiedPlayer;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.LinkedHashMap;
//import java.util.Map;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//@RequiredArgsConstructor
//public class VelocityMessageDispatcher implements MessageDispatcher<Message> {
//
//    private final BI18n i18n;
//    private final String key;
//    private final Placeholders placeholders;
//    private final PlaceholdersFactory placeholdersFactory;
//    private final Map<String, Object> fields = new LinkedHashMap<>();
//
//    private VelocityMessageTarget target = VelocityMessageTarget.CHAT;
//    private int titleFadeIn = 20;
//    private int titleStay = 60;
//    private int titleFadeOut = 20;
//
//    public VelocityMessageDispatcher target(VelocityMessageTarget target) {
//        this.target = target;
//        return this;
//    }
//
//    public VelocityMessageDispatcher titleFadeIn(int duration) {
//        this.titleFadeIn = duration;
//        return this;
//    }
//
//    public VelocityMessageDispatcher titleStay(int duration) {
//        this.titleStay = duration;
//        return this;
//    }
//
//    public VelocityMessageDispatcher titleFadeOut(int duration) {
//        this.titleFadeOut = duration;
//        return this;
//    }
//
//    public VelocityMessageDispatcher titleData(int fadeIn, int stay, int fadeOut) {
//        this.titleFadeIn = fadeIn;
//        this.titleStay = stay;
//        this.titleFadeOut = fadeOut;
//        return this;
//    }
//
//    public VelocityMessageDispatcher with(@NonNull String field, Object value) {
//        this.fields.put(field, value);
//        return this;
//    }
//
//    private VelocityMessageDispatcher with(@NonNull Map<String, Object> fields) {
//        this.fields.putAll(fields);
//        return this;
//    }
//
//    public VelocityMessageDispatcher with(@NonNull PlaceholderContext context) {
//        context.getFields().forEach((k, p) -> this.fields.put(k, p.getValue()));
//        return this;
//    }
//
//    @Override
//    public VelocityMessageDispatcher sendTo(@NonNull Object entity) {
//        if (entity instanceof CommandSender) {
//            return this.sendTo(((CommandSender) entity));
//        }
//        throw new IllegalArgumentException("Unsupported entity type: " + entity.getClass());
//    }
//
//    public VelocityMessageDispatcher sendTo(@NonNull Collection<? extends CommandSender> receivers) {
//        receivers.forEach(this::sendTo);
//        return this;
//    }
//
//    public VelocityMessageDispatcher sendTo(@NonNull Stream<? extends CommandSender> receivers) {
//        receivers.forEach(this::sendTo);
//        return this;
//    }
//
//    public VelocityMessageDispatcher sendTo(@NonNull CommandSender receiver) {
//
//        Message message = this.i18n.get(receiver, this.key);
//        this.placeholdersFactory.provide(receiver).forEach(message::with);
//        this.fields.forEach(message::with);
//
//        // do not dispatch empty messages
//        if (message.raw().isEmpty()) {
//            return this;
//        }
//
//        // target is chat or receiver is not a player
//        if (this.target == VelocityMessageTarget.CHAT || !(receiver instanceof ProxiedPlayer)) {
//            if (message instanceof BungeeMessage) {
//                receiver.sendMessage(((BungeeMessage) message).component());
//            } else {
//                receiver.sendMessage(TextComponent.fromLegacyText(message.apply()));
//            }
//            return this;
//        }
//
//        // action bar for player
//        if (this.target == VelocityMessageTarget.ACTION_BAR) {
//            ProxiedPlayer player = (ProxiedPlayer) receiver;
//            player.sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message.apply()));
//            return this;
//        }
//
//        // title for player
//        if (this.target == VelocityMessageTarget.TITLE) {
//
//            String[] parts = message.apply().split("\n", 2);
//            String title = parts[0];
//            String subtitle = parts.length > 1 ? parts[1] : "";
//
//            ProxiedPlayer player = (ProxiedPlayer) receiver;
//            Title bungeeTitle = ProxyServer.getInstance().createTitle()
//                .title(TextComponent.fromLegacyText(title))
//                .subTitle(TextComponent.fromLegacyText(subtitle))
//                .fadeIn(this.titleFadeIn)
//                .stay(this.titleStay)
//                .fadeOut(this.titleFadeOut);
//            player.sendTitle(bungeeTitle);
//            return this;
//        }
//
//        throw new IllegalArgumentException("Unsupported target: " + this.target);
//    }
//
//    public VelocityMessageDispatcher sendToAllPlayers() {
//        return this.sendTo(ProxyServer.getInstance().getPlayers());
//    }
//
//    public VelocityMessageDispatcher sendToPlayersWithPermission(@NonNull String permission) {
//        return this.sendTo(new ArrayList<>(ProxyServer.getInstance().getPlayers()).stream()
//            .filter(player -> player.hasPermission(permission))
//            .collect(Collectors.toList()));
//    }
//
//    public VelocityMessageDispatcher sendToConsole() {
//        return this.sendTo(ProxyServer.getInstance().getConsole());
//    }
//}
