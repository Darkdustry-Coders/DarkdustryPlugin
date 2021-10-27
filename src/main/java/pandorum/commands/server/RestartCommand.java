package pandorum.commands.server;

import arc.util.Log;
import arc.util.Time;
import mindustry.gen.Groups;
import net.dv8tion.jda.api.EmbedBuilder;
import pandorum.Misc;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;

public class RestartCommand implements ServerCommand {
    public static void run(final String[] args) {
        Log.info("Перезапуск сервера...");

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(BotMain.errorColor)
                .setTitle("Сервер выключился для перезапуска!");

        BotHandler.botChannel.sendMessageEmbeds(embed.build()).queue();

        Groups.player.each(Misc::connectToHub);
        Time.runTask(60f, () -> System.exit(2));
    }
}
