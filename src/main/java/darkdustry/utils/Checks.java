package darkdustry.utils;

import arc.files.Fi;
import arc.math.Mathf;
import arc.util.Log;
import darkdustry.discord.SlashContext;
import darkdustry.features.Ranks.Rank;
import darkdustry.features.votes.VoteSession;
import mindustry.game.Gamemode;
import mindustry.game.Team;
import mindustry.gen.Player;
import mindustry.io.SaveIO;
import mindustry.maps.Map;
import mindustry.net.Administration.Config;
import mindustry.net.Administration.PlayerInfo;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.world.Block;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import darkdustry.discord.Bot;

import java.awt.Color;
import java.util.Objects;

import static arc.util.Strings.*;
import static darkdustry.components.Database.hasPlayerData;
import static mindustry.Vars.*;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Bundle.bundled;
import static darkdustry.utils.Utils.*;

public class Checks {

    // region Console

    public static boolean isLaunched() {
        return check(!state.isMenu(), "Already hosting.");
    }

    public static boolean notFound(Gamemode mode, String[] name) {
        return check(mode == null, "No gamemode @ found.", name[1]);
    }

    public static boolean notFound(Map map, String[] name) {
        return check(map == null, "No map @ found.", name[0]);
    }

    public static boolean notFound(Player player, String[] name) {
        return check(player == null, "No player @ found.", name[0]);
    }

    public static boolean notFound(PlayerInfo info, String name) {
        return check(info == null, "No player @ found.", name);
    }

    public static boolean notFound(Rank rank, String name) {
        return check(rank == null, "No rank @ found.", name);
    }

    public static boolean noData(String uuid) {
        return check(!hasPlayerData(uuid), "No player data found by @.", uuid);
    }

    public static boolean invalidAmount(String[] args, int index) {
        return check(args.length <= index || !canParsePositiveInt(args[index]), "Value must be a number!");
    }

    // endregion
    // region Client

    public static boolean notFound(Player player, Map map) {
        return check(map == null, player, "commands.map-not-found");
    }

    public static boolean notFound(Player player, Fi file) {
        return check(file == null, player, "commands.save-not-found");
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
        return check(block == null, player, "commands.block-not-found");
    }

    public static boolean notFound(Player player, Player target, String name) {
        return check(target == null, player, "commands.player-not-found", name);
    }

    public static boolean notFound(Player player, String language) {
        return check(!translatorLanguages.containsKey(language), player, "commands.tr.not-found");
    }

    public static boolean notFoundCore(Player player, Block block) {
        return check(block == null, player, "commands.core.core-not-found");
    }

    public static boolean notFoundCore(Player player, Team team) {
        return check(team.core() == null, player, "commands.give.no-core", coloredTeam(team));
    }

    public static boolean votekickDisabled(Player player) {
        return check(!Config.enableVotekick.bool(), player, "commands.votekick.disabled");
    }

    public static boolean invalidVotekickTarget(Player player, Player target) {
        return check(target == player, player, "commands.votekick.player-is-you") || check(target.admin, player, "commands.votekick.player-is-admin") || check(target.team() != player.team(), player, "commands.votekick.player-is-enemy");
    }

    public static boolean invalidVoteTarget(Player player, Player target) {
        return check(target == player, "commands.vote.player-is-you") || check(target.team() != player.team(), player, "commands.vote.player-is-enemy");
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
        return check(!canParsePositiveInt(args[0]) || !canParsePositiveInt(args[1]), player, "commands.fill.incorrect-number-format");
    }

    public static boolean invalidVnwAmount(Player player, int amount) {
        return check(amount < 1 || amount > maxVnwAmount, player, "commands.vnw.limit", maxVnwAmount);
    }

    public static boolean invalidGiveAmount(Player player, int amount) {
        return check(amount < 1 || amount > maxGiveAmount, player, "commands.give.limit", maxGiveAmount);
    }

    public static boolean invalideSpawnAmount(Player player, int amount) {
        return check(amount < 1 || amount > maxSpawnAmount, player, "commands.spawn.limit", maxSpawnAmount);
    }

    public static boolean invalidFillAmount(Player player, int width, int height) {
        return check(width * height > maxFillAmount, player, "commands.fill.too-big-area", maxFillAmount);
    }

    public static boolean invalidFillAmount(Player player, int radius) {
        return check(Math.PI * Mathf.sqr(radius) > maxFillAmount, player, "commands.fill.too-big-area", maxFillAmount);
    }

    public static boolean isVoting(Player player, VoteSession session) {
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
        return check(!Cooldowns.canRun(player.uuid(), "sync"), player, "commands.sync.cooldown", Cooldowns.defaults.get("sync"));
    }

    public static boolean isCooldowned(Player player, String command) {
        return check(!Cooldowns.canRun(player.uuid(), command), player, "commands.cooldown", command, Cooldowns.defaults.get(command) / 60L);
    }

    // endregion
    // region Discord

    public static boolean isMenu(SlashContext context) {
        return check(state.isMenu(), context, ":gear: Сервер не запущен.", ":thinking: Почему?");
    }

    public static boolean notAdmin(SlashContext context) {
        return check(!Bot.isAdmin(context.event().getMember()), context, ":no_entry_sign: Эта команда только для администрации.", "У тебя нет прав на ее использование.");
    }

    public static boolean notAdmin(GenericComponentInteractionCreateEvent event) {
        return check(!Bot.isAdmin(event.getMember()), () -> event.replyEmbeds(
                new EmbedBuilder().setColor(Color.red).setTitle(":no_entry_sign: Взаимодействовать с запросами могут только админы.").build()
        ).setEphemeral(true).queue());
    }

    public static boolean notFound(SlashContext context, Map map) {
        return check(map == null, context, ":mag: Карта не найдена.", "Проверь, правильно ли введено название.");
    }

    public static boolean notFound(SlashContext context, Player player) {
        return check(player == null, context, ":mag: Игрок не найден.", "Проверь, правильно ли введен никнейм.");
    }

    public static boolean notMap(SlashContext context) {
        Attachment attachment = context.getOption("map").getAsAttachment();
        return check(!Objects.equals(attachment.getFileExtension(), mapExtension), context, ":link: Неверное вложение.", "Тебе нужно прикрепить один файл с расширением **.msav!**");
    }

    public static boolean notMap(SlashContext context, Fi file) {
        return check(!SaveIO.isSaveValid(file), () -> {
            context.error(":no_entry_sign: Файл поврежден или не является картой!").queue();
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

    private static boolean check(boolean result, SlashContext context, String... values) {
        return check(result, () -> context.error(values[0], values[1]).queue());
    }

    // endregion
}
