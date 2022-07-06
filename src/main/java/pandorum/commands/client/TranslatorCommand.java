package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.components.Bundle;
import pandorum.data.PlayerData;

import static pandorum.PluginVars.translatorLanguages;
import static pandorum.PluginVars.mindustryLocales2Api;
import static pandorum.data.Database.getPlayerData;
import static pandorum.data.Database.setPlayerData;
import static pandorum.util.PlayerUtils.bundled;
import static pandorum.util.Search.findLocale;

public class TranslatorCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            StringBuilder result = new StringBuilder(Bundle.format("commands.tr.list", findLocale(player.locale)));
            translatorLanguages.each((language, name) -> result.append("[cyan]").append(language).append(" [accent](").append(name).append(")\n"));
            Call.infoMessage(player.con, result.toString());
            return;
        }

        PlayerData data = getPlayerData(player.uuid());
        switch (args[0].toLowerCase()) {
            case "current" -> bundled(player, "commands.tr.current", data.language);
            case "off" -> {
                data.language = "off";
                setPlayerData(player.uuid(), data);
                bundled(player, "commands.tr.disabled");
            }
            case "auto" -> {
                data.language = mindustryLocales2Api.get(player.locale);
                setPlayerData(player.uuid(), data);
                bundled(player, "commands.tr.changed"); // TODO: дарк, вставь сюда нужные аргументы. Chat language set to {0} ({1}).
            }
            default -> {
                if (!translatorLanguages.containsKey(args[0])) {
                    bundled(player, "commands.tr.not-found");
                    return;
                }

                data.language = args[0];
                setPlayerData(player.uuid(), data);
                bundled(player, "commands.tr.changed", args[0], translatorLanguages.get(args[0]));
            }
        }
    }
}

