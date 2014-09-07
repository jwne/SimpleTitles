package net.minecrell.simpletitles.bungee;

import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.chat.ComponentSerializer;

public class SimpleTitles extends Plugin {
    public static final int TITLE_PROTOCOL_VERSION = 18; // 14w20a

    private static final Joiner MESSAGE_JOINER = Joiner.on(' ');
    private static final Pattern MESSAGE_PATTERN = Pattern.compile("\"((?:\\\\\"|[^\"])*)\"");

    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerCommand(this, new VanillaCommand());
        getProxy().getPluginManager().registerCommand(this, new SimpleCommand());
    }

    private static enum TitleAction {
        TITLE,
        SUBTITLE,
        TIMES,
        CLEAR,
        RESET
    }

    public class VanillaCommand extends Command {
        public VanillaCommand() {
            super("gtitle", "simpletitles.use");
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            if (!checkPlayer(sender)) return;
            if (args.length < 2) {
                sender.sendMessage(Messages.getUsage("commands.title.usage"));
                return;
            }

            ProxiedPlayer player = null;
            if (!args[0].equals("@a")) {
                player = getProxy().getPlayer(args[0]);
                if (player == null) {
                    sender.sendMessage(Messages.getError("commands.generic.player.notFound"));
                    return;
                }
            }

            TitleAction action = TitleAction.valueOf(args[1].toUpperCase(Locale.ENGLISH));
            if (action == null) {
                sender.sendMessage(Messages.getUsage("commands.title.usage"));
                return;
            }

            Title title = getProxy().createTitle();
            switch (action) {
                case TITLE:
                case SUBTITLE:
                    if (args.length == 2) {
                        sender.sendMessage(Messages.getUsage("commands.title.usage.title"));
                        return;
                    }

                    BaseComponent[] text;
                    try {
                        text = ComponentSerializer.parse(args[2]);
                    } catch (Exception e) {
                        sender.sendMessage(Messages.getError("commands.generic.parameter.invalid", args[2]));
                        getLogger().log(Level.WARNING, "Unable to parse JSON from command: " + args[2], e);
                        return;
                    }

                    if (action == TitleAction.TITLE)
                        title.title(text);
                    else title.subTitle(text);
                    break;
                case TIMES:
                    if (args.length < 5) {
                        sender.sendMessage(Messages.getUsage("commands.title.usage.times"));
                        return;
                    }

                    Integer fadeIn, stay, fadeOut;
                    if ((fadeIn = parseTimeVanilla(sender, args[2])) == null) return;
                    if ((stay = parseTimeVanilla(sender, args[3])) == null) return;
                    if ((fadeOut = parseTimeVanilla(sender, args[4])) == null) return;

                    title.fadeIn(fadeIn).stay(stay).fadeOut(fadeOut);
                    break;
                case CLEAR:
                    title.clear();
                    break;
                case RESET:
                    title.reset();
                    break;
            }

            sendTitle(player, title);
            sender.sendMessage(Messages.getTranslated("commands.title.success"));
        }
    }

    private static Integer parseTimeVanilla(CommandSender sender, String time) {
        try {
            return Integer.valueOf(time);
        } catch (NumberFormatException e) {
            sender.sendMessage(Messages.getError("commands.generic.num.invalid", time));
            return null;
        }
    }

    public class SimpleCommand extends Command {

        public SimpleCommand() {
            super("simpletitle", "simpletitles.use", "stitle");
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            if (!checkPlayer(sender)) return;
            if (args.length == 0) {
                sender.sendMessage(new ComponentBuilder("Usage: /stitle [player/@a] [\"title\"] " +
                        "[\"subtitle\"] [fadeIn stay fadeOut]").color(ChatColor.RED).create());
                return;
            }

            int start = 0, end = args.length;

            ProxiedPlayer player = null;
            if (args[0].indexOf('"') == -1) {
                if (!args[0].equalsIgnoreCase("@a")) {
                    player = getProxy().getPlayer(args[0]);
                    if (player == null) {
                        sender.sendMessage(new ComponentBuilder("Unknown player: " + args[0]).color(ChatColor
                                .RED).create());
                        return;
                    }
                }

                start++;
            }

            Title title = getProxy().createTitle().reset();

            Integer fadeIn = null, stay = null, fadeOut = null;
            if (end - start >= 3) {
                for (int i = 1; i <= 3; i++) {
                    String time = args[end - i];
                    if (time.indexOf('"') == -1) {
                        switch (i) {
                            case 3:
                                if ((fadeIn = parseTime(sender, time)) == null) return;
                                break;
                            case 2:
                                if ((stay = parseTime(sender, time)) == null) return;
                                break;
                            case 1:
                                if ((fadeOut = parseTime(sender, time)) == null) return;
                                break;
                        }
                    } else break;
                }

                if (fadeIn != null) {
                    end -= 3;
                    title.fadeIn(fadeIn).stay(stay).fadeOut(fadeOut);
                }

            }

            String input = MESSAGE_JOINER.join(Arrays.copyOfRange(args, start, end));
            Matcher matcher = MESSAGE_PATTERN.matcher(input);
            if (matcher.find()) {
                title.title(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',
                        matcher.group(1))));

                if (matcher.find()) {
                    title.subTitle(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',
                            matcher.group(1))));
                }
            }

            sendTitle(player, title);
        }
    }

    private static Integer parseTime(CommandSender sender, String time) {
        try {
            return Integer.valueOf(time);
        } catch (NumberFormatException e) {
            sender.sendMessage(new ComponentBuilder("Invalid time: " + time).color(ChatColor.RED).create());
            return null;
        }
    }

    private static boolean checkPlayer(CommandSender sender) {
        if (isPlayer(sender) && !hasTitleSupport(sender)) {
            sender.sendMessage(new ComponentBuilder("You need to use Minecraft 1.8 for this command!")
                    .color(ChatColor.RED).create());
            return false;
        }

        return true;
    }

    private void sendTitle(ProxiedPlayer player, Title title) {
        if (title == null) return;
        if (player == null) {
            for (ProxiedPlayer p : getProxy().getPlayers())
                sendTitle(p, title);
        } else {
            title.send(player);
        }
    }

    private static boolean isPlayer(CommandSender sender) {
        return sender instanceof ProxiedPlayer;
    }

    public static boolean hasTitleSupport(CommandSender sender) {
        return isPlayer(sender) && ((ProxiedPlayer) sender).getPendingConnection().getVersion()
                >= TITLE_PROTOCOL_VERSION;
    }
}
