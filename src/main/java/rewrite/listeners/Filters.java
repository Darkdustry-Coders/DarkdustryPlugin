package rewrite.listeners;

import arc.util.Log;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Administration.ActionType;
import mindustry.net.Administration.PlayerAction;
import rewrite.discord.Bot;
import rewrite.features.Translator;
import rewrite.features.history.History;
import rewrite.features.history.RotateEntry;

import static arc.util.Strings.*;
import static mindustry.Vars.*;
import static rewrite.components.Database.*;

public class Filters {
    
    public static boolean action(PlayerAction action) {
        if (History.enabled() && action.type == ActionType.rotate) History.put(new RotateEntry(action), action.tile);
        return true;
    }

    public static String chat(Player author, String text) {
        Log.info("&fi@: @", "&lc" + author.name, "&lw" + text);
        author.sendMessage(netServer.chatFormatter.format(author, text), author, text);
        Bot.sendMessage(Bot.botChannel, "@ » @", stripColors(author.name), stripColors(text));

        Translator.cache.clear();
        Groups.player.each(player -> player != author, player -> {
            PlayerData data = getPlayerData(player);
            if (data.language.equals("off") || Translator.left == 0) player.sendMessage(netServer.chatFormatter.format(author, text), author, text);
            else Translator.translate(data.language, stripColors(text), translated -> player.sendMessage(netServer.chatFormatter.format(author, text) + " [white]([lightgray]" + translated + "[])", author, text));
        });

        return null;
    }
}
