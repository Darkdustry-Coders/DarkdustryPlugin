package pandorum.comp;

import arc.Events;
import arc.util.Log;
import arc.util.Reflect;
import mindustry.core.NetServer;
import mindustry.net.Administration;
import mindustry.net.Packets.ConnectPacket;
import pandorum.PandorumPlugin;
import pandorum.Reflection;
import pandorum.annotations.events.EventListener;
import pandorum.annotations.handlers.PacketHandler;
import pandorum.annotations.events.TriggerListener;
import pandorum.discord.BotMain;
import pandorum.events.filters.ActionFilter;
import pandorum.events.filters.ChatFilter;
import pandorum.events.handlers.ConnectPacketHandler;
import pandorum.events.handlers.InvalidCommandResponse;
import pandorum.events.handlers.MenuHandler;

import java.lang.reflect.Method;

import static mindustry.Vars.net;
import static mindustry.Vars.netServer;

public class Loader {
    private static void RegisterEventListener(Method method) {
        EventListener annotation = method.getAnnotation(EventListener.class);
        Events.on(annotation.eventType(), event -> {
            try {
                method.invoke(null, event);
            } catch (Exception e) { Log.err(e.getMessage()); }
        });
        Log.info("Registered event " + annotation.eventType().toString());
    }

    private static void RegisterTriggerListener(Method method) {
        TriggerListener annotation = method.getAnnotation(TriggerListener.class);
        Events.run(annotation.trigger(), () -> {
            try {
                method.invoke(null);
            } catch (Exception e) { Log.err(e.getMessage()); }
        });
        Log.info("Registered trigger listener " + annotation.trigger().toString());
    }

    private static void RegisterPacketHandler(Method method) {
        PacketHandler annotation = method.getAnnotation(PacketHandler.class);
        net.handleServer(annotation.packetType(), (conn, packet) -> {
            try {
                method.invoke(null, conn, packet);
            } catch (Exception e)  {Log.err(e); }
        });
    }

    private static void RegisterActionFilter(Method method) {
        netServer.admins.addActionFilter((Administration.PlayerAction action) -> {
            try {
                return (boolean) method.invoke(null, action);
            } catch (Exception e)  { Log.err(e); return true; }
        });
    }

    private static void RegisterChatFilter(Method method) {
        netServer.admins.addChatFilter((player, text) -> {
            try {
                return (String) method.invoke(null, player, text);
            } catch (Exception e)  { Log.err(e); return text; }
        });
    }

    public static void init() {
        PandorumPlugin.writeBuffer = Reflect.get(NetServer.class, netServer, "writeBuffer");
        PandorumPlugin.outputBuffer = Reflect.get(NetServer.class, netServer, "outputBuffer");

        netServer.invalidHandler = InvalidCommandResponse::response;

        Reflection.getEventListenersMethods().each(Loader::RegisterEventListener);
        Reflection.getTriggerListenersMethods().each(Loader::RegisterTriggerListener);
        Reflection.getPacketHandlersMethods().each(Loader::RegisterPacketHandler);
        Reflection.getActionFiltersMethods().each(Loader::RegisterActionFilter);
        Reflection.getChatFiltersMethods().each(Loader::RegisterChatFilter);

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
