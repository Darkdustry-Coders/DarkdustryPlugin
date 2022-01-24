package pandorum;

import arc.Core;
import arc.files.Fi;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Structs;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.io.SaveIO;
import mindustry.maps.Map;
import mindustry.server.ServerControl;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.world.Block;
import pandorum.comp.Bundle;
import pandorum.comp.Icons;
import reactor.util.function.Tuple2;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static mindustry.Vars.*;
import static pandorum.PluginVars.config;

public class Misc {

    public static String colorizedTeam(Team team) {
        return Icons.get(team.name) + "[#" + team.color + "]" + team.name;
    }

    public static Map findMap(String name) {
        Seq<Map> mapsList = maps.customMaps();
        return Strings.canParseInt(name) ? mapsList.get(Strings.parseInt(name) - 1) : mapsList.find(map -> Strings.stripColors(map.name()).equalsIgnoreCase(name) || Strings.stripColors(map.name()).contains(name));
    }

    public static Fi findSave(String name) {
        Seq<Fi> savesList = Seq.with(saveDirectory.list()).filter(SaveIO::isSaveValid);
        return Strings.canParseInt(name) ? savesList.get(Strings.parseInt(name) - 1) : savesList.find(save -> save.nameWithoutExtension().equalsIgnoreCase(name) || save.nameWithoutExtension().contains(name));
    }

    public static Locale findLocale(String name) {
        Locale locale = Structs.find(Bundle.supportedLocales, l -> name.equals(l.toString()) || name.startsWith(l.toString()));
        return locale != null ? locale : Bundle.defaultLocale();
    }

    public static Player findPlayer(String name) {
        return Strings.canParseInt(name) ? Groups.player.getByID(Strings.parseInt(name)) : Groups.player.find(player -> Strings.stripGlyphs(Strings.stripColors(player.name)).equalsIgnoreCase(Strings.stripGlyphs(Strings.stripColors(name))) || Strings.stripGlyphs(Strings.stripColors(player.name)).contains(Strings.stripGlyphs(Strings.stripColors(name))));
    }

    public static Block findBlock(String name) {
        return Strings.canParseInt(name) ? content.block(Strings.parseInt(name)) : content.blocks().find(block -> block.name.equalsIgnoreCase(name));
    }

    public static Block findCore(String name) {
        return switch (name.toLowerCase()) {
            case "small", "shard", "core-shard" -> Blocks.coreShard;
            case "medium", "foundation", "core-foundation" -> Blocks.coreFoundation;
            case "big", "nucleus", "core-nucleus" -> Blocks.coreNucleus;
            default -> null;
        };
    }

    public static UnitType findUnit(String name) {
        return Strings.canParseInt(name) ? content.unit(Strings.parseInt(name)) : content.units().find(unit -> unit.name.equalsIgnoreCase(name));
    }

    public static Item findItem(String name) {
        return Strings.canParseInt(name) ? content.item(Strings.parseInt(name)) : content.items().find(item -> item.name.equalsIgnoreCase(name));
    }

    public static Team findTeam(String name) {
        return Strings.canParseInt(name) ? Team.get(Strings.parseInt(name)) : Structs.find(Team.all, team -> team.name.equalsIgnoreCase(name));
    }

    public static boolean adminCheck(Player player) {
        if (!player.admin) {
            bundled(player, "commands.permission-denied");
            return true;
        }
        return false;
    }

    public static void bundled(Player player, String key, Object... values) {
        player.sendMessage(Bundle.format(key, findLocale(player.locale), values));
    }

    public static void sendToChat(String key, Object... values) {
        Groups.player.each(player -> bundled(player, key, values));
    }

    public static String formatTime(Date time) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Moscow")));
        return format.format(time);
    }

    public static long millisecondsToMinutes(long duration) {
        return TimeUnit.MILLISECONDS.toMinutes(duration);
    }

    public static long secondsToMinutes(long duration) {
        return TimeUnit.SECONDS.toMinutes(duration);
    }

    public static Tuple2<String, Integer> hubIp() {
        if (config.hubIp.contains(":")) {
            String[] parts = config.hubIp.split(":");
            return new Tuple2(parts[0], Strings.parseInt(parts[1], port));
        }
        return new Tuple2(config.hubIp, port);
    }

    public static ServerControl getServerControl() {
        return (ServerControl) Core.app.getListeners().find(listener -> listener instanceof ServerControl);
    }

    public static InputStream download(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
            return connection.getInputStream();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
