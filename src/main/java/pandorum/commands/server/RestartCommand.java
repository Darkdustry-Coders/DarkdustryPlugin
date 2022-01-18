package pandorum.commands.server;

import arc.util.Log;
import arc.util.Timer;
import discord4j.core.spec.EmbedCreateSpec;
import mindustry.gen.Groups;
import mindustry.net.Packets.KickReason;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;

import static mindustry.Vars.netServer;
import static pandorum.Misc.bundled;

public class RestartCommand {
    public static void run(final String[] args) {
        boolean isCarefullyRestart = args[0] == "force" ? false : true;

        BotHandler.sendEmbed(EmbedCreateSpec.builder().color(BotMain.errorColor).title("Сервер выключился для перезапуска!").build());

        if (isCarefullyRestart) {
            Log.err("Сервер будет перезапущен при отсутствии игроков...");
            Groups.player.each(player -> bundled(player, "commands.restart.carefully"));

            Timer.schedule(() -> {
                if (Groups.player.size() != 0) return;
                Log.err("Перезапуск сервера...");
                System.exit(0);
            }, 0, 1).run();
        } else {
            Log.err("Перезапуск сервера...");
            netServer.kickAll(KickReason.serverRestarting);
            System.exit(0);
        }
    }
}
