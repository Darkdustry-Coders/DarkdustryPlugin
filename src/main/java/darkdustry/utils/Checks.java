package darkdustry.utils;

import arc.files.Fi;
import arc.util.*;
import com.ospx.sock.EventBus.Request;
import darkdustry.components.Database.*;
import darkdustry.components.Socket;
import darkdustry.discord.MessageContext;
import darkdustry.features.Ranks.Rank;
import darkdustry.features.votes.VoteSession;
import darkdustry.listeners.SocketEvents.EmbedResponse;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.entity.Role;
import discord4j.core.spec.EmbedCreateSpec;
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
import static darkdustry.discord.DiscordConfig.*;
import static darkdustry.utils.Utils.*;
import static discord4j.rest.util.Color.*;
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
        return check(ban == null, "No banned player found for provided input.");
    }

    public static boolean notKicked(PlayerInfo info) {
        return check(netServer.admins.getKickTime(info.id, info.lastIP) < Time.millis() && !netServer.admins.isDosBlacklisted(info.lastIP), "This player wasn't kicked from this server.");
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
        return check(target == player, player, "commands.votekick.player-is-you") || check(target.admin, player, "commands.votekick.player-is-admin") || check(target.team() != player.team(), player, "commands.votekick.player-is-enemy");
    }

    public static boolean invalidVoteTarget(Player player, Player target) {
        return check(target == player, player, "commands.vote.player-is-you") || check(target.team() != player.team(), player, "commands.vote.player-is-enemy");
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
        return check(session != null, player, "commands.vote-already-started");
    }

    public static boolean notVoting(Player player, VoteSession session) {
        return check(session == null, player, "commands.no-voting");
    }

    public static boolean alreadyAdmin(Player player) {
        return check(player.admin, player, "commands.login.already-admin");
    }

    public static boolean alreadyVoted(Player player, VoteSession session) {
        return check(session.voted.containsKey(player), player, "commands.already-voted");
    }

    public static boolean onCooldown(Player player, String command) {
        return check(!Cooldowns.canRun(player, command), player, "commands.cooldown", command, Bundle.formatDuration(player, Cooldowns.cooldown(command)));
    }

    // endregion
    // region discord

    public static boolean noRole(SelectMenuInteractionEvent event, Role role) {
        return check(event.getInteraction().getMember().map(member -> !member.getRoleIds().contains(role.getId())).orElse(true), () ->
                event.reply().withEmbeds(EmbedCreateSpec.builder()
                        .color(CINNABAR)
                        .title("Missing Permissions")
                        .description("You must be " + role.getMention() + " to use this feature.")
                        .build()).withEphemeral(true).subscribe());
    }

    public static boolean noRole(MessageContext context, Role role) {
        return check(!context.member().getRoleIds().contains(role.getId()), context, "Missing Permissions", "You must be @ to use this command.", role.getMention());
    }

    public static boolean notMap(MessageContext context) {
        return check(context.message().getAttachments().stream().noneMatch(attachment -> attachment.getFilename().endsWith(mapExtension)), context, "Invalid Attachments", "You need to attach at least one **.@** file.", mapExtension);
    }

    public static boolean notFoundServer(MessageContext context, String server) {
        return check(!discordConfig.serverToChannel.containsKey(server), context, "Server Not Found", "**Available servers:** @", Strings.join(", ", discordConfig.serverToChannel.keys()));
    }

    // endregion
    // region response

    public static boolean notFound(Request<EmbedResponse> request, Map map) {
        return check(map == null, request, "Map Not Found", "Check if the input is correct.");
    }

    public static boolean notRemoved(Request<EmbedResponse> request, Map map) {
        return check(!map.custom, request, "Map Not Removed", "This map is built-in and can't be removed.");
    }

    public static boolean notFound(MessageContext context, Player player) {
        return check(player == null, context, "Player Not Found", "Check if the input is correct.");
    }

    public static boolean notFound(MessageContext context, PlayerInfo info) {
        return check(info == null, context, "Player Info Not Found", "Check if the input is correct.");
    }

    public static boolean notFound(MessageContext context, PlayerData data) {
        return check(data == null, context, "Player Data Not Found", "Check if the input is correct.");
    }

    public static boolean notFound(MessageContext context, Rank rank) {
        return check(rank == null, context, "Rank Not Found", "Check if the input is correct.");
    }



    public static boolean invalidDuration(MessageContext context, Duration duration) {
        return check(duration.isZero(), context, "Invalid Duration", "The provided duration is invalid. (Example: 1h, 30min)");
    }

    public static boolean notBanned(MessageContext context, Ban ban) {
        return check(ban == null, context, "Unban Failed", "No banned player found for provided input.");
    }

    public static boolean notKicked(MessageContext context, PlayerInfo info) {
        return check(netServer.admins.getKickTime(info.id, info.lastIP) < Time.millis() && !netServer.admins.isDosBlacklisted(info.lastIP), context, "Pardon Failed", "This player wasn't kicked from this server.");
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
        return check(result, () -> Socket.respond(request, EmbedResponse.error(title, content, values)));
    }

    // endregion
}