package pandorum.commands.server;

import arc.util.Log;
import arc.util.Time;
import discord4j.core.spec.EmbedCreateSpec;
import mindustry.net.Packets.KickReason;

import static mindustry.Vars.netServer;
import static pandorum.discord.Bot.errorColor;
import static pandorum.discord.Bot.sendEmbed;

public class RestartCommand {
    public static void run(final String[] args) {
        Log.info("Сервер перезапускается...");
        sendEmbed(EmbedCreateSpec.builder().color(errorColor).title("Сервер выключается для перезапуска!").build());

        netServer.kickAll(KickReason.serverRestarting);
        Time.runTask(60f, () -> System.exit(2));
    }
}
