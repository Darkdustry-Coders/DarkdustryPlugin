package darkdustry.utils;

import arc.files.Fi;
import arc.util.Log;
import darkdustry.components.Icons;
import darkdustry.discord.MessageContext;
import darkdustry.features.Ranks.Rank;
import darkdustry.features.votes.VoteSession;
import discord4j.core.event.domain.interaction.ComponentInteractionEvent;
import discord4j.core.object.entity.Role;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import mindustry.game.Gamemode;
import mindustry.game.Team;
import mindustry.gen.Player;
import mindustry.maps.Map;
import mindustry.net.Administration.PlayerInfo;
import mindustry.type.Item;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.blocks.storage.CoreBlock;
import useful.Bundle;
import useful.Cooldowns;

import static arc.math.Mathf.PI;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;
import static mindustry.net.Administration.Config.enableVotekick;

public class Checks {

    // region console

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

    public static boolean invalidDuration(int duration, int min, int max) {
        return check(duration < min || duration > max, "Duration should be an integer between @ and @.", min, max);
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
        return check(team == null, player, "commands.team-not-found", Icons.teamsList());
    }

    public static boolean notFound(Player player, UnitType type) {
        return check(!isAvailable(type), player, "commands.unit-not-found", Icons.contentList(content.units().select(Utils::isAvailable)));
    }

    public static boolean notFound(Player player, Block block) {
        return check(!isAvailable(block), player, "commands.block-not-found", Icons.contentList(content.blocks().select(Utils::isAvailable)));
    }

    public static boolean notFound(Player player, Item item) {
        return check(item == null, player, "commands.item-not-found", Icons.contentList(content.items()));
    }

    public static boolean notFound(Player player, StatusEffect effect) {
        return check(effect == null, player, "commands.effect-not-found", Icons.contentList(content.statusEffects()));
    }

    public static boolean notFoundCore(Player player, Block core) {
        return check(core == null, player, "commands.core-not-found", Icons.contentList(content.blocks().select(CoreBlock.class::isInstance)));
    }

    public static boolean notFound(Player player, Map map) {
        return check(map == null, player, "commands.map-not-found");
    }

    public static boolean notFound(Player player, Fi save) {
        return check(save == null, player, "commands.save-not-found");
    }

    public static boolean noCores(Player player, Team team) {
        return check(team.cores().isEmpty(), player, "commands.give.no-core", coloredTeam(team));
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
        return check(session.voted.containsKey(player.id), player, "commands.already-voted");
    }

    public static boolean onCooldown(Player player, String command) {
        return check(!Cooldowns.canRun(player, command), player, "commands.cooldown", command, formatDuration(Cooldowns.cooldown(command), player));
    }

    // endregion
    // region discord

    public static boolean noRole(ComponentInteractionEvent event, Role role) {
        return check(event.getInteraction().getMember().map(member -> !member.getRoleIds().contains(role.getId())).orElse(true), () ->
                event.reply().withEmbeds(EmbedCreateSpec.builder()
                        .color(Color.CINNABAR)
                        .title("Missing Permissions")
                        .description("You must be at least " + role.getMention() + " to use this feature.")
                        .build()).withEphemeral(true).subscribe());
    }

    public static boolean noRole(MessageContext context, Role role) {
        return check(!context.member().getRoleIds().contains(role.getId()), context, "Missing Permissions", "You must be at least @ to use this command.", role.getMention());
    }

    public static boolean notMap(MessageContext context) {
        return check(context.message().getAttachments().stream().noneMatch(attachment -> attachment.getFilename().endsWith(mapExtension)), context, "Invalid Attachments", "You need to attach at least one **.@** file.", mapExtension);
    }

    public static boolean notFound(MessageContext context, Map map) {
        return check(map == null, context, "Map Not Found", "Check if the name is spelled correctly.");
    }

    public static boolean notFound(MessageContext context, PlayerInfo info) {
        return check(info == null, context, "Player Not Found", "Check if the name is spelled correctly.");
    }

    public static boolean notFound(MessageContext context, Rank rank) {
        return check(rank == null, context, "Rank Not Found", "Check if the name is spelled correctly.");
    }

    public static boolean invalidDuration(MessageContext context, int duration, int min, int max) {
        return check(duration < min || duration > max, context, "Invalid Duration", "Duration should be an integer between @ and @.", min, max);
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

    // endregion
}