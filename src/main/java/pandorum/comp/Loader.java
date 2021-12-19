package pandorum.comp;

import arc.Events;
import arc.util.Log;
import arc.util.Reflect;
import mindustry.core.NetServer;
import mindustry.game.EventType.*;
import mindustry.net.Administration;
import mindustry.net.Packets.ConnectPacket;
import pandorum.PandorumPlugin;
import pandorum.Reflection;
import pandorum.annotations.events.EventListener;
import pandorum.discord.BotMain;
import pandorum.events.*;
import pandorum.events.filters.ActionFilter;
import pandorum.events.filters.ChatFilter;
import pandorum.events.handlers.ConnectPacketHandler;
import pandorum.events.handlers.InvalidCommandResponse;
import pandorum.events.handlers.MenuHandler;

import static mindustry.Vars.net;
import static mindustry.Vars.netServer;

public class Loader {
    public static void init() {
        PandorumPlugin.writeBuffer = Reflect.get(NetServer.class, netServer, "writeBuffer");
        PandorumPlugin.outputBuffer = Reflect.get(NetServer.class, netServer, "outputBuffer");

        net.handleServer(ConnectPacket.class, ConnectPacketHandler::handle);

        netServer.admins.addActionFilter(ActionFilter::filter);
        netServer.admins.addChatFilter(ChatFilter::filter);
        netServer.invalidHandler = InvalidCommandResponse::response;

        Reflection.getEventsMethods("pandorum.events").each(method -> {
            EventListener annotation = method.getAnnotation(EventListener.class);
            Events.on(annotation.eventType(), event -> {
                try {
                    method.invoke(null, event);
                } catch (Exception e) { Log.err(e.getMessage()); }
            });
            Log.info("Registred event " + annotation.eventType().toString());
        });

        Events.run(Trigger.update, TriggerUpdateListener::update);

        Administration.Config.motd.set("off");
        Administration.Config.interactRateWindow.set(3);
        Administration.Config.interactRateLimit.set(50);
        Administration.Config.interactRateKick.set(1000);
        Administration.Config.showConnectMessages.set(false);
        Administration.Config.strict.set(true);
        Administration.Config.enableVotekick.set(true);

        MenuHandler.init();
        Icons.init();
        BotMain.start();
    }
}
