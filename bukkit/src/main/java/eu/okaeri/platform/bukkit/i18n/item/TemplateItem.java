package eu.okaeri.platform.bukkit.i18n.item;

import eu.okaeri.i18n.message.Message;
import eu.okaeri.platform.bukkit.i18n.BI18n;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor(staticName = "of")
public class TemplateItem {

    private final Map<String, Object> fields = new LinkedHashMap<>();
    @NonNull private final ItemStack item;

    private BI18n i18n;
    private String nameKey;
    private String descriptionKey;

    public TemplateItem with(@NonNull String field, @NonNull Object value) {
        this.fields.put(field, value);
        return this;
    }

    public TemplateItem with(@NonNull Map<String, Object> additionalFields) {
        this.fields.putAll(additionalFields);
        return this;
    }

    public TemplateItem i18n(@NonNull BI18n i18n) {
        this.i18n = i18n;
        return this;
    }

    public TemplateItem nameKey(@NonNull String nameKey) {
        this.nameKey = nameKey;
        return this;
    }

    public TemplateItem descriptionKey(@NonNull String descriptionKey) {
        this.descriptionKey = descriptionKey;
        return this;
    }

    public ItemStack apply(Object entity) {

        ItemStack clone = this.item.clone();
        ItemMeta itemMeta = clone.getItemMeta();

        if (itemMeta == null) {
            return clone;
        }

        if (this.i18n != null && entity != null && this.nameKey != null) {
            String name = this.i18n.get(entity, this.nameKey).apply();
            itemMeta.setDisplayName(name);
        }

        if (itemMeta.getDisplayName() != null) {
            Message message = Message.of(itemMeta.getDisplayName());
            this.fields.forEach(message::with);
            itemMeta.setDisplayName(message.apply());
        }

        if (this.i18n != null && entity != null && this.descriptionKey != null) {
            String fullLore = this.i18n.get(entity, this.descriptionKey).apply();
            itemMeta.setLore(Arrays.asList(fullLore.split("\n")));
        }

        if (itemMeta.getLore() != null) {
            String fullLore = String.join("\n", itemMeta.getLore());
            Message message = Message.of(fullLore);
            this.fields.forEach(message::with);
            itemMeta.setLore(Arrays.asList(message.apply().split("\n")));
        }

        clone.setItemMeta(itemMeta);
        return clone;
    }

    public ItemStack apply() {
        return this.apply(null);
    }
}
