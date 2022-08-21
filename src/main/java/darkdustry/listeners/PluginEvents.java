package darkdustry.listeners;

import arc.Events;
import arc.util.Log;
import arc.util.Time;
import darkdustry.discord.Bot;
import darkdustry.features.Alerts;
import darkdustry.features.Effects;
import darkdustry.features.Ranks;
import darkdustry.features.history.*;
import darkdustry.utils.Find;
import mindustry.game.EventType.*;
import mindustry.gen.Groups;
import mindustry.net.Administration.Config;

import java.awt.Color;

import static arc.Core.*;
import static arc.util.Strings.stripColors;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Bundle.*;
import static darkdustry.components.Database.*;
import static darkdustry.components.MenuHandler.*;
import static darkdustry.features.Ranks.*;

public class PluginEvents {

    public static void load() {
        Events.on(AdminRequestEvent.class, event -> {
            switch (event.action) {
                case wave -> sendToChat("events.admin.wave", event.player.coloredName());
                case kick -> sendToChat("events.admin.kick", event.player.coloredName(), event.other.coloredName());
                case ban -> sendToChat("events.admin.ban", event.player.coloredName(), event.other.coloredName());
                default -> {} // без этой строки vscode кидает ошибку
            }
        });

        Events.on(BlockBuildEndEvent.class, event -> {
            if (!event.unit.isPlayer()) return;
            if (History.enabled() && event.tile.build != null) History.put(new BlockEntry(event), event.tile);
            if (event.breaking) return;

            var data = getPlayerData(event.unit.getPlayer());
            data.buildingsBuilt++;
            setPlayerData(data);
        });

        Events.on(BuildSelectEvent.class, event -> {
            if (event.breaking || event.builder == null || event.builder.buildPlan() == null || !event.builder.isPlayer())
                return;
            Alerts.buildAlert(event);
        });

        Events.on(ConfigEvent.class, event -> {
            if (History.enabled() && event.player != null) History.put(new ConfigEntry(event), event.tile.tile);
        });

        Events.on(DepositEvent.class, event -> {
            if (History.enabled() && event.player != null) History.put(new DepositEntry(event), event.tile.tile);
            Alerts.depositAlert(event);
        });

        Events.on(GameOverEvent.class, event -> Groups.player.each(player -> {
            var data = getPlayerData(player.uuid());
            data.gamesPlayed++;
            setPlayerData(data);
        }));

        Events.on(PlayerJoin.class, event -> {
            var data = getPlayerData(event.player);
            Ranks.setRank(event.player, Ranks.getRank(data.rank));

            app.post(() -> Effects.onJoin(event.player));

            Log.info("@ has connected. [@]", event.player.name, event.player.uuid());
            sendToChat("events.player.join", event.player.coloredName());
            bundled(event.player, "welcome.message", Config.serverName.string(), discordServerUrl);

            Bot.sendEmbed(Bot.botChannel, Color.green, "@ присоединился", stripColors(event.player.name));
            app.post(Bot::updateBotStatus);

            if (data.welcomeMessage) showMenu(event.player, welcomeMenu, "welcome.menu.header", "welcome.menu.content",
                    new String[][] {{"ui.menus.close"}, {"welcome.menu.disable"}}, null, Config.serverName.string(), discordServerUrl);
        });

        Events.on(PlayerLeave.class, event -> {
            Effects.onLeave(event.player);

            Log.info("@ has disconnected. [@]", event.player.name, event.player.uuid());
            sendToChat("events.player.leave", event.player.coloredName());
            Bot.sendEmbed(Bot.botChannel, Color.red, "@ отключился", stripColors(event.player.name));

            app.post(Bot::updateBotStatus);

            cache.remove(event.player.uuid());
            activeHistory.remove(event.player.uuid());

            if (vote != null) vote.left(event.player);
            if (voteKick != null) voteKick.left(event.player);
        });

        Events.on(ServerLoadEvent.class, event -> Bot.sendEmbed(Bot.botChannel, Color.yellow, "Сервер запущен."));

        Events.on(TapEvent.class, event -> {
            if (!History.enabled() || !activeHistory.contains(event.player.uuid()) || event.tile == null) return;

            var builder = new StringBuilder(format("history.title", Find.locale(event.player.locale), event.tile.x, event.tile.y));
            var stack = History.get(event.tile.array());

            if (stack.isEmpty()) builder.append(format("history.empty", Find.locale(event.player.locale)));
            else stack.each(entry -> builder.append("\n").append(entry.getMessage(event.player)));

            event.player.sendMessage(builder.toString());
        });

        Events.on(WithdrawEvent.class, event -> {
            if (History.enabled() && event.player != null) History.put(new WithdrawEntry(event), event.tile.tile);
        });

        Events.on(ServerLoadEvent.class, event -> serverLoadTime = Time.millis());

        Events.on(WorldLoadEvent.class, event -> {
            mapLoadTime = Time.millis();

            activeHistory.clear();
            History.clear();
        });

        Events.run(Trigger.update, () -> Groups.player.each(player -> player.unit().moving(), Effects::onMove));
    }
}
