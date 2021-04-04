package eu.okaeri.platform.bukkit.commons.command;

public interface CommandFieldReplacer<T> {
    String replace(T target);
}
