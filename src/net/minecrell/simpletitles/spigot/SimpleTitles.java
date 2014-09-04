package net.minecrell.simpletitles.spigot;

import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.minecraft.server.v1_7_R4.ChatComponentText;
import net.minecraft.server.v1_7_R4.ChatSerializer;
import net.minecraft.server.v1_7_R4.Packet;
import org.spigotmc.ProtocolInjector.PacketTitle;
import org.spigotmc.ProtocolInjector.PacketTitle.Action;

public class SimpleTitles extends JavaPlugin {
    public static final int TITLE_PROTOCOL_VERSION = 18; // 14w20a

    private static final Joiner MESSAGE_JOINER = Joiner.on(' ');
    private static final Pattern MESSAGE_PATTERN = Pattern.compile("\"((?:\\\\\"|[^\"])*)\"");

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (isPlayer(sender) && !hasTitleSupport(sender)) {
            sender.sendMessage(ChatColor.RED + "You need to use Minecraft 1.8 for this command!");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("title")) {
            vanillaTitle(sender, args);
        } else if (cmd.getName().equalsIgnoreCase("simpletitle")) {
            return simpleTitle(sender, args);
        }

        return true;
    }

    private void vanillaTitle(CommandSender sender, String[] args) {
        if (args.length < 2) {
            Messages.sendUsage(sender, "commands.title.usage");
            return;
        }

        Player player = null;
        if (!args[0].equals("@a")) {
            player = getServer().getPlayerExact(args[0]);
            if (player == null) {
                Messages.sendError(sender, "commands.generic.player.notFound");
                return;
            }
        }

        Action action = Action.valueOf(args[1].toUpperCase(Locale.ENGLISH));
        if (action == null) {
            Messages.sendUsage(sender, "commands.title.usage");
            return;
        }

        PacketTitle packet;

        switch (action) {
            case TITLE:
            case SUBTITLE:
                if (args.length == 2) {
                    Messages.sendUsage(sender, "commands.title.usage.title");
                    return;
                }

                try {
                    packet = new PacketTitle(action, ChatSerializer.a(args[2]));
                } catch (Exception e) {
                    Messages.sendError(sender, "commands.generic.parameter.invalid", args[2]);
                    getLogger().log(Level.WARNING, "Unable to parse JSON from command: " + args[2], e);
                    return;
                }

                break;
            case TIMES:
                if (args.length < 5) {
                    Messages.sendUsage(sender, "commands.title.usage.times");
                    return;
                }

                Integer fadeIn, stay, fadeOut;
                if ((fadeIn = parseTimeVanilla(sender, args[2])) == null) return;
                if ((stay = parseTimeVanilla(sender, args[3])) == null) return;
                if ((fadeOut = parseTimeVanilla(sender, args[4])) == null) return;

                packet = new PacketTitle(action, fadeIn, stay, fadeOut);
                break;
            default:
                packet = new PacketTitle(action);
        }

        sendPacket(player, packet);
        Messages.sendTranslated(sender, "commands.title.success");
    }

    private static Integer parseTimeVanilla(CommandSender sender, String time) {
        try {
            return Integer.valueOf(time);
        } catch (NumberFormatException e) {
            Messages.sendError(sender, "commands.generic.num.invalid", time);
            return null;
        }
    }

    private boolean simpleTitle(CommandSender sender, String[] args) {
        if (args.length == 0) return false;

        int start = 0, end = args.length;

        Player player = null;
        if (args[0].indexOf('"') == -1) {
            if (!args[0].equalsIgnoreCase("@a")) {
                player = getServer().getPlayer(args[0]);
                if (player == null) {
                    sender.sendMessage(ChatColor.RED + "Unknown player: " + args[0]);
                    return true;
                }
            }

            start++;
        }

        Integer fadeIn = null, stay = null, fadeOut = null;
        if (end - start >= 3) {
            for (int i = 1; i <= 3; i++) {
                String time = args[end - i];
                if (time.indexOf('"') == -1) {
                    switch (i) {
                        case 3:
                            if ((fadeIn = parseTime(sender, time)) == null) return true;
                            break;
                        case 2:
                            if ((stay = parseTime(sender, time)) == null) return true;
                            break;
                        case 1:
                            if ((fadeOut = parseTime(sender, time)) == null) return true;
                            break;
                    }
                } else break;
            }

            if (fadeIn != null) {
                end -= 3;
                sendPacket(player, new PacketTitle(Action.TIMES, fadeIn, stay, fadeOut));
            }

        }

        String input = MESSAGE_JOINER.join(Arrays.copyOfRange(args, start, end));
        Matcher matcher = MESSAGE_PATTERN.matcher(input);
        if (matcher.find()) {
            sendPacket(player, new PacketTitle(Action.RESET));
            sendPacket(player, new PacketTitle(Action.TITLE,
                    new ChatComponentText(ChatColor.translateAlternateColorCodes('&', matcher.group(1)))));

            if (matcher.find()) {
                sendPacket(player, new PacketTitle(Action.SUBTITLE,
                        new ChatComponentText(ChatColor.translateAlternateColorCodes('&', matcher.group(1)))));
            }
        }



        return true;
    }

    private static Integer parseTime(CommandSender sender, String time) {
        try {
            return Integer.valueOf(time);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid time: " + time);
            return null;
        }
    }

    private void sendPacket(Player player, Packet packet) {
        if (packet == null) return;
        if (player == null) {
            for (Player p : getServer().getOnlinePlayers())
                sendPacket(p, packet);
        } else if (hasTitleSupport(player)) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    private static boolean isPlayer(CommandSender sender) {
        return sender instanceof Player;
    }

    public static boolean hasTitleSupport(CommandSender sender) {
        return sender instanceof CraftPlayer &&
                ((CraftPlayer) sender).getHandle().playerConnection.networkManager.getVersion()
                        >= TITLE_PROTOCOL_VERSION;
    }
}
