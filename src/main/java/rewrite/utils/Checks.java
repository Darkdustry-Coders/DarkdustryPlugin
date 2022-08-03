package rewrite.utils;

import mindustry.game.Gamemode;
import mindustry.gen.Player;
import mindustry.maps.Map;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import rewrite.DarkdustryPlugin;
import rewrite.discord.Bot;
import rewrite.discord.MessageContext;

import java.awt.Color;

import static mindustry.Vars.*;
import static rewrite.components.Bundle.*;

public class Checks {

    public static boolean isCooldowned(Player player, String cmd) {
        return check(Cooldowns.runnable(player.uuid(), cmd), player, "commands.cooldown", Cooldowns.defaults.get(cmd) / 60L);
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

    public static boolean notAdmin(MessageContext context) {
        return check(!Bot.isAdmin(context.member), context, ":no_entry_sign: Эта команда только для администрации.", "У тебя нет прав на ее использование.");
    }

    public static boolean notAdmin(ButtonInteractionEvent event) {
        return check(!Bot.isAdmin(event.getMember()), () -> event.replyEmbeds(new EmbedBuilder().setColor(Color.red).setTitle(":no_entry_sign: Взаимодействовать с запросами могут только админы.").build()).setEphemeral(true).queue());
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
