package pandorum.discord;

import arc.Core;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Strings;
import arc.util.io.Streams;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateFields;
import discord4j.core.spec.MessageCreateSpec;
import mindustry.Vars;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.maps.Map;
import pandorum.Misc;
import pandorum.PandorumPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static pandorum.discord.BotMain.*;

public class BotHandler {
    public static final String prefix = PandorumPlugin.config.prefix;
    public static final CommandHandler handler = new CommandHandler(prefix);
    public static MessageChannel botChannel, adminChannel;

    public static final ObjectMap<Message, String> waiting = new ObjectMap<>();

    public BotHandler() {
        botChannel = (MessageChannel) client.getChannelById(Snowflake.of(PandorumPlugin.config.DiscordChannelID)).block();
        adminChannel = (MessageChannel) client.getChannelById(Snowflake.of(844215222784753664L)).block();

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

            info(msg, "Команды", builder.toString());
        });

        handler.<Message>register("addmap", "Добавить карту на сервер.", (args, msg) -> {
            if (checkAdmin(msg.getAuthorAsMember().block())) {
                err(msg, "Эта команда недоступна для тебя.", "У тебя нет прав на ее использование.");
                return;
            }

            if (msg.getAttachments().size() != 1 || !msg.getAttachments().get(0).getFilename().endsWith(".msav")) {
                err(msg, "Ошибка", "Пожалуйста, прикрепи файл карты к сообщению.");
                return;
            }

            Attachment a = msg.getAttachments().get(0);

            try {
                File mapFile = new File("config/maps/" + a.getFilename());
                Streams.copy(download(a.getUrl()), new FileOutputStream(mapFile));
                Vars.maps.reload();
                text(msg, "*Карта добавлена на сервер.*");
            } catch (Exception e) {
                err(msg, "Ошибка добавления карты.", "Произошла непредвиденная ошибка.");
            }
        });

        handler.<Message>register("map", "<название...>", "Получить файл карты с сервера.", (args, msg) -> {
            if (checkAdmin(msg.getAuthorAsMember().block())) {
                err(msg, "Эта команда недоступна для тебя.", "У тебя нет прав на ее использование.");
                return;
            }

            Vars.maps.reload();
            Map map = Misc.findMap(args[0]);
            if (map == null) {
                err(msg, "Карта не найдена.", "Проверьте правильность ввода.");
                return;
            }

            try {
                msg.getChannel().block()
                        .createMessage(MessageCreateSpec.builder()
                        .addFile(MessageCreateFields.File.of(map.file.name(), new FileInputStream(map.file.file())))
                        .build()).block();
            } catch (Exception e) {
                err(msg, "Возникла ошибка.", "Ошибка получения карты с сервера.");
            }
        });

        handler.<Message>register("removemap", "<название...>", "Удалить карту с сервера.", (args, msg) -> {
            if (checkAdmin(msg.getAuthorAsMember().block())) {
                err(msg, "Эта команда недоступна для тебя.", "У тебя нет прав на ее использование.");
                return;
            }

            Vars.maps.reload();
            Map map = Misc.findMap(args[0]);
            if (map == null) {
                err(msg, "Карта не найдена.", "Проверьте правильность ввода.");
                return;
            }

            try {
                Vars.maps.removeMap(map);
                Vars.maps.reload();
                text(msg, "*Карта удалена с сервера.*");
            } catch (Exception e) {
                err(msg, "Возникла ошибка.", "Ошибка удаления карты с сервера.");
            }
        });

        handler.<Message>register("maps", "[страница]", "Список всех карт сервера.", (args, msg) -> {
            if (args.length > 0 && !Strings.canParseInt(args[0])) {
                err(msg, "Страница должна быть числом.", "Аргументы не верны.");
                return;
            }

            Vars.maps.reload();
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

            EmbedCreateSpec embed = EmbedCreateSpec.builder()
                    .color(BotMain.normalColor)
                    .author("Server maps", null, "https://cdn.iconscout.com/icon/free/png-256/map-and-location-2569358-2148268.png")
                    .title("Список карт сервера (страница " + (page + 1) + " из " + pages + ")")
                    .addField("Карты:", maps.toString(), false)
                    .build();

            sendEmbed(msg.getChannel().block(), embed);
        });

        handler.<Message>register("status","Узнать статус сервера.", (args, msg) -> {
            if (Vars.state.isMenu()) {
                err(msg, "Сервер отключен.", "Попросите администратора запустить его.");
                return;
            }

            EmbedCreateSpec embed = EmbedCreateSpec.builder()
                    .color(BotMain.successColor)
                    .author("Статус сервера", null, "https://icon-library.com/images/yes-icon/yes-icon-15.jpg")
                    .title("Сервер онлайн.")
                    .addField("Игроков:", Integer.toString(Groups.player.size()), false)
                    .addField("Карта:", Vars.state.map.name(), false)
                    .addField("Волна:", Integer.toString(Vars.state.wave), false)
                    .addField("Потребление ОЗУ:", Core.app.getJavaHeap() / 1024 / 1024 + " MB", false)
                    .footer("Используй " + prefix + "players, чтобы посмотреть список всех игроков.", null)
                    .build();

            sendEmbed(msg.getChannel().block(), embed);
        });

        handler.<Message>register("players","Посмотреть список игроков на сервере.", (args, msg) -> {
            if (Vars.state.isMenu()) {
                err(msg, "Сервер отключен.", "Попросите администратора запустить его.");
                return;
            }

            if (Groups.player.size() == 0) {
                err(msg, "На сервере нет игроков.", "Список игроков пуст.");
                return;
            }

            StringBuilder players = new StringBuilder();
            int i = 1;
            for (Player player : Groups.player) {
                players.append(i).append(". ").append(Strings.stripColors(player.name)).append("\n");
                i++;
            }

            EmbedCreateSpec embed = EmbedCreateSpec.builder()
                    .color(BotMain.normalColor)
                    .author("Server players", null, "https://cdn4.iconfinder.com/data/icons/symbols-vol-1-1/40/user-person-single-id-account-player-male-female-512.png")
                    .title("Список игроков на сервере (всего " + Groups.player.size() + ")")
                    .addField("Игроки:", players.toString(), false)
                    .build();

            sendEmbed(msg.getChannel().block(), embed);
        });
    }

    public static void text(Message message, String text, Object... args) {
        text(message.getChannel().block(), text, args);
    }

    public static void text(MessageChannel channel, String text, Object... args) {
        channel.createMessage(Strings.format(text, args)).block();
    }

    public static void info(Message message, String title, String text, Object... args) {
        sendEmbed(message.getChannel().block(), EmbedCreateSpec.builder().color(normalColor).addField(title, Strings.format(text, args), true).build());
    }

    public static void err(Message message, String title, String text, Object... args) {
        sendEmbed(message.getChannel().block(), EmbedCreateSpec.builder().color(errorColor).addField(title, Strings.format(text, args), true).build());
    }

    public static InputStream download(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
            return connection.getInputStream();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean checkAdmin(Member member) {
        if (member.isBot()) return true;
        return member.getRoles().toStream().noneMatch(role -> role.getId().equals(Snowflake.of(907554436149309460L)) || role.getId().equals(Snowflake.of(810760273689444385L)));
    }

    public static void sendEmbed(EmbedCreateSpec embed) {
        botChannel.createMessage(embed).block();
    }

    public static void sendEmbed(MessageChannel channel, EmbedCreateSpec embed) {
        channel.createMessage(embed).block();
    }
}
