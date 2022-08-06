package rewrite.utils;

import arc.files.Fi;
import mindustry.game.Gamemode;
import mindustry.gen.Player;
import mindustry.io.SaveIO;
import mindustry.maps.Map;
import mindustry.net.Administration.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import rewrite.DarkdustryPlugin;
import rewrite.discord.Bot;
import rewrite.discord.MessageContext;

import java.awt.*;
import java.util.List;
import java.util.Objects;

import static arc.util.Strings.canParseInt;
import static mindustry.Vars.mapExtension;
import static mindustry.Vars.state;
import static rewrite.PluginVars.translatorLanguages;
import static rewrite.PluginVars.vote;
import static rewrite.components.Bundle.bundled;

public class Checks { // TODO: рефакторнуть

    public static boolean alreadySynced(Player player) {
        return check(!Cooldowns.canRun(player.uuid(), "sync"), player, "commands.sync.cooldown", Cooldowns.defaults.get("sync"));
    }

    public static boolean isCooldowned(Player player, String command) {
        return check(!Cooldowns.canRun(player.uuid(), command), player, "commands.cooldown", Cooldowns.defaults.get(command) / 60L);
    }

    public static boolean isLaunched() {
        return check(!state.isMenu(), "Сервер уже запущен.");
    }

    public static boolean isMenu(MessageContext context) {
        return check(state.isMenu(), context, ":gear: Сервер не запущен.", ":thinking: Почему?");
    }

    public static boolean alreadyVoted(Player player) {
        return check(vote.voted.contains(player.uuid()), player, "commands.already-voted");
    }

    public static boolean isVoting(Player player) {
        return check(vote != null, player, "commands.vote-already-started");
    }

    public static boolean votekickDisabled(Player player) {
        return check(!Config.enableVotekick.bool(), player, "commands.votekick.disabled");
    }

    public static boolean invalidVoteTarget(Player player, Player target) {
        return check(target == player, player, "commands.votekick.player-is-you") || check(target.admin, player, "commands.votekick.player-is-admin") || check(target.team() != player.team(), player, "commands.votekick.player-is-enemy");
    }

    public static boolean invalidVoteSign(Player player, int sign) {
        return check(sign == 0, player, "commands.vote.incorrect-sign");
    }

    public static boolean alreadyAdmin(Player player) {
        return check(player.admin, player, "commands.login.already-admin");
    }

    public static boolean notVoting(Player player) {
        return check(vote == null, player, "commands.no-voting");
    }

    public static boolean notFound(Player player, String language) {
        return check(!translatorLanguages.containsKey(language), player, "commands.tr.not-found");
    }

    public static boolean notFound(Gamemode mode, String[] name) {
        return check(mode == null, "Режим игры @ не найден.", name[1]);
    }

    public static boolean notFound(Map map, String[] name) {
        return check(map == null, "Карта @ не найдена.", name[0]);
    }

    public static boolean notFound(Player player, Map map) {
        return check(map == null, player, "commands.nominate.map.not-found");
    }

    public static boolean notFound(Player player, Fi file) {
        return check(file == null, player, "commands.nominate.load.not-found");
    }

    public static boolean notFound(Player player, Player target, String name) {
        return check(target == null, player, "commands.player-not-found", name);
    }

    public static boolean notFound(MessageContext context, Map map) {
        return check(map == null, context, ":mag: Карта не найдена.", "Проверь, правильно ли введено название.");
    }

    public static boolean notFound(MessageContext context, Player player) {
        return check(player == null, context, ":mag: Игрок не найден.", "Проверь, правильно ли введен никнейм.");
    }

    public static boolean notAdmin(Player player) {
        return check(!player.admin, player, "commands.permission-denied");
    }

    public static boolean notAdmin(MessageContext context) {
        return check(!Bot.isAdmin(context.member), context, ":no_entry_sign: Эта команда только для администрации.", "У тебя нет прав на ее использование.");
    }

    public static boolean notAdmin(ButtonInteractionEvent event) {
        return check(!Bot.isAdmin(event.getMember()), () -> event.replyEmbeds(new EmbedBuilder().setColor(Color.red).setTitle(":no_entry_sign: Взаимодействовать с запросами могут только админы.").build()).setEphemeral(true).queue());
    }

    public static boolean invalidPage(MessageContext context, String[] page) {
        return check(page.length > 0 && !canParseInt(page[0]), context, ":interrobang: Страница должна быть числом.", "Зачем ты это делаешь?");
    }

    public static boolean invalidPage(MessageContext context, int page, int pages) {
        return check(--page >= pages || page < 0, context, ":interrobang: Неверная страница.", "Страница должна быть числом от 1 до " + pages);
    }

    public static boolean notMap(MessageContext context) {
        List<Attachment> attachments = context.message.getAttachments();
        return check(attachments.size() != 1 || !Objects.equals(attachments.get(0).getFileExtension(), mapExtension), context, ":link: Неверное вложение.", "Тебе нужно прикрепить один файл с расширением **.msav!**");
    }

    public static boolean notMap(MessageContext context, Fi file) {
        return check(!SaveIO.isSaveValid(file), () -> {
            context.err(":no_entry_sign: Файл поврежден или не является картой!");
            file.delete();
        });
    }

    private static boolean check(boolean result, Runnable todo) {
        if (result) todo.run();
        return result;
    }

    private static boolean check(boolean result, String error, Object... values) {
        return check(result, () -> DarkdustryPlugin.error(error, values));
    }

    private static boolean check(boolean result, Player player, String key, Object... values) {
        return check(result, () -> bundled(player, key, values));
    }

    private static boolean check(boolean result, MessageContext context, String... values) {
        return check(result, () -> context.err(values[0], values[1]));
    }
}
