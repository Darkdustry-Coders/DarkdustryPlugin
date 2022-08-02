package rewrite.listeners;

import arc.util.Log;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Administration.PlayerAction;
import rewrite.features.Translator;

import static arc.util.Strings.*;
import static mindustry.Vars.*;
import static rewrite.components.Database.*;

public class Filters {
    
    public static boolean action(PlayerAction action) {
        // if (History.enabled() && action.type == ActionType.rotate) {
        //     var entry = new RotateEntry(action);
        //     History.putTileHistory(entry, action.tile);
        // }

        return true; // TODO: добавить историю
    }

    public static String chat(Player author, String text) {
        Log.info("&fi@: @", "&lc" + author.name, "&lw" + text);
        author.sendMessage(netServer.chatFormatter.format(author, text), author, text);
        // Bot.sendMessage(botChannel, "@ » @", Strings.stripColors(author.name), Strings.stripColors(text));

        Groups.player.each(player -> player != author, player -> {
            PlayerData data = getPlayerData(player.uuid());
            if (data.language.equals("off")) player.sendMessage(netServer.chatFormatter.format(author, text), author, text);
            else Translator.translate(data.language, stripColors(text), translated -> player.sendMessage(netServer.chatFormatter.format(author, text) + (translated.isBlank() ? "" : " [white]([lightgray]" + translated + "[])"), author, text));
        });

        return null;
    }
}
