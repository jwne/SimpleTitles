package net.minecrell.simpletitles.spigot;

import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;

import net.minecraft.server.v1_7_R4.ChatMessage;
import net.minecraft.server.v1_7_R4.ChatModifier;
import net.minecraft.server.v1_7_R4.EnumChatFormat;
import net.minecraft.server.v1_7_R4.IChatBaseComponent;

public final class Messages {
    private Messages() {}

    private static IChatBaseComponent getTranslated(String translation, Object... args) {
        return new ChatMessage(translation, args);
    }

    private static IChatBaseComponent getError(String translation, Object... args) {
        IChatBaseComponent message = getTranslated(translation, args);

        ChatModifier color = new ChatModifier();
        color.setColor(EnumChatFormat.RED);
        message.setChatModifier(color);

        return message;
    }

    private static IChatBaseComponent getUsage(String translation, Object... args) {
        return getError("commands.generic.usage", getTranslated(translation, args));
    }

    public static void sendTranslated(CommandSender sender, String translation, Object... args) {
        send(sender, getTranslated(translation, args));
    }

    public static void sendError(CommandSender sender, String translation, Object... args) {
        send(sender, getError(translation, args));
    }

    public static void sendUsage(CommandSender sender, String translation, Object... args) {
        send(sender, getUsage(translation, args));
    }

    private static void send(CommandSender sender, IChatBaseComponent message) {
        if (sender instanceof CraftPlayer) {
            ((CraftPlayer) sender).getHandle().sendMessage(message);
        } else {
            sender.sendMessage(message.c());
        }
    }
}
