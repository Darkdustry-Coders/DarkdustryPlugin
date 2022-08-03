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

    public static boolean isLanuched() {
        if (!state.isMenu()) DarkdustryPlugin.error("Сервер уже запущен.");
        return !state.isMenu();
    }

    public static boolean isMenu(MessageContext context) {
        if (state.isMenu()) context.err(":gear: Сервер не запущен.", ":thinking: Почему?");
        return state.isMenu();
    }

    public static boolean isAdmin(Player player) {
        if (player.admin) bundled(player, "commands.login.already-admin");
        return player.admin;
    }

    public static boolean notFound(Gamemode mode, String[] name) {
        if (mode == null) DarkdustryPlugin.error("Режим игры '@' не найден.", name[1]);
        return mode == null;
    }

    public static boolean notFound(Map map, String name) {
        if (map == null) DarkdustryPlugin.error("Карта '@' не найдена.", name);
        return map == null;
    }

    public static boolean notAdmin(MessageContext context) {
        if (!Bot.isAdmin(context.member)) context.err(":no_entry_sign: Эта команда только для администрации.", "У тебя нет прав на ее использование.");
        return !Bot.isAdmin(context.member);
    }

    public static boolean notAdmin(ButtonInteractionEvent event) {
        if (!Bot.isAdmin(event.getMember())) event.replyEmbeds(new EmbedBuilder().setColor(Color.red).setTitle(":no_entry_sign: Взаимодействовать с запросами могут только админы.").build()).setEphemeral(true).queue();
        return !Bot.isAdmin(event.getMember());
    }
}
