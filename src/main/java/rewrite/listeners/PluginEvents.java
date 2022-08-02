package rewrite.listeners;

import arc.struct.ObjectMap;
import mindustry.game.EventType.*;
import mindustry.gen.Call;
import mindustry.net.Administration.Config;
import rewrite.DarkdustryPlugin;
import rewrite.components.Database.PlayerData;
import rewrite.features.Effects;
import rewrite.features.Ranks;
import rewrite.utils.Find;

import static pandorum.listeners.handlers.MenuHandler.*; // TODO: заменить импорт из пандорума
import static rewrite.PluginVars.*;
import static rewrite.components.Bundle.*;
import static rewrite.components.Database.*;

import java.util.Locale;

@SuppressWarnings("unchecked")
public class PluginEvents {

    public static ObjectMap<Class<?>, Event<?>> events = new ObjectMap<>();
    public static Event<GameOverEvent> gameover;

    public static void load() {
        register(AdminRequestEvent.class, event -> {
            switch (event.action) {
                case wave -> sendToChat("events.admin.wave", event.player.name);
                case kick -> sendToChat("events.admin.kick", event.player.name, event.other.name);
                case ban -> sendToChat("events.admin.ban", event.player.name, event.other.name);
                default -> {} // без этой строки vscode кидает ошибку
            }
        });
        register(BlockBuildEndEvent.class, event -> {});
        register(BuildSelectEvent.class, event -> {});
        register(ConfigEvent.class, event -> {});
        register(DepositEvent.class, event -> {});
        register(GameOverEvent.class, event -> {});
        register(PlayerJoin.class, event -> {
            PlayerData data = getPlayerData(event.player.uuid());
            Ranks.setRank(event.player, Ranks.getRank(data.rank));

            Effects.onJoin(event.player);
            DarkdustryPlugin.info("@ зашел на сервер. [@]", event.player.name, event.player.uuid());
            sendToChat("events.player.join", event.player.name);
            bundled(event.player, "welcome.message", Config.serverName.string(), discordServerUrl);
        
            // Bot.sendEmbed(botChannel, Color.green, "@ присоединился", Strings.stripColors(event.player.name));
            // app.post(Bot::updateBotStatus); // TODO: добавить бота

            Locale locale = Find.locale(event.player.locale);
            if (data.welcomeMessage) Call.menu(event.player.con, welcomeMenu,
                    format("welcome.menu.header", locale),
                    format("welcome.menu.content", locale, Config.serverName.string(), discordServerUrl),
                    new String[][] { { format("ui.menus.close", locale) }, { format("welcome.menu.disable", locale) } });
        });
        register(PlayerLeave.class, event -> {
            Effects.onLeave(event.player);
            DarkdustryPlugin.info("@ вышел с сервера. [@]", event.player.name, event.player.uuid());
            sendToChat("events.player.leave", event.player.name);

            // Bot.sendEmbed(botChannel, Color.red, "@ отключился", Strings.stripColors(event.player.name));
            // app.post(Bot::updateBotStatus);

            // activeHistoryPlayers.remove(event.player.uuid());
            // activeSpectatingPlayers.remove(event.player.uuid());

            // if (currentVoteKick != null && event.player == currentVoteKick.target()) {
            //     currentVoteKick.stop();
            //     netServer.admins.handleKicked(event.player.uuid(), event.player.ip(), kickDuration);
            //     sendToChat("commands.votekick.left", event.player.name, kickDuration / 60000);
            // }

            //     if (config.mode == Gamemode.pvp) {
            //         Seq<String> teamVotes = votesSurrender.get(event.player.team(), Seq::new);
            //         if (teamVotes.remove(event.player.uuid())) {
            //             sendToChat("commands.surrender.left", coloredTeam(event.player.team()), event.player.name, teamVotes.size, Mathf.ceil(voteRatio * Groups.player.count(p -> p.team() == event.player.team())));
            //         }
            //     }

            // if (votesRtv.remove(event.player.uuid())) TODO: переделать эту ср*нь
            //     sendToChat("commands.rtv.left", event.player.name, votesRtv.size, Mathf.ceil(voteRatio * Groups.player.size()));

            // if (votesVnw.remove(event.player.uuid())) 
            //     sendToChat("commands.vnw.left", event.player.name, votesVnw.size, Mathf.ceil(voteRatio * Groups.player.size()));
        });
        register(ServerLoadEvent.class, event -> {});
        register(TapEvent.class, event -> {});
        register(WithdrawEvent.class, event -> {});
        register(WorldLoadEvent.class, event -> {});
    }

    private static <T> void register(Class<T> type, Event<T> event){
        events.put(type, event);
    }

    public interface Event<T> extends Runnable {

        void get(T event);

        @Override
        default void run() {
            get(null);
        }

        default void listener(Object event) {
            get((T) event);
        }
    }
}
