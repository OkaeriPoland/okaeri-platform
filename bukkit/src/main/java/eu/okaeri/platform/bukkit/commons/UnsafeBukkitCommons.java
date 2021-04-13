package eu.okaeri.platform.bukkit.commons;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class UnsafeBukkitCommons {

    //
    // NMS / REFLECTIONS
    //
    @Getter private static String nmsv;
    private static boolean legacy18o17;
    private static MethodHandles.Lookup lookup = MethodHandles.lookup();

    static {
        nmsv = Bukkit.getServer().getClass().getPackage().getName();
        nmsv = nmsv.substring(nmsv.lastIndexOf(".") + 1);
        legacy18o17 = ("v1_8_R1".equalsIgnoreCase(nmsv) || nmsv.startsWith("v1_7_"));
    }

    private static Class<?> getNmsClass(String pattern) {
        try {
            return Class.forName(pattern.replace("{nms}", nmsv));
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    private static MethodHandle getMHFrom(Class<?> clazz, String name, Class<?>... params) {
        try {
            Method method = clazz.getDeclaredMethod(name, params);
            return lookup.unreflect(method);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static MethodHandle getMHGFrom(Class<?> clazz, String name) {
        try {
            Field method = clazz.getDeclaredField(name);
            return lookup.unreflectGetter(method);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Class<?> CraftPlayer = getNmsClass("org.bukkit.craftbukkit.{nms}.entity.CraftPlayer");
    private static Class<?> EntityPlayer = getNmsClass("net.minecraft.server.{nms}.EntityPlayer");
    private static Class<?> PlayerConnection = getNmsClass("net.minecraft.server.{nms}.PlayerConnection");
    private static Class<?> PacketPlayOutChat = getNmsClass("net.minecraft.server.{nms}.PacketPlayOutChat");
    private static Class<?> Packet = getNmsClass("net.minecraft.server.{nms}.Packet");
    private static Class<?> IChatBaseComponent = getNmsClass("net.minecraft.server.{nms}.IChatBaseComponent");
    private static Class<?> ChatComponentText = getNmsClass("net.minecraft.server.{nms}.ChatComponentText");
    private static Class<?> ChatSerializer = getNmsClass("net.minecraft.server.{nms}.ChatSerializer");
    private static Class<?> BCTextComponent = getNmsClass("net.md_5.bungee.api.chat.TextComponent");
    private static Class<?> BCChatMessageType = getNmsClass("net.md_5.bungee.api.ChatMessageType");
    private static Class<?> BCBaseComponentArray = getNmsClass("[Lnet.md_5.bungee.api.chat.BaseComponent;");
    private static Class<?> BCBaseComponent = getNmsClass("net.md_5.bungee.api.chat.BaseComponent");
    private static Class<?> PlayerSpigot = getNmsClass("org.bukkit.entity.Player$Spigot");

    private static MethodHandle CraftPlayerGetHandle = getMHFrom(CraftPlayer, "getHandle");
    private static MethodHandle ItemMetaSetUnbreakable = getMHFrom(ItemMeta.class, "setUnbreakable", boolean.class);
    private static MethodHandle EntityPlayerPlayerConnection = getMHGFrom(EntityPlayer, "playerConnection");
    private static MethodHandle EntityPlayerLocale = getMHGFrom(EntityPlayer, "locale");
    private static MethodHandle PlayerConnectionSendPacket = getMHFrom(PlayerConnection, "sendPacket", Packet);
    private static MethodHandle BCTextComponentFromLegacyText = getMHFrom(BCTextComponent, "fromLegacyText", String.class);
    private static MethodHandle BCChatMessageTypeValueOf = getMHFrom(BCChatMessageType, "valueOf", String.class);
    private static MethodHandle PlayerGetSpigot = getMHFrom(Player.class, "spigot");
    private static MethodHandle PlayerGetLocale = getMHFrom(Player.class, "getLocale");
    private static MethodHandle PlayerSpigotSendMessageBA = getMHFrom(PlayerSpigot, "sendMessage", BCBaseComponentArray);
    private static MethodHandle PlayerSpigotSendMessageTBA = getMHFrom(PlayerSpigot, "sendMessage", BCChatMessageType, BCBaseComponentArray);

    //
    // ITEMMETA: UNBREAKABLE
    //
    @SneakyThrows
    public static void setItemMetaUnbreakable(ItemMeta itemMeta, boolean state) {

        if (ItemMetaSetUnbreakable != null) {
            ItemMetaSetUnbreakable.bindTo(itemMeta).invoke(state);
            return;
        }

        itemMeta.spigot().setUnbreakable(state);
    }

    //
    // LOCALE HELPERS
    //
    @SneakyThrows
    public static String getLocaleString(Player player) {

        if (PlayerGetLocale != null) {
            return (String) PlayerGetLocale.bindTo(player).invoke();
        }

        Object handle = CraftPlayerGetHandle.bindTo(player).invoke();
        return (String) EntityPlayerLocale.bindTo(handle).invoke();
    }

    //
    // TEXT COMPONENTS / MESSAGES
    //
    @SneakyThrows
    public static Object toBaseComponentArray(String message) {
        return BCTextComponentFromLegacyText.invoke(message);
    }

    @SneakyThrows
    public static void sendComponent(Player player, Object message, ChatTarget target) {

        if ((message == null) || !player.isOnline()) {
            return;
        }

        if (BCBaseComponent.isInstance(message)) {
            Object[] arr = (Object[]) Array.newInstance(BCBaseComponent, 1);
            arr[0] = message;
            message = arr;
        }

        if (!BCBaseComponentArray.isInstance(message)) {
            throw new IllegalArgumentException("message not instance of " + BCBaseComponentArray + ": " + message);
        }

        if (((target == ChatTarget.CHAT) || (target == ChatTarget.SYSTEM)) && (PlayerSpigotSendMessageBA != null)) {
            PlayerSpigotSendMessageBA.bindTo(PlayerGetSpigot.invoke(player)).invoke(message);
            return;
        }

        if (PlayerSpigotSendMessageTBA != null) {
            PlayerSpigotSendMessageTBA.bindTo(PlayerGetSpigot.invoke(player)).invoke(BCChatMessageTypeValueOf.invoke(target.name()), message);
            return;
        }

        throw new RuntimeException("cannot send component (" + player.getName() + ", " + target.name() + "): " + message);
    }

    @SneakyThrows
    public static void sendMessage(Player player, String message, ChatTarget target) {

        if ((message == null) || !player.isOnline()) {
            return;
        }

        if (((target == ChatTarget.CHAT) || (target == ChatTarget.SYSTEM)) && (PlayerSpigotSendMessageBA != null)) {
            PlayerSpigotSendMessageBA.bindTo(PlayerGetSpigot.invoke(player)).invoke(toBaseComponentArray(message));
            return;
        }

        if (PlayerSpigotSendMessageTBA != null) {
            PlayerSpigotSendMessageTBA.bindTo(PlayerGetSpigot.invoke(player)).invoke(BCChatMessageTypeValueOf.invoke(target.name()), toBaseComponentArray(message));
            return;
        }

        // partial support for 1.8/1.7.10
        Object component = createPacketComponent(message);
        Object packet = PacketPlayOutChat.getConstructor(new Class<?>[]{IChatBaseComponent, byte.class}).newInstance(component, target.position);
        Object handle = CraftPlayerGetHandle.invoke(CraftPlayer.cast(player));
        Object connection = EntityPlayerPlayerConnection.invoke(handle);
        PlayerConnectionSendPacket.invoke(connection, packet);
    }

    @SneakyThrows
    private static Object createPacketComponent(String message) {

        if (legacy18o17) {
            return IChatBaseComponent.cast(ChatSerializer.getDeclaredMethod("a", String.class).invoke(ChatSerializer, "{\"text\": \"" + message + "\"}"));
        }

        return ChatComponentText.getConstructor(new Class<?>[]{String.class}).newInstance(message);
    }

    @RequiredArgsConstructor
    public enum ChatTarget {

        CHAT((byte) 0),
        SYSTEM((byte) 1),
        ACTION_BAR((byte) 2);

        final byte position;
    }
}
