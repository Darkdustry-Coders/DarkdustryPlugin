package darkdustry.listeners;

import arc.Events;
import arc.util.*;
import darkdustry.discord.Bot;
import darkdustry.features.*;
import darkdustry.features.history.*;
import darkdustry.utils.Find;
import mindustry.game.EventType.*;
import mindustry.gen.Groups;

import static arc.Core.app;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Bundle.*;
import static darkdustry.components.MenuHandler.*;
import static darkdustry.components.MongoDB.*;
import static darkdustry.discord.Bot.Palette.*;
import static darkdustry.discord.Bot.*;
import static darkdustry.features.Effects.cache;
import static mindustry.net.Administration.Config.serverName;

public class PluginEvents {

    public static void load() {
        Events.on(BlockBuildEndEvent.class, event -> {
            if (!event.unit.isPlayer() || event.tile.build == null) return;
            if (History.enabled())
                History.put(new BlockEntry(event), event.tile);
            if (event.breaking) return;

            getPlayerData(event.unit.getPlayer().uuid()).subscribe(data -> {
                data.buildingsBuilt++;
                setPlayerData(data).subscribe();
            });
        });

        Events.on(BuildSelectEvent.class, event -> {
            if (event.breaking || event.builder == null || event.builder.buildPlan() == null || !event.builder.isPlayer())
                return;
            Alerts.buildAlert(event);
        });

        Events.on(ConfigEvent.class, event -> {
            if (History.enabled() && event.player != null)
                History.put(new ConfigEntry(event), event.tile.tile);
        });

        Events.on(DepositEvent.class, event -> {
            if (History.enabled() && event.player != null)
                History.put(new DepositEntry(event), event.tile.tile);
            Alerts.depositAlert(event);
        });

        Events.on(GameOverEvent.class, event -> Groups.player.each(player -> getPlayerData(player.uuid()).subscribe(data -> {
            data.gamesPlayed++;
            setPlayerData(data).subscribe();
        })));

        Events.on(PlayerJoin.class, event -> getPlayerData(event.player.uuid()).subscribe(data -> {
            Ranks.setRank(event.player, Ranks.getRank(data.rank));

            app.post(() -> Effects.onJoin(event.player));

            Log.info("@ has connected. [@]", event.player.plainName(), event.player.uuid());
            sendToChat("events.join", event.player.coloredName());
            bundled(event.player, "welcome.message", serverName.string(), discordServerUrl);

            sendEmbed(botChannel, SUCCESS, "@ присоединился", event.player.plainName());

            if (data.welcomeMessage) {
                var locale = Find.locale(event.player.locale);
                var builder = new StringBuilder();

                welcomeMessageCommands.each(command -> builder.append("\n[cyan]").append(clientCommands.getPrefix()).append(command).append("[gray] - [lightgray]").append(get("commands." + command + ".description", locale)));

                showMenu(event.player, welcomeMenu, "welcome.header", "welcome.content",
                        new String[][] {{"ui.button.close"}, {"ui.button.discord"}, {"welcome.button.disable"}},
                        null, serverName.string(), builder.toString());
            }

            app.post(Bot::updateBotStatus);
        }));

        Events.on(PlayerLeave.class, event -> {
            Effects.onLeave(event.player);

            Log.info("@ has disconnected. [@]", event.player.plainName(), event.player.uuid());
            sendToChat("events.leave", event.player.coloredName());
            sendEmbed(botChannel, ERROR, "@ отключился", event.player.plainName());

            cache.remove(event.player.uuid());
            activeHistory.remove(event.player.uuid());

            if (vote != null) vote.left(event.player);
            if (voteKick != null) voteKick.left(event.player);

            app.post(Bot::updateBotStatus);
        });

        Events.on(TapEvent.class, event -> {
            if (!History.enabled() || !activeHistory.contains(event.player.uuid()) || event.tile == null) return;

            var builder = new StringBuilder(format("history.title", Find.locale(event.player.locale), event.tile.x, event.tile.y));
            var stack = History.get(event.tile.array());

            if (stack.isEmpty()) builder.append(format("history.empty", Find.locale(event.player.locale)));
            else stack.each(entry -> builder.append("\n").append(entry.getMessage(event.player)));

            event.player.sendMessage(builder.toString());
        });

        Events.on(WithdrawEvent.class, event -> {
            if (History.enabled() && event.player != null)
                History.put(new WithdrawEntry(event), event.tile.tile);
        });

        Events.on(ServerLoadEvent.class, event -> {
            serverLoadTime = Time.millis();
            sendEmbed(botChannel, INFO, "Сервер запущен");
        });

        Events.on(WorldLoadEvent.class, event -> {
            mapLoadTime = Time.millis();

            activeHistory.clear();
            History.clear();

            app.post(Bot::updateBotStatus);
        });

        Events.run(Trigger.update, () -> Groups.player.each(player -> player != null && player.unit().moving(), Effects::onMove));
    }
}