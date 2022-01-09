package pandorum.comp;

import arc.Events;
import arc.util.Log;
import mindustry.core.GameState.State;
import mindustry.core.NetServer;
import mindustry.game.EventType.AdminRequestEvent;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.net.Administration;
import mindustry.net.Administration.PlayerInfo;
import mindustry.net.Administration.TraceInfo;
import mindustry.net.Packets.AdminAction;

import java.io.IOException;
import java.net.BindException;

import static mindustry.Vars.*;
import static pandorum.Misc.*;
import static pandorum.PluginVars.kickDuration;

public class CustomNetServer extends NetServer {

    public static void adminRequest(Player player, Player other, AdminAction action) {
        if (!player.admin || other == null || (other.admin && other != player)) return;

        Events.fire(new AdminRequestEvent(player, other, action));

        if (action == AdminAction.wave) {
            logic.runWave();
            Log.info("&lc@ пропустил волну.", player.name);
            sendToChat("events.admin.wave-skip", player.coloredName());
        } else if (action == AdminAction.ban) {
            netServer.admins.banPlayer(other.uuid());
            other.kick(Bundle.format("events.admin.banned", findLocale(other.locale), player.coloredName()), 0);
            Log.info("&lc@ забанил игрока @.", player.name, other.name);
            sendToChat("events.admin.ban", player.coloredName(), other.coloredName());
        } else if (action == AdminAction.kick) {
            other.kick(Bundle.format("events.admin.kicked", findLocale(other.locale), player.coloredName(), millisecondsToMinutes(kickDuration)), kickDuration);
            Log.info("&lc@ выгнал игрока @.", player.name, other.name);
            sendToChat("events.admin.kick", player.coloredName(), other.coloredName());
        } else if (action == AdminAction.trace) {
            PlayerInfo playerInfo = netServer.admins.getInfo(other.uuid());
            TraceInfo traceInfo = new TraceInfo(other.con.address, other.uuid(), other.con.modclient, other.con.mobile, playerInfo.timesJoined, playerInfo.timesKicked);
            Call.traceInfo(player.con, other, traceInfo);

            Log.info("&lc@ запросил информацию об игроке @.", player.name, other.name);
        }
    }

    public void openServer() {
        try {
            net.host(Administration.Config.port.num());
            Log.info("Сервер запущен на порту @.", Administration.Config.port.num());
        } catch (BindException e) {
            Log.err("Порт уже используется! Убедись, что нет другого сервера, запущенного на этом же порту!");
            state.set(State.menu);
        } catch (IOException e) {
            Log.err(e);
            state.set(State.menu);
        }
    }
}
