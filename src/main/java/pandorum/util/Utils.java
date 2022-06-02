package pandorum.util;

import arc.Core;
import arc.files.Fi;
import arc.func.Cons;
import arc.math.geom.Position;
import arc.struct.Seq;
import arc.util.CommandHandler.Command;
import arc.util.Strings;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.server.ServerControl;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Tile;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import pandorum.components.Bundle;
import pandorum.components.Icons;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static mindustry.Vars.mods;
import static pandorum.PluginVars.*;
import static pandorum.discord.Bot.adminRole;

public class Utils {

    public static <T> T notNullElse(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    public static int voteChoice(String value) {
        return switch (value.toLowerCase()) {
            case "y", "yes", "д", "да", "так", "+" -> 1;
            case "n", "no", "н", "нет", "ні", "-" -> -1;
            default -> 0;
        };
    }

    public static boolean deepEquals(String first, String second) {
        return stripAll(first).equalsIgnoreCase(stripAll(second)) || stripAll(first).toLowerCase().contains(stripAll(second).toLowerCase());
    }

    public static boolean isNearCore(Team team, Position position) {
        return team.cores().contains(core -> core.dst(position) < alertsDistance);
    }

    public static boolean isDangerousBuild(Block block, Team team, Tile tile) {
        return dangerousBuildBlocks.containsKey(block) && dangerousBuildBlocks.get(block).get() && isNearCore(team, tile);
    }

    public static boolean isDangerousDeposit(Building build, Item item, Team team) {
        return dangerousDepositBlocks.containsKey(build.block) && dangerousDepositBlocks.get(build.block) == item && isNearCore(team, build);
    }

    public static String stripAll(String str) {
        return Strings.stripColors(Strings.stripGlyphs(str));
    }

    public static String coloredTeam(Team team) {
        return Icons.get(team.name) + "[#" + team.color + "]" + team.name;
    }

    public static String getName(Unit unit) {
        return Utils.notNullElse(unit.getControllerName(), Icons.get(unit.type.name));
    }

    public static boolean adminCheck(Player player) {
        return player != null && player.admin;
    }

    public static boolean adminCheck(Member member) {
        return member != null && (member.getRoles().contains(adminRole) || member.hasPermission(Permission.ADMINISTRATOR));
    }

    public static void bundled(Player player, String key, Object... values) {
        player.sendMessage(Bundle.format(key, Search.findLocale(player.locale), values));
    }

    public static void sendToChat(String key, Object... values) {
        Groups.player.each(player -> bundled(player, key, values));
    }

    public static void eachPlayer(Team team, Cons<Player> cons) {
        Groups.player.each(player -> player.team() == team, cons);
    }

    public static String formatDate(long time) {
        DateFormat format = new SimpleDateFormat("HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Moscow")));
        return format.format(new Date(time));
    }

    public static String formatDuration(long time) {
        Duration duration = Duration.ofMillis(time);
        StringBuilder builder = new StringBuilder();
        if (duration.toDaysPart() > 0) builder.append(duration.toDaysPart()).append(" д ");
        if (duration.toHoursPart() > 0) builder.append(duration.toHoursPart()).append(" ч ");
        if (duration.toMinutesPart() > 0) builder.append(duration.toMinutesPart()).append(" мин ");

        builder.append(duration.toSecondsPart()).append(" сек");
        return builder.toString();
    }

    public static long millisecondsToMinutes(long time) {
        return TimeUnit.MILLISECONDS.toMinutes(time);
    }

    public static long secondsToMinutes(long time) {
        return TimeUnit.SECONDS.toMinutes(time);
    }

    public static Seq<Command> getAvailableClientCommands(boolean admin) {
        Seq<Command> commands = clientCommands.getCommandList();
        Seq<Command> playerCommands = commands.copy().filter(command -> !clientAdminOnlyCommands.contains(command));
        Seq<Command> adminCommands = commands.copy().filter(command -> admin && clientAdminOnlyCommands.contains(command));

        return playerCommands.sort(Comparator.comparing(command -> command.text)).addAll(adminCommands.sort(Comparator.comparing(command -> command.text)));
    }

    public static Seq<Command> getAvailableDiscordCommands(boolean admin) {
        Seq<Command> commands = discordCommands.getCommandList();
        Seq<Command> playerCommands = commands.copy().filter(command -> !discordAdminOnlyCommands.contains(command));
        Seq<Command> adminCommands = commands.copy().filter(command -> admin && discordAdminOnlyCommands.contains(command));

        return playerCommands.sort(Comparator.comparing(command -> command.text)).addAll(adminCommands.sort(Comparator.comparing(command -> command.text)));
    }

    public static Fi getPluginFile() {
        return mods.locateMod("darkdustry-plugin").root;
    }

    public static ServerControl getServerControl() {
        return (ServerControl) Core.app.getListeners().find(listener -> listener instanceof ServerControl);
    }
}
