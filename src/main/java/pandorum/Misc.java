package pandorum;

import arc.files.Fi;
import arc.struct.*;
import arc.util.*;
import mindustry.core.NetClient;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.maps.Map;

import java.util.Objects;
import java.util.Locale;
    
import static mindustry.Vars.*;
import pandorum.comp.*;
import pandorum.PandorumPlugin.*;

public abstract class Misc{

    private Misc(){}

    private static final Seq<String> bools = Seq.with(bundle.get("misc.bools", findLocale("ru")).split(", "));

    public static boolean bool(String text){
        Objects.requireNonNull(text, "text");
        return bools.contains(text.toLowerCase());
    }

    public static String colorizedTeam(Team team){
        Objects.requireNonNull(team, "team");
        return Strings.format("[#@]@", team.color, team);
    }

    public static String colorizedName(Player player){
        Objects.requireNonNull(player, "player");
        return NetClient.colorizeName(player.id, player.name);
    }

    public static Map findMap(String text){
        for(int i = 0; i < maps.all().size; i++){
            Map map = maps.all().get(i);
            if((Strings.canParseInt(text) && i == Strings.parseInt(text) - 1) || map.name().equals(text)){
                return map;
            }
        }
        return null;
    }

    public static Fi findSave(String text){
        for(int i = 0; i < saveDirectory.list().length; i++){
            Fi save = saveDirectory.list()[i];
            if((Strings.canParseInt(text) && i == Strings.parseInt(text) - 1) || Objects.equals(save.nameWithoutExtension(), text)){
                return save;
            }
        }
        return null;
    }
    public static Locale findLocale(String code) {
        Locale locale = Structs.find(bundle.supportedLocales, l -> l.toString().equals(code) ||
                code.startsWith(l.toString()));
        return locale != null ? locale : bundle.defaultLocale();
    }

    public static boolean isError(String output) {
        try {
            String errorName = output.substring(0, output.indexOf(' ') - 1);
            Class.forName("org.mozilla.javascript." + errorName);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    public static boolean adminCheck(Player player) {
        if (!player.admin()) {
            bundled(player, "commands.permission-denied");
            return false;
        }
        return true;
    }

    public static void bundled(Player player, String key, Object... values) {
        player.sendMessage(bundle.format(key, findLocale(player.locale), values));
    }

    public static void sendToChat(String key, Object... values) {
        Groups.player.each(p -> p.sendMessage(bundle.format(key, findLocale(p.locale), values)));
    }
}
