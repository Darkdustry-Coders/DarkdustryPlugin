package pandorum.comp;

import arc.Events;
import arc.struct.Seq;
import arc.util.ArcRuntimeException;
import arc.util.Timer;
import arc.util.io.Streams;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.net.Administration;
import org.javacord.api.entity.activity.ActivityType;
import pandorum.PandorumPlugin;
import pandorum.comp.effects.Effects;
import pandorum.discord.BotMain;
import pandorum.events.*;
import pandorum.events.filters.ActionFilter;
import pandorum.events.filters.ChatFilter;

import java.io.InputStream;
import java.util.Objects;

import static mindustry.Vars.netServer;
import static pandorum.discord.BotMain.bot;

public class Loader {
    public static void init() {
        try {
            InputStream stream = Loader.class.getClassLoader().getResourceAsStream("vpn-ipv4.txt");
            Objects.requireNonNull(stream, "stream");
            PandorumPlugin.forbiddenIps = Seq.with(Streams.copyString(stream).split(System.lineSeparator())).map(IpInfo::new);
        } catch(Exception e) {
            throw new ArcRuntimeException(e);
        }

        Administration.Config.showConnectMessages.set(false);
        Administration.Config.strict.set(true);
        Administration.Config.motd.set("off");
        Administration.Config.messageRateLimit.set(1);
        Administration.Config.enableVotekick.set(true);

        netServer.admins.addActionFilter(ActionFilter::filter);
        netServer.admins.addChatFilter(ChatFilter::filter);
        netServer.invalidHandler = InvalidCommandResponse::response;

        Events.on(EventType.PlayerUnbanEvent.class, PlayerUnbanListener::call);
        Events.on(EventType.PlayerBanEvent.class, PlayerBanListener::call);
        Events.on(EventType.ServerLoadEvent.class, ServerLoadListener::call);
        Events.on(EventType.WorldLoadEvent.class, WorldLoadListener::call);
        Events.on(EventType.BlockBuildEndEvent.class, BlockBuildEndListener::call);
        Events.on(EventType.ConfigEvent.class, ConfigListener::call);
        Events.on(EventType.TapEvent.class, TapListener::call);
        Events.on(EventType.DepositEvent.class, DepositListener::call);
        Events.on(EventType.WithdrawEvent.class, WithdrawListener::call);
        Events.on(EventType.BuildSelectEvent.class, BuildSelectListener::call);
        Events.on(EventType.PlayerJoin.class, PlayerJoinListener::call);
        Events.on(EventType.PlayerLeave.class, PlayerLeaveListener::call);
        Events.on(EventType.GameOverEvent.class, GameOverListener::call);
        Events.on(EventType.WaveEvent.class, WaveEventListener::call);
        Events.on(EventType.AdminRequestEvent.class, AdminRequestListener::call);
        Events.run(EventType.Trigger.update, TriggerUpdateListener::update);

        Effects.init();
        MenuListener.init();
        Icons.init();
        Ranks.init();

        BotMain.run();

        Timer.schedule(() -> bot.updateActivity(ActivityType.WATCHING, (Groups.player.size() + (Groups.player.size() % 10 == 1 && Groups.player.size() != 11 ? " игрок на сервере." : " игроков на сервере."))), 0f, 10f);
    }
}
