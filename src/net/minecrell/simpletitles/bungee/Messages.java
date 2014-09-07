package net.minecrell.simpletitles.bungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;

public final class Messages {
    private Messages() {}

    public static BaseComponent getTranslated(String translation, Object... args) {
        return new TranslatableComponent(translation, args);
    }

    public static BaseComponent getError(String translation, Object... args) {
        BaseComponent message = getTranslated(translation, args);
        message.setColor(ChatColor.RED);
        return message;
    }

    public static BaseComponent getUsage(String translation, Object... args) {
        return getError("commands.generic.usage", getTranslated(translation, args));
    }
}
