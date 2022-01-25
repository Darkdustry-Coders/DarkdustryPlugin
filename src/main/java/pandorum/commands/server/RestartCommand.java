package pandorum.commands.server;

import arc.util.Log;
import arc.util.Time;
import mindustry.net.Packets.KickReason;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

import static mindustry.Vars.netServer;
import static pandorum.discord.Bot.botChannel;

public class RestartCommand {
    public static void run(final String[] args) {
        Log.info("Сервер перезапускается...");
        botChannel.sendMessageEmbeds(new EmbedBuilder().setTitle("Сервер выключается для перезапуска!").setColor(Color.red).build()).queue();

        netServer.kickAll(KickReason.serverRestarting);
        Time.runTask(60f, () -> System.exit(2));
    }
}
