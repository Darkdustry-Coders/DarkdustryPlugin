package pandorum.discord;

import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Strings;
import arc.util.io.Streams;
import mindustry.Vars;
import mindustry.maps.Map;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import pandorum.PandorumPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static pandorum.discord.BotMain.*;

public class BotHandler {
    private static final String prefix = PandorumPlugin.config.prefix;
    public static final CommandHandler handler = new CommandHandler(prefix);

    public static Guild guild;

    public static TextChannel botChannel;

    public BotHandler() {
        guild = jda.getGuildById(810758118442663936L);
        botChannel = guild.getTextChannelById(PandorumPlugin.config.DiscordChannelID);

        register();
    }

    private static void register() {
        handler.<Message>register("help", "Список команд.", (args, msg) -> {
            StringBuilder builder = new StringBuilder();
            for (CommandHandler.Command command : handler.getCommandList()) {
                builder.append(prefix);
                builder.append("**");
                builder.append(command.text);
                builder.append("**");
                if (command.params.length > 0) {
                    builder.append(" *");
                    builder.append(command.paramText);
                    builder.append("*");
                }
                builder.append(" - ");
                builder.append(command.description);
                builder.append("\n");
            }

            info(msg.getChannel(), "Команды", builder.toString());
        });

        handler.<Message>register("addmap", "Добавить карту на сервер.", (args, msg) -> {
            if (msg.getAttachments().size() != 1 || !msg.getAttachments().get(0).getFileName().endsWith(".msav")) {
                errDelete(msg, "Ошибка", "Пожалуйста, прикрепи файл карты к сообщению.");
                return;
            }

            Message.Attachment a = msg.getAttachments().get(0);

            try {
                new File("../maps/").mkdir();
                File mapFile = new File("../maps/" + a.getFileName());
                Streams.copy(download(a.getUrl()), new FileOutputStream(mapFile));

                Vars.maps.reload();

                text(msg, "*Карта добавлена на сервер.*");
            } catch (Exception e) {
                errDelete(msg, "Ошибка добавления карты.", "Произошла непредвиденная ошибка.");
            }
        });

        handler.<Message>register("maps", "Список всех карт сервера.", (args, msg) -> {
            Seq<Map> mapList = Vars.maps.customMaps();

            StringBuilder maps = new StringBuilder();
            for (int i = 0; i < mapList.size; i++) {
                maps.append(i).append(". ").append(mapList.get(i).name()).append("\n");
            }

            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(BotMain.errorColor)
                    .setAuthor("Карты сервера")
                    .setTitle("Список карт сервера")
                    .addField("Найдено " + mapList.size + "карт:", maps.toString(), false);

            msg.getChannel().sendMessageEmbeds(embed.build()).queue();
        });
    }

    public static void info(MessageChannel channel, String title, String text, Object... args) {
        channel.sendMessageEmbeds(new EmbedBuilder().addField(title, Strings.format(text, args), true).setColor(normalColor).build()).queue();
    }

    public static void text(Message message, String text, Object... args) {
        text(message.getChannel(), text, args);
    }

    public static void text(MessageChannel channel, String text, Object... args) {
        channel.sendMessage(Strings.format(text, args)).queue();
    }

    public static void text(String text, Object... args) {
        text(botChannel, text, args);
    }

    public static void errDelete(Message message, String title, String text, Object... args) {
        message.getChannel().sendMessageEmbeds(new EmbedBuilder().addField(title, Strings.format(text, args), true).setColor(errorColor).build()).queue(result -> result.delete().queueAfter(messageDeleteTime, TimeUnit.MILLISECONDS));

        message.delete().queueAfter(messageDeleteTime, TimeUnit.MILLISECONDS);
    }

    public static InputStream download(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
            return connection.getInputStream();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
