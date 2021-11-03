package pandorum.discord;

import arc.Core;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Strings;
import arc.util.io.Streams;
import mindustry.Vars;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.maps.Map;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import pandorum.Misc;
import pandorum.PandorumPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static pandorum.discord.BotMain.errorColor;
import static pandorum.discord.BotMain.normalColor;

public class BotHandler {
    public static final String prefix = PandorumPlugin.config.prefix;
    public static final CommandHandler handler = new CommandHandler(prefix);
    public static ServerTextChannel botChannel, adminChannel;
    public static Server server;

    public static final ObjectMap<Message, String> waiting = new ObjectMap<>();

    public BotHandler() {
        botChannel = BotMain.bot.getServerTextChannelById(PandorumPlugin.config.DiscordChannelID).get();
        adminChannel = BotMain.bot.getServerTextChannelById(844215222784753664L).get();

        server = BotMain.bot.getServerById(810758118442663936L).get();

        register();
    }

    private static void register() {
        handler.<Message>register("help", "Список команд.", (args, msg) -> {
            StringBuilder builder = new StringBuilder();
            for (CommandHandler.Command command : handler.getCommandList()) {
                builder.append(prefix).append("**").append(command.text).append("**");
                if (command.params.length > 0) {
                    builder.append(" *").append(command.paramText).append("*");
                }
                builder.append(" - ").append(command.description).append("\n");
            }

            info(msg.getServerTextChannel().get(), "Команды", builder.toString());
        });

        handler.<Message>register("addmap", "Добавить карту на сервер.", (args, msg) -> {
            if (checkAdmin(msg.getUserAuthor().get())) {
                err(msg, "Эта команда только для админов.", "У тебя нет прав на ее использование.");
                return;
            }

            if (msg.getAttachments().size() != 1 || !msg.getAttachments().get(0).getFileName().endsWith(".msav")) {
                err(msg, "Ошибка", "Пожалуйста, прикрепи файл карты к сообщению.");
                return;
            }

            MessageAttachment a = msg.getAttachments().get(0);

            try {
                File mapFile = new File("config/maps/" + a.getFileName());
                Streams.copy(download(a.getUrl().toString()), new FileOutputStream(mapFile));

                Vars.maps.reload();

                text(msg, "*Карта добавлена на сервер.*");
            } catch (Exception e) {
                err(msg, "Ошибка добавления карты.", "Произошла непредвиденная ошибка.");
            }
        });

        handler.<Message>register("map", "<name...>", "Получить файл карты с сервера.", (args, msg) -> {
            if (checkAdmin(msg.getUserAuthor().get())) {
                err(msg, "Эта команда только для админов.", "У тебя нет прав на ее использование.");
                return;
            }

            Map map = Misc.findMap(args[0]);
            if (map == null) {
                err(msg, "Карта не найдена.", "Проверьте правильность ввода.");
                return;
            }

            new MessageBuilder().addFile(map.file.file()).send(msg.getChannel()).join();
        });

        handler.<Message>register("maps", "[страница]", "Список всех карт сервера.", (args, msg) -> {
            if (args.length > 0 && !Strings.canParseInt(args[0])) {
                err(msg, "Страница должна быть числом.", "Аргументы не верны.");
                return;
            }

            Seq<Map> mapList = Vars.maps.customMaps();
            if (mapList.size == 0) {
                err(msg, "На сервере нет карт.", "Список карт пуст.");
                return;
            }

            int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
            int pages = Mathf.ceil(mapList.size / 20f);

            if (--page >= pages || page < 0) {
                err(msg, "Указана неверная страница списка карт.", "Страница должна быть числом от 1 до " + pages);
                return;
            }

            StringBuilder maps = new StringBuilder();
            for (int i = 20 * page; i < Math.min(20 * (page + 1), mapList.size); i++) {
                maps.append(i + 1).append(". ").append(Strings.stripColors(mapList.get(i).name())).append("\n");
            }

            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(BotMain.normalColor)
                    .setAuthor("Server maps")
                    .setTitle("Список карт сервера (страница " + (page + 1) + " из " + pages + ")")
                    .addField("Карты:", maps.toString(), false);

            msg.getChannel().sendMessage(embed).join();
        });

        handler.<Message>register("status","Узнать статус сервера.", (args, msg) -> {
            if (Vars.state.isMenu()) {
                err(msg, "Сервер отключен.", "Попросите администратора запустить его.");
                return;
            }

            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(BotMain.successColor)
                    .setAuthor("Статус сервера")
                    .setTitle("Сервер онлайн.")
                    .addField("Игроков:", Integer.toString(Groups.player.size()), false)
                    .addField("Карта:", Vars.state.map.name(), false)
                    .addField("Волна:", Integer.toString(Vars.state.wave), false)
                    .addField("Потребление ОЗУ:", Core.app.getJavaHeap() / 1024 / 1024 + " MB", false)
                    .setFooter("Используй **" + prefix + "players**, чтобы получить список всех игроков.");

            msg.getChannel().sendMessage(embed).join();
        });

        handler.<Message>register("players","Посмотреть список игроков на сервере.", (args, msg) -> {
            if (Vars.state.isMenu() || Groups.player.size() == 0) {
                err(msg, "На сервере нет игроков.", "Список игроков пуст.");
                return;
            }

            StringBuilder players = new StringBuilder();
            int i = 1;
            for (Player player : Groups.player) {
                players.append(i).append(". ").append(Strings.stripColors(player.name)).append("\n");
                i++;
            }

            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(BotMain.normalColor)
                    .setAuthor("Server players")
                    .setTitle("Список игроков на сервере (всего " + Groups.player.size() + ")")
                    .addField("Игроки:", players.toString(), false);

            msg.getChannel().sendMessage(embed).join();
        });
    }

    public static void info(ServerTextChannel channel, String title, String text, Object... args) {
        channel.sendMessage(new EmbedBuilder().addField(title, Strings.format(text, args), true).setColor(normalColor)).join();
    }

    public static void text(Message message, String text, Object... args) {
        text(message.getServerTextChannel().get(), text, args);
    }

    public static void text(ServerTextChannel channel, String text, Object... args) {
        channel.sendMessage(Strings.format(text, args)).join();
    }

    public static void text(String text, Object... args) {
        text(botChannel, text, args);
    }

    public static void err(Message message, String title, String text, Object... args) {
        message.getChannel().sendMessage(new EmbedBuilder().addField(title, Strings.format(text, args), true).setColor(errorColor)).join();
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
        if (user.isBot()) return true;
        return user.getRoles(server).stream().noneMatch(role -> role.getId() == 810760273689444385L);
    }
}
