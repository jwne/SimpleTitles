# SimpleTitles

SimpleTitles adds 2 new commands to your server allowing you to send the new titles introduced in Minecraft 1.8 to your players easily! You can create titles using the `/title` command like in Vanilla and there is also a simpler command allowing you to create them with only one command.

The plugin works on both Spigot and BungeeCord servers. For Spigot you need at least build #1647 and for BungeeCord you need at least build #996.

## Commands
The permission for all commands is `simpletitles.use`.

1.  `/title` (`/gtitle` for BungeeCord) - This plugin works like in Vanilla, has the same messages and everything else is the same. There is an explanation how to use this command in the [Minecraft Wiki](http://minecraft.gamepedia.com/Commands#title).
2.  `/stitle` - This command is actually the simpler one. It supports color codes and you don't need to send multiple commands just for a simple title! Here are the arguments for the command:

    1.  _Optional_: The player name if you want to send it to one player only.
    2.  The title and sub title (_optional_) as a quoted message. That means you need to add double quotes around the message. You can create colors using the color codes (`&`).
    3.  _Optional_: 3 display times: one number for the amount of ticks (1/20 seconds) for the fade in effect, how long it should stay on the screen and one number for the duration of the fade out effect.

### Example
Here are some examples of the simple title command:

- `/stitle "&aHello!" "&eHow are you?"` - Send a title and sub title with the default display times to all players on your server.
- `/stitle Minecrell "Title" "Sub title" 40 600 40` - Send a title and sub title only to `Minecrell` and fade in for 2 seconds, stay for 30 seconds and fade out for 40 seconds.

## Source
The plugin is open source and released under the terms and conditions of the [GNU General Public License](http://www.gnu.org/licenses/gpl-3.0). You are free to redistribute and/or modify it to your likings, but please add a link to the [GitHub Page](https://github.com/Minecrell/SimpleTitles) and redistribute it under a compatible license.
