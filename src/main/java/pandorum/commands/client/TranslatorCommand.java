package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Player;
import pandorum.mongo.models.PlayerModel;

import static pandorum.PluginVars.codeLanguages;
import static pandorum.util.Search.findTranslatorLocale;
import static pandorum.util.Utils.bundled;

public class TranslatorCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        PlayerModel.find(player, playerModel -> {
            switch (args[0].toLowerCase()) {
                case "current" -> bundled(player, "commands.tr.current", playerModel.locale);
                case "list" -> {
                    StringBuilder locales = new StringBuilder();
                    codeLanguages.keys().toSeq().each(locale -> locales.append(locale).append(" "));
                    bundled(player, "commands.tr.list", locales.toString());
                }
                case "off" -> {
                    playerModel.locale = "off";
                    playerModel.save();
                    bundled(player, "commands.tr.disabled");
                }
                case "auto" -> {
                    playerModel.locale = "auto";
                    playerModel.save();
                    bundled(player, "commands.tr.auto");
                }
                default -> {
                    String locale = findTranslatorLocale(args[0]);
                    if (locale == null) {
                        bundled(player, "commands.tr.incorrect");
                        return;
                    }

                    playerModel.locale = locale;
                    playerModel.save();
                    bundled(player, "commands.tr.changed", locale, codeLanguages.get(locale));
                }
            }
        });
    }
}
