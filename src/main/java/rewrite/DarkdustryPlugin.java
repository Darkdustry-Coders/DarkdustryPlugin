// Rewrites are always better.
// (C) Skat, 2021 год до н. э.

package rewrite;

import arc.Events;
import arc.files.Fi;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Strings;
import mindustry.core.Version;
import mindustry.game.EventType.Trigger;
import mindustry.gen.Groups;
import mindustry.io.JsonIO;
import mindustry.mod.Plugin;
import rewrite.commands.ClientCommands;
import rewrite.commands.DiscordCommands;
import rewrite.commands.ServerCommands;
import rewrite.components.Config;
import rewrite.components.Database;
import rewrite.features.Effects;
import rewrite.features.Ranks;
import rewrite.listeners.PluginEvents;

import static mindustry.Vars.*;
import static rewrite.PluginVars.*;
import static rewrite.components.Bundle.*;
import static rewrite.listeners.PluginEvents.*;

@SuppressWarnings("unused")
public class DarkdustryPlugin extends Plugin {

    @Override
    public void init() {
        Fi configFile = dataDirectory.child(configFileName);
        if (configFile.exists()) {
            config = JsonIO.json.fromJson(Config.class, configFile.reader());
            info("Конфигурация загружена. (@)", configFile.absolutePath());
        } else {
            configFile.writeString(JsonIO.json.toJson(config = new Config()));
            info("Файл конфигурации сгенерирован. (@)", configFile.absolutePath());
        }

        Ranks.load();
        PluginEvents.load();

        // Database.connect("", config.dbUser, config.dbPassword);

        Version.build = -1;

        events.each(event -> Events.on(event.type(), event.listener()));
        Events.run(Trigger.update, () -> Groups.player.each(player -> player.unit().moving(), Effects::onMove));

        Events.run("HexedGameOver", gameover::run);
        Events.run("CastleGameOver", gameover::run);
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        for (ClientCommands command : ClientCommands.values())
            if (command.enabled()) handler.register(command.name(), get(command.params, ""), get(command.description, ""), command);
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        for (ServerCommands command : ServerCommands.values()) handler.register(command.name(), command.params, command.description, command);
    }

    public void registerDiscordCommands(CommandHandler handler) {
        for (DiscordCommands command : DiscordCommands.values()) handler.register(command.name(), command.params, command.description, command);
    }

    public static void info(String text, Object... values) {
        Log.infoTag("Darkdustry", Strings.format(text, values));
    }

    public static void error(String text, Object... values) {
        Log.errTag("Darkdustry", Strings.format(text, values));
    }
}
