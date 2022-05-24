package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Player;
import pandorum.data.PlayerData;

import static pandorum.PluginVars.codeLanguages;
import static pandorum.data.Database.getPlayerData;
import static pandorum.util.Search.findTranslatorLocale;
import static pandorum.util.Utils.bundled;

public class TranslatorCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        PlayerData data = getPlayerData(player.uuid());

        switch (args[0].toLowerCase()) {
            case "current" -> bundled(player, "commands.tr.current", data.locale);
            case "list" -> {
                StringBuilder locales = new StringBuilder();
                codeLanguages.keys().toSeq().each(locale -> locales.append(locale).append(" "));
                bundled(player, "commands.tr.list", locales.toString());
            }
            case "off" -> {
                data.locale = "off";
                bundled(player, "commands.tr.disabled");
            }
            case "auto" -> {
                data.locale = "auto";
                bundled(player, "commands.tr.auto");
            }
            default -> {
                String locale = findTranslatorLocale(args[0]);
                if (locale == null) {
                    bundled(player, "commands.tr.incorrect");
                    return;
                }

                data.locale = locale;
                bundled(player, "commands.tr.changed", locale, codeLanguages.get(locale));
            }
        }
    }
}
