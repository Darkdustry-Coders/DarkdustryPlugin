package pandorum.commands.client;

import mindustry.gen.Player;
import pandorum.models.PlayerModel;

import static pandorum.util.Utils.bundled;
import static pandorum.PluginVars.codeLanguages;

public class TranslatorCommand {
    public static void run(final String[] args, final Player player) {
        PlayerModel.find(player, playerModel -> {
            switch (args[0].toLowerCase()) {
                case "current" -> bundled(player, "commands.tr.current", playerModel.locale);
                case "list" -> {
                    StringBuilder locales = new StringBuilder();
                    codeLanguages.keys().toSeq().each(locale -> locales.append(" ").append(locale));
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
                    if (!codeLanguages.containsKey(args[0])) {
                        bundled(player, "commands.tr.incorrect");
                        break;
                    }

                    playerModel.locale = args[0];
                    playerModel.save();
                    bundled(player, "commands.tr.changed", args[0], codeLanguages.get(args[0]));
                }
            }
        });
    }
}
