package darkdustry.utils;

import arc.files.Fi;
import arc.math.Mathf;
import arc.util.Log;
import darkdustry.features.Ranks.Rank;
import darkdustry.features.votes.VoteSession;
import mindustry.game.*;
import mindustry.gen.Player;
import mindustry.io.SaveIO;
import mindustry.maps.Map;
import mindustry.net.Administration.PlayerInfo;
import mindustry.type.*;
import mindustry.world.Block;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;

import static arc.util.Strings.canParsePositiveInt;
import static darkdustry.PluginVars.*;
import static darkdustry.discord.Bot.*;
import static darkdustry.discord.Bot.Palette.error;
import static darkdustry.utils.Cooldowns.defaults;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;
import static mindustry.net.Administration.Config.enableVotekick;
import static useful.Bundle.bundled;

public class Checks {

    // region Console

    public static boolean alreadyHosting() {
        return check(!state.isMenu(), "Already hosting.");
    }

    public static boolean notFound(Gamemode mode, String name) {
        return check(mode == null, "No gamemode @ found.", name);
    }

    public static boolean notFound(Map map, String name) {
        return check(map == null, "No map @ found.", name);
    }

    public static boolean notFound(Player player, String name) {
        return check(player == null, "No player @ found.", name);
    }

    public static boolean notFound(PlayerInfo info, String name) {
        return check(info == null, "No player @ found.", name);
    }

    public static boolean notFound(Rank rank, String name) {
        return check(rank == null, "No rank @ found.", name);
    }

    // endregion
    // region Client

    public static boolean notAdmin(Player player) {
        return check(!player.admin, player, "commands.permission-denied");
    }

    public static boolean notFound(Player player, Player target) {
        return check(target == null, player, "commands.player-not-found");
    }

    public static boolean notFound(Player player, Team team) {
        return check(team == null, player, "commands.team-not-found", teams);
    }

    public static boolean notFound(Player player, Item item) {
        return check(item == null, player, "commands.item-not-found", items);
    }

    public static boolean notFound(Player player, UnitType type) {
        return check(type == null || type.internal, player, "commands.unit-not-found", units);
    }

    public static boolean notFound(Player player, Block block) {
        return check(block == null || !block.inEditor, player, "commands.block-not-found");
    }

    public static boolean notFoundCore(Player player, Block block) {
        return check(block == null || !block.inEditor, player, "commands.core-not-found");
    }

    public static boolean notFound(Player player, Map map) {
        return check(map == null, player, "commands.map-not-found");
    }

    public static boolean notFound(Player player, Fi save) {
        return check(save == null, player, "commands.save-not-found");
    }

    public static boolean notLanguage(Player player, String language) {
        return check(language == null, player, "commands.language-not-found");
    }

    public static boolean noCores(Player player, Team team) {
        return check(team.core() == null, player, "commands.give.no-core", coloredTeam(team));
    }

    public static boolean votekickDisabled(Player player) {
        return check(!enableVotekick.bool(), player, "commands.votekick.disabled");
    }

    public static boolean invalidVotekickTarget(Player player, Player target) {
        return check(target == player, player, "commands.votekick.player-is-you") || check(target.admin, player, "commands.votekick.player-is-admin") || check(target.team() != player.team(), player, "commands.votekick.player-is-enemy");
    }

    public static boolean invalidVoteTarget(Player player, Player target) {
        return check(target == player, player, "commands.vote.player-is-you") || check(target.team() != player.team(), player, "commands.vote.player-is-enemy");
    }

    public static boolean invalidVoteSign(Player player, int sign) {
        return check(sign == 0, player, "commands.vote.incorrect-sign");
    }

    public static boolean invalidAmount(Player player, String[] args, int index) {
        return check(args.length > index && !canParsePositiveInt(args[index]), player, "commands.not-int");
    }

    public static boolean invalidTpCoords(Player player, String[] args) {
        return check(!canParsePositiveInt(args[0]) || !canParsePositiveInt(args[1]), player, "commands.tp.incorrect-number-format");
    }

    public static boolean invalidFillAmount(Player player, String[] args) {
        return check(!canParsePositiveInt(args[1]) || !canParsePositiveInt(args[2]), player, "commands.fill.incorrect-number-format");
    }

    public static boolean invalidVnwAmount(Player player, int amount) {
        return check(amount < 1 || amount > maxVnwAmount, player, "commands.vnw.limit", maxVnwAmount);
    }

    public static boolean invalidGiveAmount(Player player, int amount) {
        return check(amount < 1 || amount > maxGiveAmount, player, "commands.give.limit", maxGiveAmount);
    }

    public static boolean invalidSpawnAmount(Player player, int amount) {
        return check(amount < 1 || amount > maxSpawnAmount, player, "commands.spawn.limit", maxSpawnAmount);
    }

    public static boolean invalidFillAmount(Player player, int width, int height) {
        return check(width * height > maxFillAmount, player, "commands.fill.too-big-area", maxFillAmount);
    }

    public static boolean invalidFillAmount(Player player, int radius) {
        return check(Math.PI * Mathf.sqr(radius) > maxFillAmount, player, "commands.fill.too-big-area", maxFillAmount);
    }

    public static boolean alreadyVoting(Player player, VoteSession session) {
        return check(session != null, player, "commands.vote-already-started");
    }

    public static boolean notVoting(Player player, VoteSession session) {
        return check(session == null, player, "commands.no-voting");
    }

    public static boolean alreadyAdmin(Player player) {
        return check(player.admin, player, "commands.login.already-admin");
    }

    public static boolean alreadyVoted(Player player, VoteSession session) {
        return check(session.voted.containsKey(player.id), player, "commands.already-voted");
    }

    public static boolean onCooldown(Player player, String command) {
        return check(!Cooldowns.canRun(player, command), player, "commands.cooldown", command, formatDuration(defaults.get(command, defaultCooldown), player.locale));
    }

    // endregion
    // region Discord

    public static boolean notAdmin(GenericComponentInteractionCreateEvent event) {
        return check(!isAdmin(event.getMember()), () -> event.replyEmbeds(new EmbedBuilder().setColor(error).setTitle(":no_entry_sign: Missing permissions.").build()).setEphemeral(true).queue());
    }

    public static boolean notAdmin(Context context) {
        return check(!isAdmin(context.event().getMember()), context, ":no_entry_sign: Missing permissions!");
    }

    public static boolean notHosting(Context context) {
        return check(state.isMenu(), context, ":gear: Server not running.");
    }

    public static boolean notFound(Context context, Map map, String name) {
        return check(map == null, context, ":mag: No map **@** found.", name);
    }

    public static boolean notMap(Context context) {
        return check(context.message().getAttachments().size() != 1 || !mapExtension.equals(context.message().getAttachments().get(0).getFileExtension()), context, ":link: You need to attach a valid **.@** file!", mapExtension);
    }

    public static boolean notMap(Context context, Fi file) {
        return check(!SaveIO.isSaveValid(file) && file.delete(), context, ":link: You need to attach a valid **.@** file!", mapExtension);
    }

    // endregion
    // region Checks

    private static boolean check(boolean result, Runnable todo) {
        if (result) todo.run();
        return result;
    }

    private static boolean check(boolean result, String error, Object... values) {
        return check(result, () -> Log.err(error, values));
    }

    private static boolean check(boolean result, Player player, String key, Object... values) {
        return check(result, () -> bundled(player, key, values));
    }

    private static boolean check(boolean result, Context context, String title, Object... values) {
        return check(result, () -> context.error(title, values).queue());
    }

    // endregion
}