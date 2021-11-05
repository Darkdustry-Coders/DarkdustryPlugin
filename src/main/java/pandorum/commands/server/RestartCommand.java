package pandorum.commands.server;

import arc.util.Log;
import arc.util.Time;
import mindustry.net.Packets.KickReason;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;

import static mindustry.Vars.netServer;

public class RestartCommand implements ServerCommand {
    public static void run(final String[] args) {
        Log.info("Перезапуск сервера...");

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(BotMain.errorColor)
                .setTitle("Сервер выключился для перезапуска!");

        BotHandler.botChannel.sendMessage(embed).join();

        netServer.kickAll(KickReason.serverRestarting);
        Time.runTask(10f, () -> System.exit(2));
    }
}
