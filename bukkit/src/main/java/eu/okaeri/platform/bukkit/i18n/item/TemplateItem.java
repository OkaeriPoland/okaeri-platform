package eu.okaeri.platform.bukkit.i18n.item;

import eu.okaeri.i18n.message.Message;
import eu.okaeri.platform.bukkit.i18n.BI18n;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@RequiredArgsConstructor(staticName = "of")
public class TemplateItem {

    private final Map<String, Object> fields = new LinkedHashMap<>();
    @NonNull private final ItemStack item;

    public static TemplateItem of(@NonNull Material material) {
        return of(new ItemStack(material));
    }

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
            Message message = this.i18n.get(entity, this.nameKey);
            this.fields.forEach(message::with);
            itemMeta.setDisplayName(message.apply());
        }
        else if (itemMeta.getDisplayName() != null) {
            Locale fallbackLocale = (this.i18n == null || entity == null) ? Locale.ENGLISH : this.i18n.getLocale(entity);
            Message message = Message.of(fallbackLocale, itemMeta.getDisplayName());
            this.fields.forEach(message::with);
            itemMeta.setDisplayName(message.apply());
        }

        if (this.i18n != null && entity != null && this.descriptionKey != null) {
            Message message = this.i18n.get(entity, this.descriptionKey);
            this.fields.forEach(message::with);
            itemMeta.setLore(Arrays.asList(message.apply().split("\n")));
        }
        else if (itemMeta.getLore() != null) {
            Locale fallbackLocale = (this.i18n == null || entity == null) ? Locale.ENGLISH : this.i18n.getLocale(entity);
            Message message = Message.of(fallbackLocale, String.join("\n", itemMeta.getLore()));
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
