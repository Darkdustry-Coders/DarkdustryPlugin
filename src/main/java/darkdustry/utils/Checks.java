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
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;

import java.util.Objects;

import static arc.util.Strings.canParsePositiveInt;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Bundle.bundled;
import static darkdustry.discord.Bot.*;
import static darkdustry.utils.Cooldowns.defaults;
import static darkdustry.utils.Utils.*;
import static java.util.Objects.requireNonNull;
import static mindustry.Vars.*;
import static mindustry.net.Administration.Config.enableVotekick;

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
        return check(session.voted.containsKey(player.uuid()), player, "commands.already-voted");
    }

    public static boolean alreadySynced(Player player) {
        return check(!Cooldowns.canRun(player.uuid(), "sync"), player, "commands.sync.cooldown", formatDuration(defaults.get("sync"), Find.locale(player.locale)));
    }

    public static boolean isOnCooldown(Player player, String command) {
        return check(!player.admin && !Cooldowns.canRun(player.uuid(), command), player, "commands.cooldown", command, formatDuration(defaults.get(command), Find.locale(player.locale)));
    }

    // endregion
    // region Discord

    public static boolean notHosting(SlashCommandInteractionEvent event) {
        return check(state.isMenu(), event, ":gear: Сервер не запущен.", ":thinking: Почему?");
    }

    public static boolean notAdmin(GenericComponentInteractionCreateEvent event) {
        return check(!isAdmin(event.getMember()), () -> event.replyEmbeds(
                error(":no_entry_sign: Взаимодействовать с запросами могут только админы.").build()
        ).setEphemeral(true).queue());
    }

    public static boolean notFound(SlashCommandInteractionEvent event, Map map) {
        return check(map == null, event, ":mag: Карта не найдена.", "Проверь, правильно ли введено название.");
    }

    public static boolean notFound(SlashCommandInteractionEvent event, Player player) {
        return check(player == null, event, ":mag: Игрок не найден.", "Проверь, правильно ли введен никнейм.");
    }

    public static boolean notMap(SlashCommandInteractionEvent event) {
        var attachment = requireNonNull(event.getOption("map")).getAsAttachment();
        return check(!Objects.equals(attachment.getFileExtension(), mapExtension), event, ":link: Неверное вложение.", "Тебе нужно прикрепить один файл с расширением **.msav!**");
    }

    public static boolean notMap(SlashCommandInteractionEvent event, Fi file) {
        return check(!SaveIO.isSaveValid(file), () -> {
            event.replyEmbeds(error(":link: Неверное вложение.", "Файл поврежден или не является картой!").build()).queue();
            file.delete();
        });
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

    private static boolean check(boolean result, SlashCommandInteractionEvent event, String title, String description, Object... values) {
        return check(result, () -> event.replyEmbeds(error(title, description, values).build()).queue());
    }

    // endregion
}