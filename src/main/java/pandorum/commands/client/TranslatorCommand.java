package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Player;
import pandorum.data.PlayerData;

import static pandorum.PluginVars.translatorLocales;
import static pandorum.data.Database.getPlayerData;
import static pandorum.data.Database.setPlayerData;
import static pandorum.util.PlayerUtils.bundled;

public class TranslatorCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        PlayerData data = getPlayerData(player.uuid());

        if (args.length == 0 || args[0].equalsIgnoreCase("current")) {
            bundled(player, "commands.tr.current", data.locale);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "list" -> {
                StringBuilder locales = new StringBuilder();
                translatorLocales.keys().toSeq().each(locale -> locales.append(locale).append(" "));
                bundled(player, "commands.tr.list", locales.toString());
            }
            case "off" -> {
                data.locale = "off";
                setPlayerData(player.uuid(), data);
                bundled(player, "commands.tr.disabled");
            }
            case "auto" -> {
                data.locale = "auto";
                setPlayerData(player.uuid(), data);
                bundled(player, "commands.tr.auto");
            }
            default -> {
                if (!translatorLocales.containsKey(args[0])) {
                    bundled(player, "commands.tr.incorrect");
                    return;
                }

                data.locale = args[0];
                setPlayerData(player.uuid(), data);
                bundled(player, "commands.tr.changed", args[0], translatorLocales.get(args[0]));
            }
        }
    }
}
