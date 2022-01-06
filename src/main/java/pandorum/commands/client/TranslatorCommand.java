package pandorum.commands.client;

import mindustry.gen.Player;
import pandorum.models.PlayerModel;

import static pandorum.Misc.bundled;
import static pandorum.PluginVars.codeLanguages;

public class TranslatorCommand {
    public static void run(final String[] args, final Player player) {
        PlayerModel.find(player.uuid(), playerInfo -> {
            switch (args[0].toLowerCase()) {
                case "current" -> bundled(player, "commands.tr.current", playerInfo.locale);
                case "list" -> {
                    StringBuilder locales = new StringBuilder();
                    codeLanguages.keys().toSeq().each(locale -> locales.append(" ").append(locale));
                    bundled(player, "commands.tr.list", locales.toString());
                }
                case "off" -> {
                    playerInfo.locale = "off";
                    playerInfo.save();
                    bundled(player, "commands.tr.disabled");
                }
                case "auto" -> {
                    playerInfo.locale = "auto";
                    playerInfo.save();
                    bundled(player, "commands.tr.auto");
                }
                default -> {
                    if (!codeLanguages.containsKey(args[0])) {
                        bundled(player, "commands.tr.incorrect");
                        break;
                    }

                    playerInfo.locale = args[0];
                    playerInfo.save();
                    bundled(player, "commands.tr.changed", args[0]);
                }
            }
        });
    }
}
