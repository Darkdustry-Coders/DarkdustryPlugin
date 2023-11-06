package darkdustry.utils;

import arc.files.Fi;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.*;
import com.ospx.sock.EventBus.Request;
import darkdustry.database.models.*;
import darkdustry.discord.MessageContext;
import darkdustry.features.Ranks.Rank;
import darkdustry.features.net.Socket;
import darkdustry.features.votes.VoteSession;
import darkdustry.listeners.SocketEvents.EmbedResponse;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import mindustry.game.Gamemode;
import mindustry.game.*;
import mindustry.gen.Player;
import mindustry.maps.Map;
import mindustry.net.Administration.PlayerInfo;
import mindustry.type.*;
import mindustry.world.Block;
import mindustry.world.blocks.storage.CoreBlock;
import useful.*;

import java.time.Duration;

import static arc.math.Mathf.*;
import static darkdustry.config.Config.*;
import static darkdustry.config.DiscordConfig.*;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;
import static mindustry.net.Administration.Config.*;

public class Checks {

    // region console

    public static boolean alreadyHosting() {
        return check(state.isGame(), "Already hosting.");
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
        return check(info == null, "No player info @ found.", name);
    }

    public static boolean notFound(PlayerData data, String name) {
        return check(data == null, "No player data @ found.", name);
    }

    public static boolean notFound(Rank rank, String name) {
        return check(rank == null, "No rank @ found.", name);
    }

    public static boolean invalidDuration(Duration duration) {
        return check(duration.isZero(), "The provided duration is invalid. (Example: 1h, 30min)");
    }

    public static boolean notBanned(Ban ban) {
        return check(ban == null, "This player wasn't banned from the server.");
    }

    public static boolean notKicked(PlayerInfo info) {
        return check(netServer.admins.getKickTime(info.id, info.lastIP) < Time.millis() && !netServer.admins.isDosBlacklisted(info.lastIP), "This player wasn't kicked from the server.");
    }

    // endregion
    // region client

    public static boolean notAdmin(Player player) {
        return check(!player.admin, player, "commands.permission-denied");
    }

    public static boolean notFound(Player player, Player target) {
        return check(target == null, player, "commands.player-not-found");
    }

    public static boolean notFound(Player player, Team team) {
        return check(team == null, player, "commands.team-not-found", formatTeams());
    }

    public static boolean notFound(Player player, UnitType type) {
        return check(type == null || !available(type), player, "commands.unit-not-found", formatContents(content.units().select(Utils::available)));
    }

    public static boolean notFound(Player player, Block block) {
        return check(block == null || !available(block), player, "commands.block-not-found");
    }

    public static boolean notFound(Player player, Item item) {
        return check(item == null, player, "commands.item-not-found", formatContents(content.items()));
    }

    public static boolean notFound(Player player, StatusEffect effect) {
        return check(effect == null, player, "commands.effect-not-found", formatContents(content.statusEffects()));
    }

    public static boolean notFoundCore(Player player, Block core) {
        return check(core == null, player, "commands.core-not-found", formatContents(content.blocks().select(CoreBlock.class::isInstance)));
    }

    public static boolean notFound(Player player, Map map) {
        return check(map == null, player, "commands.map-not-found");
    }

    public static boolean notFound(Player player, Fi save) {
        return check(save == null, player, "commands.save-not-found");
    }

    public static boolean noCores(Player player, Team team) {
        return check(team.cores().isEmpty(), player, "commands.give.no-core", team.coloredName());
    }

    public static boolean votekickDisabled(Player player) {
        return check(!enableVotekick.bool(), player, "commands.votekick.disabled");
    }

    public static boolean invalidVotekickTarget(Player player, Player target) {
        return check(player == target, player, "commands.votekick.player-is-you") || check(target.admin, player, "commands.votekick.player-is-admin") || check(player.team() != target.team(), player, "commands.votekick.player-is-enemy");
    }

    public static boolean invalidVoteTarget(Player player, Player target) {
        return check(player == target, player, "commands.vote.player-is-you") || check(player.team() != target.team(), player, "commands.vote.player-is-enemy");
    }

    public static boolean invalidSurrenderTeam(Player player) {
        return check(player.team().data().noCores(), player, "commands.surrender.invalid-team");
    }

    public static boolean otherSurrenderTeam(Player player, Team team) {
        return check(player.team() != team, player, "commands.surrender.other-team");
    }

    public static boolean invalidVoteSign(Player player, int sign) {
        return check(sign == 0, player, "commands.vote.incorrect-sign");
    }

    public static boolean invalidAmount(Player player, int amount, int min, int max) {
        return check(amount < min || amount > max, player, "commands.invalid-amount", min, max);
    }

    public static boolean invalidDuration(Player player, int duration, int min, int max) {
        return check(duration < min || duration > max, player, "commands.invalid-duration", min, max);
    }

    public static boolean invalidCoordinates(Player player, int x, int y) {
        return check(x < 0 || x > world.width() || y < 0 || y > world.height(), player, "commands.invalid-coordinates");
    }

    public static boolean invalidArea(Player player, int width, int height, int maxArea) {
        return check(width < 0 || height < 0 || width * height > maxArea, player, "commands.invalid-area-rect", maxArea);
    }

    public static boolean invalidArea(Player player, int radius, int maxArea) {
        return check(radius < 0 || PI * radius * radius > maxArea, player, "commands.invalid-area-circle", maxArea);
    }

    public static boolean alreadyVoting(Player player, VoteSession session) {
        return check(session != null, player, "commands.voting.already-started");
    }

    public static boolean notVoting(Player player, VoteSession session) {
        return check(session == null, player, "commands.voting.none");
    }

    public static boolean alreadyAdmin(Player player) {
        return check(player.admin, player, "commands.login.already-admin");
    }

    public static boolean alreadyVoted(Player player, VoteSession session) {
        return check(session.votes.containsKey(player), player, "commands.voting.already-voted");
    }

    // endregion
    // region discord

    public static boolean noRole(SelectMenuInteractionEvent event, Seq<Long> roleIDs) {
        return check(event.getInteraction()
                        .getMember()
                        .map(member -> member.getRoleIds().stream().noneMatch(role -> roleIDs.contains(role.asLong())))
                        .orElse(true),
                () -> event.reply().withEmbeds(EmbedCreateSpec.builder()
                        .color(Color.CINNABAR)
                        .title("Missing Permissions")
                        .description("You must have one of these roles to use this feature: " + formatRoles(roleIDs, "\n- "))
                        .build()).withEphemeral(true).subscribe());
    }

    public static boolean noRole(MessageContext context, Seq<Long> roleIDs) {
        return check(context.member()
                .getRoleIds()
                .stream().noneMatch(role -> roleIDs.contains(role.asLong())), context, "Missing Permissions", "You must have one of these roles to use this feature: @", formatRoles(roleIDs, "\n- "));
    }

    public static boolean notMap(MessageContext context) {
        return check(context.message()
                .getAttachments()
                .stream()
                .noneMatch(attachment -> attachment.getFilename().endsWith(mapExtension)), context, "Invalid Attachments", "You need to attach at least one **.@** file.", mapExtension);
    }

    public static boolean notFound(MessageContext context, PlayerData data) {
        return check(data == null, context, "Player Data Not Found", "Check if the input is correct.");
    }

    public static boolean notFound(MessageContext context, Rank rank) {
        return check(rank == null, context, "Rank Not Found", "Check if the input is correct.");
    }

    public static boolean notFound(MessageContext context, String server) {
        return check(!discordConfig.serverToChannel.containsKey(server), context, "Server Not Found", "**Available servers:** @", Strings.join(", ", discordConfig.serverToChannel.keys()));
    }

    // endregion
    // region response

    public static boolean notFound(Request<EmbedResponse> request, Player player) {
        return check(player == null, request, "Player Not Found", "Check if the input is correct.");
    }

    public static boolean notFound(Request<EmbedResponse> request, PlayerInfo info) {
        return check(info == null, request, "Player Info Not Found", "Check if the input is correct.");
    }

    public static boolean notFound(Request<EmbedResponse> request, Map map) {
        return check(map == null, request, "Map Not Found", "Check if the input is correct.");
    }

    public static boolean invalidDuration(Request<EmbedResponse> request, Duration duration) {
        return check(duration.isZero(), request, "Invalid Duration", "The provided duration is invalid. (Example: 1h, 30min)");
    }

    public static boolean notRemoved(Request<EmbedResponse> request, Map map) {
        return check(!map.custom, request, "Map Not Removed", "This map is built-in and can't be removed.");
    }

    public static boolean notBanned(Request<EmbedResponse> request, Ban ban) {
        return check(ban == null, request, "Unban Failed", "This player wasn't banned from the server.");
    }

    public static boolean notKicked(Request<EmbedResponse> request, PlayerInfo info) {
        return check(netServer.admins.getKickTime(info.id, info.lastIP) < Time.millis() && !netServer.admins.isDosBlacklisted(info.lastIP), request, "Pardon Failed", "This player wasn't kicked from the server.");
    }

    public static boolean noRtv(Request<EmbedResponse> request) {
        return check(!config.mode.enableRtv, request, "Not Allowed", "This server doesn't allow changing the map.");
    }

    // endregion
    // region utils

    private static boolean check(boolean result, Runnable runnable) {
        if (result) runnable.run();
        return result;
    }

    private static boolean check(boolean result, String error, Object... values) {
        return check(result, () -> Log.err(error, values));
    }

    private static boolean check(boolean result, Player player, String key, Object... values) {
        return check(result, () -> Bundle.send(player, key, values));
    }

    private static boolean check(boolean result, MessageContext context, String title, String content, Object... values) {
        return check(result, () -> context.error(title, content, values).subscribe());
    }

    private static boolean check(boolean result, Request<EmbedResponse> request, String title, String content, Object... values) {
        return check(result, () -> Socket.respond(request, EmbedResponse.error(title).withContent(content, values)));
    }

    // endregion
}