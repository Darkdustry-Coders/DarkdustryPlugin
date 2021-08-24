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

public abstract class Misc{

    private Misc(){}

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
        Locale locale = Structs.find(Bundle.supportedLocales, l -> l.toString().equals(code) ||
                code.startsWith(l.toString()));
        return locale != null ? locale : Bundle.defaultLocale();
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
        player.sendMessage(Bundle.format(key, findLocale(player.locale), values));
    }

    public static void sendToChat(String key, Object... values) {
        Groups.player.each(p -> p.sendMessage(Bundle.format(key, findLocale(p.locale), values)));
    }

    public static Player findByName(String name) {
        return Groups.player.find(p -> Strings.stripColors(p.name).equalsIgnoreCase(name));
    }

    public static boolean nameCheck(Player player) {
        String name = Strings.stripColors(player.name);
        if (name.length() < 2 || name.length() > 25) {
            player.con.kick(Bundle.format("events.bad-name-length", findLocale(player.locale)), 0);
            return true;
        }
        //TODO кикать матерные ники
        if (name.contains("@")) {
            player.con.kick(Bundle.format("events.bad-name", findLocale(player.locale)), 0);
            return true;
        }
        return false;
    }
}
