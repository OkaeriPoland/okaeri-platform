package eu.okaeri.platform.bukkit.commons.i18n;

import eu.okaeri.configs.schema.ConfigDeclaration;
import eu.okaeri.configs.schema.FieldDeclaration;
import eu.okaeri.i18n.configs.LocaleConfig;
import eu.okaeri.i18n.configs.OCI18n;
import eu.okaeri.i18n.configs.impl.MOCI18n;
import eu.okaeri.i18n.message.Message;
import eu.okaeri.placeholders.message.CompiledMessage;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BI18n extends MOCI18n {

    private final Map<Locale, LocaleConfig> configs = new HashMap<>();

    @Override
    public OCI18n<CompiledMessage, Message> registerConfig(Locale locale, LocaleConfig config) {
        this.update(config);
        this.configs.put(locale, config);
        return super.registerConfig(locale, config);
    }

    public void load() {
        for (Map.Entry<Locale, LocaleConfig> entry : this.configs.entrySet()) {
            LocaleConfig config = entry.getValue();
            this.update(config);
            super.registerConfig(entry.getKey(), config);
        }
    }

    private void update(LocaleConfig config) {

        config.load();
        ConfigDeclaration declaration = config.getDeclaration();

        for (FieldDeclaration field : declaration.getFields()) {

            if (!(field.getValue() instanceof String)) {
                continue;
            }

            String fieldValue = String.valueOf(field.getValue());
            field.updateValue(ChatColor.translateAlternateColorCodes('&', fieldValue));
        }
    }
}
