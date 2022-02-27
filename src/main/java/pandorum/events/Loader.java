package pandorum.events;

import arc.Events;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Reflect;
import arc.util.Timer;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.core.NetServer;
import mindustry.core.Version;
import mindustry.game.EventType.*;
import mindustry.net.Administration.Config;
import mindustry.net.Packets.Connect;
import mindustry.net.Packets.ConnectPacket;
import pandorum.components.Icons;
import pandorum.components.MapParser;
import pandorum.components.Ranks;
import pandorum.components.Translator;
import pandorum.discord.Bot;
import pandorum.events.filters.ActionFilter;
import pandorum.events.filters.ChatFilter;
import pandorum.events.handlers.ConnectHandler;
import pandorum.events.handlers.ConnectPacketHandler;
import pandorum.events.handlers.InvalidCommandResponse;
import pandorum.events.handlers.MenuHandler;
import pandorum.events.listeners.*;

import static mindustry.Vars.net;
import static mindustry.Vars.netServer;
import static pandorum.PluginVars.*;

public class Loader {

    public static void init() {
        writeBuffer = Reflect.get(NetServer.class, netServer, "writeBuffer");
        outputBuffer = Reflect.get(NetServer.class, netServer, "outputBuffer");

        net.handleServer(Connect.class, ConnectHandler::handle);
        net.handleServer(ConnectPacket.class, ConnectPacketHandler::handle);

        netServer.admins.addActionFilter(ActionFilter::filter);
        netServer.admins.addChatFilter(ChatFilter::filter);
        netServer.invalidHandler = InvalidCommandResponse::response;

        Events.on(AdminRequestEvent.class, AdminRequestListener::call);
        Events.on(BlockBuildEndEvent.class, BlockBuildEndListener::call);
        Events.on(BuildSelectEvent.class, BuildSelectListener::call);
        Events.on(ConfigEvent.class, ConfigListener::call);
        Events.on(DepositEvent.class, DepositListener::call);
        Events.on(PlayerJoin.class, PlayerJoinListener::call);
        Events.on(PlayerLeave.class, PlayerLeaveListener::call);
        Events.on(TapEvent.class, TapListener::call);
        Events.on(WithdrawEvent.class, WithdrawListener::call);

        Events.on(GameOverEvent.class, event -> GameOverListener.call());
        Events.on(ServerLoadEvent.class, event -> ServerLoadListener.call());
        Events.on(WaveEvent.class, event -> WaveListener.call());
        Events.on(WorldLoadEvent.class, event -> WorldLoadListener.call());

        Events.run(Trigger.update, TriggerUpdateListener::update);
        Events.run("HexedGameOver", GameOverListener::call);
        Events.run("CastleGameOver", GameOverListener::call);

        Config.motd.set("off");
        Config.interactRateWindow.set(3);
        Config.interactRateLimit.set(50);
        Config.interactRateKick.set(1000);
        Config.showConnectMessages.set(false);
        Config.logging.set(true);
        Config.strict.set(true);
        Config.enableVotekick.set(true);

        Version.build = -1;

        Translator.loadLanguages();

        MenuHandler.init();
        Icons.init();
        MapParser.init();
        Ranks.init();
        Bot.init();

        dangerousBuildBlocks = Seq.with(Blocks.incinerator, Blocks.thoriumReactor);
        dangerousDepositBlocks = ObjectMap.of(Blocks.combustionGenerator, Items.blastCompound, Blocks.steamGenerator, Items.blastCompound, Blocks.thoriumReactor, Items.thorium);

        Timer.schedule(Updater::update, 0f, 1f);

        Log.info("[Darkdustry] Инициализация плагина завершена...");
    }
}
