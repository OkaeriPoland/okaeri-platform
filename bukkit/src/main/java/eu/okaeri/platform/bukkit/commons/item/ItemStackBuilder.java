package eu.okaeri.platform.bukkit.commons.item;

import eu.okaeri.platform.bukkit.commons.UnsafeBukkitCommons;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemStackBuilder {

    private ItemStack itemStack;

    private ItemStackBuilder() {
    }

    private ItemStackBuilder(Material material, int amount) {
        if (material == null) throw new IllegalArgumentException("material cannot be null");
        this.itemStack = new ItemStack(material, amount);
    }

    public static ItemStackBuilder of(Material material) {
        return new ItemStackBuilder(material, 1);
    }

    public static ItemStackBuilder of(Material material, int amount) {
        return new ItemStackBuilder(material, amount);
    }

    public static ItemStackBuilder of(ItemStack item) {
        if (item == null) throw new IllegalArgumentException("item cannot be null");
        return ItemStackBuilder.of(item.getType(), item.getAmount())
                .withDurability(item.getDurability())
                .withOwnItemMeta(item.getItemMeta());
    }

    public static ItemStackBuilder ofCopy(ItemStack item) {
        if (item == null) throw new IllegalArgumentException("item cannot be null");
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder();
        itemStackBuilder.itemStack = item.clone();
        return itemStackBuilder;
    }

    public ItemStackBuilder withName(String displayName) {
        return this.withNameRaw(ChatColor.translateAlternateColorCodes('&', displayName));
    }

    public ItemStackBuilder withNameRaw(String displayName) {
        if (displayName == null) throw new IllegalArgumentException("displayName cannot be null");
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        itemMeta.setDisplayName(displayName);
        this.itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemStackBuilder withLore(String lore) {
        return this.withLore(Collections.singletonList(lore));
    }

    public ItemStackBuilder withLore(List<String> lore) {
        return this.withLoreRaw(lore.stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .collect(Collectors.toList()));
    }

    public ItemStackBuilder withLoreRaw(String lore) {
        return this.withLoreRaw(Collections.singletonList(lore));
    }

    public ItemStackBuilder withLoreRaw(List<String> lore) {
        if (lore == null) throw new IllegalArgumentException("lore cannot be null");
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        itemMeta.setLore(lore);
        this.itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemStackBuilder appendLoreRaw(List<String> lore) {
        if (lore == null) throw new IllegalArgumentException("lore cannot be null");
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        if (!itemMeta.hasLore()) {
            itemMeta.setLore(lore);
        } else {
            List<String> newLore = itemMeta.getLore();
            newLore.addAll(lore);
            itemMeta.setLore(newLore);
        }
        this.itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemStackBuilder appendLore(List<String> lore) {
        return this.appendLoreRaw(lore.stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .collect(Collectors.toList()));
    }

    public ItemStackBuilder appendLoreRaw(String line) {
        return this.appendLoreRaw(Collections.singletonList(line));
    }

    public ItemStackBuilder appendLore(String line) {
        return this.appendLore(Collections.singletonList(line));
    }

    public ItemStackBuilder withDurability(int durability) {
        this.itemStack.setDurability((short) durability);
        return this;
    }

    public ItemStackBuilder withFlag(ItemFlag itemFlag) {
        if (itemFlag == null) throw new IllegalArgumentException("itemFlag cannot be null");
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        itemMeta.addItemFlags(itemFlag);
        this.itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemStackBuilder withEnchantment(Enchantment enchantment, int level) {
        if (enchantment == null) throw new IllegalArgumentException("enchantment cannot be null");
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        itemMeta.addEnchant(enchantment, level, true);
        this.itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemStackBuilder withEnchantments(Map<Enchantment, Integer> enchantments) {
        if (enchantments == null) throw new IllegalArgumentException("enchantments cannot be null");
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        enchantments.forEach((enchantment, level) -> itemMeta.addEnchant(enchantment, level, true));
        this.itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemStackBuilder withOwnItemMeta(ItemMeta itemMeta) {
        if (itemMeta == null) throw new IllegalArgumentException("itemMeta cannot be null");
        this.itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemStackBuilder makeUnbreakable() {
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        UnsafeBukkitCommons.setItemMetaUnbreakable(itemMeta, true);
        this.itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemStackBuilder manipulate(ItemStackManipulator manipulator) {
        if (manipulator == null) throw new IllegalArgumentException("manipulator cannot be null");
        this.itemStack = manipulator.manipulate(this.itemStack);
        return this;
    }

    public ItemStack get() {
        return this.itemStack;
    }
}