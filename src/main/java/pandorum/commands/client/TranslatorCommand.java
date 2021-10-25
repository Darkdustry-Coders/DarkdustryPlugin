package pandorum.commands.client;

import com.mongodb.BasicDBObject;
import mindustry.gen.Player;
import pandorum.models.PlayerModel;

import static pandorum.Misc.bundled;
import static pandorum.PandorumPlugin.*;

public class TranslatorCommand implements ClientCommand {
    public static void run(final String[] args, final Player player) {
        PlayerModel.find(new BasicDBObject("UUID", player.uuid()), playerInfo -> {
            switch (args[0].toLowerCase()) {
                case "current" -> bundled(player, "commands.tr.current", playerInfo.locale);
                case "list" -> {
                    StringBuilder builder = new StringBuilder();
                    codeLanguages.keys().forEach(locale -> builder.append(" ").append(locale));
                    bundled(player, "commands.tr.list", builder.toString());
                }
                case "off" -> {
                    playerInfo.locale = "off";
                    bundled(player, "commands.tr.disabled");
                }
                case "auto" -> {
                    playerInfo.locale = "auto";
                    bundled(player, "commands.tr.auto");
                }
                default -> {
                    if (!codeLanguages.containsKey(args[0])) {
                        bundled(player, "commands.tr.incorrect");
                        break;
                    }

                    playerInfo.locale = args[0];
                    bundled(player, "commands.tr.changed", args[0]);
                }
            }
            playerInfo.save();
        });
    }
}
