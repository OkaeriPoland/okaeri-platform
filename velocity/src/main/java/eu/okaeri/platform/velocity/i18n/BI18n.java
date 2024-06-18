package eu.okaeri.platform.velocity.i18n;

import eu.okaeri.i18n.configs.LocaleConfig;
import eu.okaeri.i18n.configs.extended.MessageMEOCI18n;
import eu.okaeri.i18n.extended.MessageColors;
import eu.okaeri.i18n.message.Message;
import eu.okaeri.i18n.message.SimpleMessage;
import eu.okaeri.i18n.minecraft.adventure.AdventureMessage;
import eu.okaeri.placeholders.Placeholders;
import eu.okaeri.placeholders.message.CompiledMessage;
import eu.okaeri.platform.core.i18n.message.MessageAssembler;
import eu.okaeri.platform.core.placeholder.PlaceholdersFactory;
import eu.okaeri.platform.velocity.i18n.message.VelocityMessageDispatcher;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.nio.file.Files;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class BI18n extends MessageMEOCI18n {

    private static final Pattern ALT_COLOR_PATTERN = Pattern.compile("&[0-9A-Fa-fK-Ok-oRXrx]");

    private final @Getter I18nColorsConfig colorsConfig;
    private final @Getter PlaceholdersFactory placeholdersFactory;

    private @Getter @Setter MessageAssembler messageAssembler = AdventureMessage::of;

    public BI18n(@NonNull I18nColorsConfig colorsConfig, @NonNull PlaceholdersFactory placeholdersFactory) {
        this.colorsConfig = colorsConfig;
        this.placeholdersFactory = placeholdersFactory;
    }

    @Override
    public Message assembleMessage(Placeholders placeholders, @NonNull CompiledMessage compiled) {
        return this.messageAssembler.assemble(placeholders, compiled);
    }

    @Override
    public VelocityMessageDispatcher get(@NonNull String key) {
        return new VelocityMessageDispatcher(this, key, this.getPlaceholders(), this.getPlaceholdersFactory());
    }

    @Override
    public Message get(@NonNull Object entity, @NonNull String key) {
        Message message = super.get(entity, key);
        this.getPlaceholdersFactory().provide(entity).forEach(message::with);
        return message;
    }

    @Override
    public void load() {

        if ((this.getColorsConfig().getBindFile() != null) && Files.exists(this.getColorsConfig().getBindFile())) {
            this.getColorsConfig().load();
        }

        for (Map.Entry<Locale, LocaleConfig> entry : this.getConfigs().entrySet()) {
            super.registerConfig(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public boolean hasColors(@NonNull String text) {
        return ALT_COLOR_PATTERN.matcher(text).find();
    }

    @Override
    public String color(@NonNull String source) {
        // FIXME: built-in minimessage support???
        TextComponent deserialized = LegacyComponentSerializer.legacyAmpersand().deserialize(source);
        return LegacyComponentSerializer.legacySection().serialize(deserialized);
    }

    @Override
    protected Optional<MessageColors> matchColors(@NonNull String fieldName) {
        return this.getColorsConfig().getMatchers().stream()
            .filter(matcher -> matcher.getPattern().matcher(fieldName).matches())
            .map(matcher -> MessageColors.of(
                textColorToLegacySection(matcher.getMessageColor()),
                textColorToLegacySection(matcher.getFieldsColor())
            ))
            .findAny();
    }

    // mmm... legacy
    private static String textColorToLegacySection(@NonNull TextColor color) {
        Component component = MiniMessage.miniMessage().deserialize("<" + color + ">*");
        String legacy = LegacyComponentSerializer.legacySection().serialize(component);
        return legacy.substring(0, legacy.length() - 1); // remove * added before to force coloring at the end
    }
}
