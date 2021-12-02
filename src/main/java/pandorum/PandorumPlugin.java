package pandorum;

import arc.files.Fi;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Interval;
import arc.util.Log;
import arc.util.Timekeeper;
import arc.util.io.ReusableByteOutStream;
import arc.util.io.Writes;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.mod.Plugin;
import org.bson.Document;
import pandorum.commands.client.*;
import pandorum.commands.server.*;
import pandorum.comp.AntiVPN;
import pandorum.comp.Config;
import pandorum.comp.Loader;
import pandorum.comp.Translator;
import pandorum.entry.HistoryEntry;
import pandorum.models.PlayerModel;
import pandorum.struct.CacheSeq;
import pandorum.vote.VoteKickSession;
import pandorum.vote.VoteSession;

import java.io.IOException;

import static mindustry.Vars.dataDirectory;

public final class PandorumPlugin extends Plugin {

    public static String discordServerLink = "https://dsc.gg/darkdustry";

    public static final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
            .setPrettyPrinting()
            .serializeNulls()
            .disableHtmlEscaping()
            .create();

    public static final VoteSession[] current = {null};
    public static final VoteKickSession[] currentlyKicking = {null};
    public static Config config;

    public static final ObjectMap<String, Timekeeper> nominateCooldowns = new ObjectMap<>(), votekickCooldowns = new ObjectMap<>(), loginCooldowns = new ObjectMap<>();

    public static final ObjectMap<Team, Seq<String>> surrendered = new ObjectMap<>();
    public static final Seq<String> votesRTV = new Seq<>(), votesVNW = new Seq<>(), activeHistoryPlayers = new Seq<>();

    public static final Interval interval = new Interval(3);
    public static CacheSeq<HistoryEntry>[][] history;

    public static MongoClient mongoClient;
    public static MongoCollection<Document> playersInfoCollection;

    public static ReusableByteOutStream writeBuffer;
    public static Writes outputBuffer;

    public static Translator translator;
    public static AntiVPN antiVPN;

    public PandorumPlugin() throws IOException {
        Fi file = dataDirectory.child("config.json");
        if (file.exists()) {
            config = gson.fromJson(file.reader(), Config.class);
        } else {
            file.writeString(gson.toJson(config = new Config()));
            Log.info("Файл конфигурации сгенерирован... (@)", file.absolutePath());
        }

        ConnectionString connString = new ConnectionString("mongodb://darkdustry:XCore2000@127.0.0.1:27017/?authSource=darkdustry");

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .retryWrites(true)
                .build();

        mongoClient = MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase("darkdustry");
        playersInfoCollection = database.getCollection("playersinfo");

        PlayerModel.setSourceCollection(playersInfoCollection);

        antiVPN = new AntiVPN("w7j425-826177-597253-3134u9");
        translator = new Translator();
    }

    @Override
    public void init() {
        Loader.init();
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.removeCommand("say");
        handler.removeCommand("pardon");
        handler.removeCommand("exit");

        handler.register("reload-config", "Перезапустить файл конфигурации.", ReloadCommand::run);
        handler.register("despw", "Убить всех юнитов на карте.", DespawnCommand::run);
        handler.register("clear-bans", "Разбанить всех.", ClearBansCommand::run);
        handler.register("clear-admins", "Снять все админки.", ClearAdminsCommand::run);
        handler.register("rr", "Перезапустить сервер.", RestartCommand::run);
        handler.register("exit", "Выключить сервер.", ExitCommand::run);
        handler.register("say", "<сообщение...>", "Сказать в чат от имени сервера.", SayCommand::run);
        handler.register("pardon", "<uuid...>", "Снять кик с игрока.", PardonCommand::run);
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        handler.removeCommand("a");
        handler.removeCommand("t");
        handler.removeCommand("help");
        handler.removeCommand("votekick");
        handler.removeCommand("vote");
        handler.removeCommand("sync");

        handler.register("help", "[page]", "Список всех команд.", HelpCommand::run);
        handler.register("discord", "Ссылка на наш Discord сервер.", DiscordLinkCommand::run);
        handler.register("a", "<message...>", "Отправить сообщение админам.", AdminChatCommand::run);
        handler.register("t", "<message...>", "Отправить сообщение игрокам твоей команды.", TeamChatCommand::run);
        handler.register("players", "[page]", "Вывести список игроков и их ID.", PlayerListCommand::run);
        handler.register("despw", "Убить юнитов на карте.", UnitsDespawnCommand::run);
        handler.register("units", "<list/change/name> [unit] [player...]", "Действия с юнитами.", UnitsCommand::run);
        handler.register("unban", "<uuid...>", "Разбанить игрока.", UnbanCommand::run);
        handler.register("ban", "<uuid...>", "Забанить игрока.", BanCommand::run);
        handler.register("votekick", "<player...>", "Проголосовать за кик игрока.", VoteKickCommand::run);
        handler.register("vote", "<y/n>", "Решить судьбу игрока.", VoteCommand::run);
        handler.register("sync", "Синхронизация с сервером.", SyncCommand::run);
        handler.register("tr", "<off/auto/current/locale>", "Переключение переводчика чата.", TranslatorCommand::run);
        handler.register("info", "[player...]", "Посмотреть информацию о себе.", InfoCommand::run);
        handler.register("login", "Зайти на сервер как администратор.", LoginCommand::run);
        handler.register("rank", "Информация о рангах.", RankCommand::run);

        if (config.mode.isSimple) {
            handler.register("history", "Переключение отображения истории при нажатии на тайл.", HistoryCommand::run);
            handler.register("rtv", "Проголосовать за смену карты.", RTVCommand::run);
            handler.register("artv", "Принудительно завершить игру.", ARTVCommand::run);
            handler.register("core", "[small/medium/big]", "Заспавнить ядро.", CoreCommand::run);
            handler.register("give", "<item> [count]", "Выдать ресурсы в ядро.", GiveCommand::run);
            handler.register("alert", "Включить или отключить предупреждения о постройке реакторов вблизи к ядру.", AlertCommand::run);
            handler.register("team", "<team> [player...]", "Смена команды для админов.", TeamCommand::run);
            handler.register("spectate", "Режим наблюдателя.", SpectateCommand::run);
            handler.register("map", "Информация о карте.",  MapCommand::run);
            handler.register("maps", "[page]", "Вывести список карт.", MapListCommand::run);
            handler.register("saves", "[page]", "Вывести список сохранений на сервере.", SavesListCommand::run);
            handler.register("nominate", "<map/save/load> <name...>", "Начать голосование за смену карты/загрузку карты.", NominateCommand::run);
            handler.register("voting", "<y/n>", "Проголосовать.", VotingCommand::run);
            handler.register("spawn", "<unit> [count] [team]", "Заспавнить юнитов.", SpawnCommand::run);

            if (!config.mode.isPvP) {
                handler.register("vnw", "Проголосовать за пропуск волны.", VNWCommand::run);
            }

            if (config.mode.isPvP) {
                handler.register("surrender", "Сдаться.", SurrenderCommand::run);
            }

            if (config.mode == Config.Gamemode.sandbox) {
                handler.register("fill", "<width> <height> <block_1> [block_2]", "Заполнить область данным типом блока.", FillCommand::run);
            }
        }

        if (config.mode != Config.Gamemode.hub) {
            handler.register("hub", "Выйти в Хаб.", HubCommand::run);
        }
    }
}
