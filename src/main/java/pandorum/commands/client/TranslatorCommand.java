package pandorum.commands.client;

import mindustry.gen.Player;
import pandorum.database.databridges.PlayerInfo;

import static pandorum.PluginVars.codeLanguages;
import static pandorum.util.Utils.bundled;

public class TranslatorCommand {
    public static void run(final String[] args, final Player player) {
        PlayerInfo.find(player, playerModel -> {
            switch (args[0].toLowerCase()) {
                case "current" -> bundled(player, "commands.tr.current", playerModel.locale);
                case "list" -> {
                    StringBuilder locales = new StringBuilder();
                    codeLanguages.keys().toSeq().each(locale -> locales.append(" ").append(locale));
                    bundled(player, "commands.tr.list", locales.toString());
                }
                case "off" -> {
                    playerModel.locale = "off";
                    PlayerInfo.save(playerModel);
                    bundled(player, "commands.tr.disabled");
                }
                case "auto" -> {
                    playerModel.locale = "auto";
                    PlayerInfo.save(playerModel);
                    bundled(player, "commands.tr.auto");
                }
                default -> {
                    String locale = codeLanguages.keys().toSeq().find(key -> key.equalsIgnoreCase(args[0]));
                    if (locale == null) {
                        bundled(player, "commands.tr.incorrect");
                        return;
                    }

                    playerModel.locale = locale;
                    PlayerInfo.save(playerModel);
                    bundled(player, "commands.tr.changed", locale, codeLanguages.get(locale));
                }
            }
        });
    }
}
