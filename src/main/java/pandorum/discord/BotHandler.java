package pandorum.discord;

import arc.Core;
import arc.files.Fi;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Strings;
import arc.util.io.Streams;
import mindustry.Vars;
import mindustry.gen.Groups;
import mindustry.maps.Map;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import pandorum.Misc;
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
        botChannel = guild != null ? guild.getTextChannelById(PandorumPlugin.config.DiscordChannelID) : null;

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
            if (checkAdmin(msg.getAuthor())) {
                errDelete(msg, "Эта команда только для админов.", "У тебя нет прав на ее использование.");
                return;
            }

            if (msg.getAttachments().size() != 1 || !msg.getAttachments().get(0).getFileName().endsWith(".msav")) {
                errDelete(msg, "Ошибка", "Пожалуйста, прикрепи файл карты к сообщению.");
                return;
            }

            Message.Attachment a = msg.getAttachments().get(0);

            try {
                new File("config/maps/").mkdir();
                File mapFile = new File("../maps/" + a.getFileName());
                Streams.copy(download(a.getUrl()), new FileOutputStream(mapFile));

                Vars.maps.reload();

                text(msg, "*Карта добавлена на сервер.*");
            } catch (Exception e) {
                errDelete(msg, "Ошибка добавления карты.", "Произошла непредвиденная ошибка.");
            }
        });

        handler.<Message>register("map", "<name...>", "Получить файл карты с сервера.", (args, msg) -> {
            if (checkAdmin(msg.getAuthor())) {
                errDelete(msg, "Эта команда только для админов.", "У тебя нет прав на ее использование.");
                return;
            }

            Map map = Misc.findMap(args[0]);
            if (map == null) {
                errDelete(msg, "Карта не найдена.", "Проверьте правильность ввода.");
                return;
            }

            Fi mapFile = map.file;
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(BotMain.successColor)
                    .setAuthor("Карта с сервера")
                    .setTitle("Карта успешно получена!");

            msg.getChannel().sendMessageEmbeds(embed.build()).addFile(mapFile.file()).queue();
        });

        handler.<Message>register("maps", "[страница]", "Список всех карт сервера.", (args, msg) -> {
            if (checkAdmin(msg.getAuthor())) {
                errDelete(msg, "Эта команда только для админов.", "У тебя нет прав на ее использование.");
                return;
            }

            if (args.length > 0 && !Strings.canParseInt(args[0])) {
                errDelete(msg, "Страница должна быть числом.", "Аргументы не верны.");
                return;
            }

            Seq<Map> mapList = Vars.maps.customMaps();
            if (mapList.size == 0) {
                errDelete(msg, "На сервере нет карт.", "Список карт пуст.");
                return;
            }

            int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
            int pages = Mathf.ceil(mapList.size / 20f);

            if (--page >= pages || page < 0) {
                errDelete(msg, "Указана неверная страница списка карт.", "Страница должна быть числом от 1 до " + pages);
                return;
            }

            StringBuilder maps = new StringBuilder();
            for (int i = 6 * page; i < Math.min(6 * (page + 1), mapList.size); i++) {
                maps.append(i + 1).append(". ").append(Strings.stripColors(mapList.get(i).name())).append("\n");
            }

            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(BotMain.normalColor)
                    .setAuthor("Карты сервера")
                    .setTitle("Список карт сервера (страница " + page + " из " + pages + ")")
                    .addField("Карты:", maps.toString(), false);

            msg.getChannel().sendMessageEmbeds(embed.build()).queue();
        });

        handler.<Message>register("status","Узнать статус сервера.", (args, msg) -> {
            if (Vars.state.isMenu()) {
                errDelete(msg, "Сервер отключен.", "Попросите администратора запустить его.");
                return;
            }

            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(BotMain.successColor)
                    .setAuthor("Статус сервера")
                    .setTitle("Сервер онлайн.")
                    .addField("Игроков:", Integer.toString(Groups.player.size()), false)
                    .addField("Карта:", Vars.state.map.name(), false)
                    .addField("Волна:", Integer.toString(Vars.state.wave), false)
                    .addField("Нагрузка на сервер:", Long.toString(Core.app.getJavaHeap() / 1024 / 1024), false);

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

    public static boolean checkAdmin(User user) {
        if (user.isBot() || user.isSystem()) return true;
        Member member = guild.retrieveMember(user).complete();
        return member.getRoles().stream().noneMatch(role -> role.getIdLong() == 810760273689444385L);
    }
}
