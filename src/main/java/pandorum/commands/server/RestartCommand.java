package pandorum.commands.server;

import arc.util.Log;
import arc.util.Time;
import discord4j.core.spec.EmbedCreateSpec;
import mindustry.net.Packets.KickReason;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;

import static mindustry.Vars.netServer;

public class RestartCommand {
    public static void run(final String[] args) {
        Log.info("Перезапуск сервера...");

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(BotMain.errorColor)
                .title("Сервер выключился для перезапуска!")
                .build();

        BotHandler.sendEmbed(embed);

        netServer.kickAll(KickReason.serverRestarting);
        Time.runTask(10f, () -> System.exit(2));
    }
}
