package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.components.Bundle;
import pandorum.data.PlayerData;
import pandorum.features.Translator.Language;

import static pandorum.PluginVars.translatorLanguages;
import static pandorum.data.Database.getPlayerData;
import static pandorum.data.Database.setPlayerData;
import static pandorum.features.Translator.getLanguageByCode;
import static pandorum.util.PlayerUtils.bundled;
import static pandorum.util.Search.findLocale;

public class TranslatorCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        PlayerData data = getPlayerData(player.uuid());

        if (args.length == 0 || args[0].equalsIgnoreCase("current")) {
            bundled(player, "commands.tr.current", data.locale);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "list" -> {
                StringBuilder result = new StringBuilder(Bundle.format("commands.tr.list", findLocale(player.locale)));
                translatorLanguages.each(language -> result.append("[cyan]").append(language.code()).append(" [accent](").append(language.name()).append(")\n"));
                Call.infoMessage(player.con, result.toString());
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
                Language language = getLanguageByCode(args[0]);
                if (language == null) {
                    bundled(player, "commands.tr.incorrect");
                    return;
                }

                data.locale = language.code();
                setPlayerData(player.uuid(), data);
                bundled(player, "commands.tr.changed", language.code(), language.name());
            }
        }
    }
}
