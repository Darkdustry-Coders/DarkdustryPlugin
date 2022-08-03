package rewrite.commands;

import arc.Events;
import arc.func.Boolp;
import arc.struct.Seq;
import arc.util.CommandHandler.Command;
import arc.util.CommandHandler.CommandRunner;
import mindustry.game.EventType.GameOverEvent;
import mindustry.gen.Groups;
import mindustry.net.Administration.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import pandorum.components.MapParser;
import rewrite.components.Config.Gamemode;
import rewrite.discord.MessageContext;

import java.awt.Color;

import static arc.Core.*;
import static mindustry.Vars.*;
import static rewrite.PluginVars.*;
import static rewrite.utils.Checks.*;
import static rewrite.utils.Utils.*;

public enum DiscordCommands implements CommandRunner<MessageContext> {
    help("Список всех команд.", (args, context) -> {
        Seq<Command> commandsList = discordCommands.getCommandList();
        StringBuilder commands = new StringBuilder();

        for (Command command : commandsList) {
            commands.append(discordCommands.getPrefix()).append("**").append(command.text).append("**");
            if (!command.paramText.isEmpty()) commands.append(" *").append(command.paramText).append("*");
            commands.append(" - ").append(command.description).append("\n");
        }

        context.info(":newspaper: Доступные команды:", commands.toString());
    }),
    ip("IP адрес сервера.", (args, context) -> {
        context.info(":desktop: " + stripAll(Config.serverName.string()), "IP: @:@", config.hubIp, Config.port.num());
    }),
    players("Список всех игроков на сервере.", "[страница]", (args, context) -> {

    }),
    status("Состояние сервера.", (args, context) -> {
        if (isMenu(context)) return;
        context.channel.sendMessageEmbeds(new EmbedBuilder()
                .setColor(Color.green)
                .setTitle(":desktop: " + stripAll(Config.serverName.string()))
                .addField("Игроков:", String.valueOf(Groups.player.size()), true)
                .addField("Карта:", state.map.name(), true)
                .addField("Волна:", String.valueOf(state.wave), true)
                .addField("TPS:", String.valueOf(graphics.getFramesPerSecond()), true)
                .addField("До следующей волны:", formatDuration((int) state.wavetime / 60 * 1000L), true)
                .setImage("attachment://minimap.png").build()
        ).addFile(MapParser.parseTiles(world.tiles), "minimap.png").queue();
    }),
    kick("Выгнать игрока с сервера.", "<ID/никнейм...>", (args, context) -> {

    }),
    ban("Забанить игрока на сервере.", "<ID/никнейм...>", (args, context) -> {

    }),
    map("Получить карту с сервера.",  "<название...>", (args, context) -> {

    }, () -> config.mode != Gamemode.hexed),
    maps("Список всех карт сервера.", "[страница]", (args, context) -> {

    }, () -> config.mode != Gamemode.hexed),
    addmap("Добавить карту на сервер.", (args, context) -> {

    }, () -> config.mode != Gamemode.hexed),
    removemap("Удалить карту с сервера.",  "<название...>", (args, context) -> {

    }, () -> config.mode != Gamemode.hexed),
    gameover("Принудительно завершить игру.", (args, context) -> {
        if (isMenu(context)) return;
        if (notAdmin(context)) return;

        Events.fire(new GameOverEvent(state.rules.waveTeam));
        context.success(":map: Игра успешно завершена.");
    }, config.mode::isDefault);

    public final String description;
    public final String params;
    private final CommandRunner<MessageContext> runner;
    private final Boolp enabled;

    DiscordCommands(String description, CommandRunner<MessageContext> runner) {
        this(description, "", runner, () -> true);
    }

    DiscordCommands(String description, CommandRunner<MessageContext> runner, Boolp enabled) {
        this(description, "", runner, enabled);
    }

    DiscordCommands(String description, String params, CommandRunner<MessageContext> runner) {
        this(description, params, runner, () -> true);
    }

    DiscordCommands(String description, String params, CommandRunner<MessageContext> runner, Boolp enabled) {
        this.description = description;
        this.params = params;
        this.runner = runner;
        this.enabled = enabled;
    }

    @Override
    public void accept(String[] args, MessageContext context) {
        runner.accept(args, context);
    }

    public boolean enabled() {
        return enabled.get();
    }
}