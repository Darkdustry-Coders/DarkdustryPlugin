package rewrite.utils;

import arc.files.Fi;
import mindustry.game.Gamemode;
import mindustry.gen.Player;
import mindustry.io.SaveIO;
import mindustry.maps.Map;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import rewrite.DarkdustryPlugin;
import rewrite.discord.Bot;
import rewrite.discord.MessageContext;

import java.awt.Color;
import java.util.List;

import static arc.util.Strings.*;
import static mindustry.Vars.*;
import static rewrite.components.Bundle.*;

public class Checks {

    public static boolean isCooldowned(Player player, String cmd) {
        return check(Cooldowns.canRun(player.uuid(), cmd), player, "commands.cooldown", Cooldowns.defaults.get(cmd) / 60L);
    }

    public static boolean isLanuched() {
        return check(!state.isMenu(), "Сервер уже запущен.");
    }

    public static boolean isMenu(MessageContext context) {
        return check(state.isMenu(), context, ":gear: Сервер не запущен.", ":thinking: Почему?");
    }

    public static boolean isAdmin(Player player) {
        return check(player.admin, player, "commands.login.already-admin");
    }

    public static boolean notFound(Gamemode mode, String[] name) {
        return check(mode == null, "Режим игры @ не найден.", name[1]);
    }

    public static boolean notFound(Map map, String name[]) {
        return check(map == null, "Карта @ не найдена.", name[0]);
    }

    public static boolean notFound(MessageContext context, Map map) {
        return check(map == null, context, ":mag: Карта не найдена.", "Проверь, правильно ли введено название.");
    }

    public static boolean notFound(MessageContext context, Player player){
        return check(player == null, context, ":mag: Игрок не найден.", "Проверь, правильно ли введен никнейм.");
    }

    public static boolean notAdmin(MessageContext context) {
        return check(!Bot.isAdmin(context.member), context, ":no_entry_sign: Эта команда только для администрации.", "У тебя нет прав на ее использование.");
    }

    public static boolean notAdmin(ButtonInteractionEvent event) {
        return check(!Bot.isAdmin(event.getMember()), () -> event.replyEmbeds(new EmbedBuilder().setColor(Color.red).setTitle(":no_entry_sign: Взаимодействовать с запросами могут только админы.").build()).setEphemeral(true).queue());
    }

    public static boolean notPage(Player player, String[] page) {
        return check(page.length > 0 && !canParseInt(page[0]), player, "commands.page-not-int");
    }

    public static boolean notPageDs(MessageContext context, String[] page) {
        return check(page.length > 0 && !canParseInt(page[0]), context, ":interrobang: Страница должна быть числом.", "Зачем ты это делаешь?");
    }

    public static boolean notPageDs(MessageContext context, int page, int pages) {
        return check(--page >= pages || page < 0, context, ":interrobang: Неверная страница.", "Страница должна быть числом от 1 до " + pages);
    }

    public static boolean notMap(MessageContext context) {
        List<Attachment> attachments = context.message.getAttachments();
        return check(attachments.size() != 1 || !attachments.get(0).getFileExtension().equals(mapExtension), context, ":link: Неверное вложение.", "Тебе нужно прикрепить один файл с расширением **.msav!**");
    }

    public static boolean notMap(MessageContext context, Fi file) {
        return check(!SaveIO.isSaveValid(file), () -> {
            context.err(":no_entry_sign: Файл поврежден или не является картой!");
            file.delete();
        });
    }

    private static boolean check(boolean result, Runnable todo) {
        if (result) todo.run();
        return result; // я знаю, кринж, не бейте
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
