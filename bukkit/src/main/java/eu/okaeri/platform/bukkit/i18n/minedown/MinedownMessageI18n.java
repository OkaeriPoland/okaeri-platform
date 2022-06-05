package eu.okaeri.platform.bukkit.i18n.minedown;

import eu.okaeri.placeholders.Placeholders;
import eu.okaeri.placeholders.message.CompiledMessage;
import eu.okaeri.platform.minecraft.i18n.I18nPrefixProvider;
import eu.okaeri.platform.minecraft.i18n.base.MinecraftI18n;
import eu.okaeri.validator.annotation.Nullable;
import lombok.NonNull;

import java.util.Locale;

public abstract class MinedownMessageI18n extends MinecraftI18n<ComponentMessage> {

    public MinedownMessageI18n(String prefixField, String prefixMarker, Placeholders placeholders, I18nPrefixProvider prefixProvider) {
        super(prefixField, prefixMarker, placeholders, prefixProvider);
    }

    public MinedownMessageI18n(String prefixField, String prefixMarker) {
        super(prefixField, prefixMarker);
    }

    @Override
    public CompiledMessage storeConfigValue(@NonNull Locale locale, @NonNull Object value) {
        return CompiledMessage.of(locale, String.valueOf(value));
    }

    @Override
    public ComponentMessage createMessageFromStored(@Nullable CompiledMessage object, @NonNull String key) {
        if (object == null) {
            return ComponentMessage.of("<" + key + ">");
        }
        return ComponentMessage.of(this.placeholders, object);
    }

    @Override
    public ComponentMessage get(@NonNull Object entity, @NonNull String key) {

        ComponentMessage message = super.get(entity, key);
        String raw = message.raw();

        if (raw.startsWith(this.getPrefixMarker()) && (this.getPrefixProvider() != null)) {
            raw = raw.substring(this.getPrefixMarker().length());
            String prefix = this.getPrefixProvider().getPrefix(entity, key);
            return ComponentMessage.of(this.getPlaceholders(), prefix + raw);
        }

        return message;
    }
}
