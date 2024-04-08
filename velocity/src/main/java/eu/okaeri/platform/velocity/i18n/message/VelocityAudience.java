//package eu.okaeri.platform.velocity.i18n.message;
//
//import com.velocitypowered.api.proxy.ProxyServer;
//import eu.okaeri.i18n.message.Message;
//import eu.okaeri.platform.core.i18n.message.Audience;
//import lombok.NonNull;
//import net.md_5.bungee.api.ProxyServer;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.stream.Collectors;
//
//public class VelocityAudience extends Audience<Message> {
//
//    protected VelocityAudience() {
//        this(new ArrayList<>());
//    }
//
//    protected VelocityAudience(Collection<Object> targets) {
//        super(targets);
//    }
//
//    public static VelocityAudience of(@NonNull Object... targets) {
//        return (targets.length == 0)
//            ? new VelocityAudience()
//            : new VelocityAudience(new ArrayList<>(Arrays.asList(targets)));
//    }
//
//    public static VelocityAudience of(@NonNull Collection<Object> targets) {
//        return new VelocityAudience(targets);
//    }
//
//    @Override
//    public VelocityAudience andOf(@NonNull Object... targets) {
//        this.targets.addAll(Arrays.asList(targets));
//        return this;
//    }
//
//    @Override
//    public VelocityAudience andOf(@NonNull Collection<Object> targets) {
//        this.targets.addAll(targets);
//        return this;
//    }
//
//    public static VelocityAudience ofAllPlayers() {
//        return new VelocityAudience().andOfAllPlayers();
//    }
//
//    public VelocityAudience andOfAllPlayers() {
//        this.targets.addAll(ProxyServer.getInstance().getPlayers());
//        return this;
//    }
//
//    public static VelocityAudience ofPlayersWithPermission(@NonNull String permission) {
//        return new VelocityAudience().andOfPlayersWithPermission(permission);
//    }
//
//    public VelocityAudience andOfPlayersWithPermission(@NonNull String permission) {
//        this.targets.addAll(new ArrayList<>(ProxyServer.getInstance().getPlayers()).stream()
//            .filter(player -> player.hasPermission(permission))
//            .collect(Collectors.toList()));
//        return this;
//    }
//
//    public static VelocityAudience ofConsole() {
//        return new VelocityAudience().andOfConsole();
//    }
//
//    public VelocityAudience andOfConsole() {
//        this.targets.add(ProxyServer.getInstance().getConsole());
//        return this;
//    }
//}
