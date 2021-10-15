package pandorum.commands.client;

import mindustry.gen.Player;
import org.bson.Document;

import static pandorum.Misc.bundled;
import static pandorum.PandorumPlugin.*;

public class TranslatorCommand {
    public static void run(final String[] args, final Player player) {
        Document playerInfo = createInfo(player);
        switch (args[0].toLowerCase()) {
            case "current" -> {
                String locale = playerInfo.getString("locale");
                bundled(player, "commands.tr.current", locale == null ? "off" : locale);
            }
            case "list" -> {
                StringBuilder builder = new StringBuilder();
                codeLanguages.keys().forEach(locale -> builder.append(" ").append(locale));
                bundled(player, "commands.tr.list", builder.toString());
            }
            case "off" -> {
                playerInfo.replace("locale", "off");
                bundled(player, "commands.tr.disabled");
            }
            case "auto" -> {
                playerInfo.replace("locale", "auto");
                bundled(player, "commands.tr.auto");
            }
            default -> {
                if (!codeLanguages.containsKey(args[0])) {
                    bundled(player, "commands.tr.incorrect");
                    break;
                }

                playerInfo.replace("locale", args[0]);
                bundled(player, "commands.tr.changed", args[0]);
            }
        }

        savePlayerStats(player.uuid());
    }
}
