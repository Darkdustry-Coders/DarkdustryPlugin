package pandorum.commands.server;

import arc.Core;
import arc.func.Cons;
import arc.util.Log;
import arc.util.Time;
import mindustry.net.Packets.KickReason;
import pandorum.discord.Bot;

import java.awt.*;

import static mindustry.Vars.netServer;

public class RestartCommand implements Cons<String[]> {
    public void get(String[] args) {
        Log.info("Сервер перезапускается...");
        Bot.sendEmbed(Color.red, "Сервер перезапускается...");

        netServer.kickAll(KickReason.serverRestarting);
        Time.runTask(60f, () -> Core.app.exit());
    }
}
