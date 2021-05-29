package eu.okaeri.platform.bukkit.commons.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.*;

/**
 * CommandRunner runner = CommandRunner.of(Bukkit.getOnlinePlayers())
 *   .field("{name}", (target) -> target.getName())
 *   .execute("say {name}");
 */
public class CommandRunner<T> {

    private Collection<? extends T> targets;
    private boolean forceMainThread = true;
    private CommandSender dispatcher = null;
    private Map<String, String> fields = new LinkedHashMap<>();
    private Map<String, CommandFieldReplacer<T>> dynamicFields = new LinkedHashMap<>();
    private Plugin plugin;

    public static CommandRunner<?> of(Plugin plugin) {
        return of(plugin, Bukkit.getConsoleSender());
    }

    public static <T> CommandRunner<T> of(Plugin plugin, T target) {
        return new CommandRunner<T>(plugin, Collections.singletonList(target));
    }

    public static <T> CommandRunner<T> of(Plugin plugin, Collection<? extends T> targets) {
        return new CommandRunner<T>(plugin, new ArrayList<>(targets));
    }

    private CommandRunner(Plugin plugin, Collection<? extends T> targets) {
        this.plugin = plugin;
        this.targets = targets;
    }

    public CommandRunner<T> field(String name, String content) {
        this.fields.put(name, content);
        return this;
    }

    public CommandRunner<T> field(String name, CommandFieldReplacer<T> fieldReplacer) {
        this.dynamicFields.put(name, fieldReplacer);
        return this;
    }

    public CommandRunner<T> forceMainThread(boolean state) {
        this.forceMainThread = state;
        return this;
    }

    public CommandRunner<T> dispatcher(CommandSender sender) {
        this.dispatcher = sender;
        return this;
    }

    public CommandRunner<T> execute(String command) {
        return this.execute(Collections.singletonList(command));
    }

    public CommandRunner<T> execute(List<String> commands) {

        if (commands.isEmpty()) {
            return this;
        }

        for (T target : this.targets) {
            commands.stream()
                    .map(command -> {
                        for (Map.Entry<String, String> entry : this.fields.entrySet()) {
                            String fieldName = entry.getKey();
                            String fieldValue = entry.getValue();
                            command = command.replace("{" + fieldName + "}", fieldValue);
                        }
                        return command;
                    })
                    .map(command -> {
                        for (Map.Entry<String, CommandFieldReplacer<T>> replacerEntry : this.dynamicFields.entrySet()) {
                            String fieldName = replacerEntry.getKey();
                            CommandFieldReplacer<T> replacer = replacerEntry.getValue();
                            command = command.replace("{" + fieldName + "}", replacer.replace(target));
                        }
                        return command;
                    })
                    .forEachOrdered(command -> {
                        CommandSender whoDispatches = (this.dispatcher == null) ? Bukkit.getConsoleSender() : this.dispatcher;
                        if (this.forceMainThread) {
                            if (Bukkit.isPrimaryThread()) {
                                Bukkit.dispatchCommand(whoDispatches, command);
                            } else {
                                Bukkit.getScheduler().runTask(this.plugin, () -> Bukkit.dispatchCommand(whoDispatches, command));
                            }
                        } else {
                            Bukkit.dispatchCommand(whoDispatches, command);
                        }
                    });
        }

        return this;
    }
}