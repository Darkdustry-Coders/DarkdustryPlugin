package pandorum;

import static mindustry.Vars.dataDirectory;

import java.io.IOException;
import java.net.URISyntaxException;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;

import org.bson.Document;
import org.json.JSONArray;

import arc.files.Fi;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Interval;
import arc.util.Log;
import arc.util.Timekeeper;
import io.socket.client.IO;
import io.socket.client.Socket;
import mindustry.game.Team;
import mindustry.mod.Plugin;
import okhttp3.OkHttpClient;
import pandorum.commands.client.ARTVCommand;
import pandorum.commands.client.AdminChatCommand;
import pandorum.commands.client.AlertCommand;
import pandorum.commands.client.CoreCommand;
import pandorum.commands.client.FillCommand;
import pandorum.commands.client.GiveCommand;
import pandorum.commands.client.HelpCommand;
import pandorum.commands.client.HistoryCommand;
import pandorum.commands.client.HubCommand;
import pandorum.commands.client.InfoCommand;
import pandorum.commands.client.LoginCommand;
import pandorum.commands.client.MapCommand;
import pandorum.commands.client.MapListCommand;
import pandorum.commands.client.NominateCommand;
import pandorum.commands.client.PlayerListCommand;
import pandorum.commands.client.RTVCommand;
import pandorum.commands.client.RainbowCommand;
import pandorum.commands.client.RankCommand;
import pandorum.commands.client.SavesListCommand;
import pandorum.commands.client.SpawnCommand;
import pandorum.commands.client.SpectateCommand;
import pandorum.commands.client.SurrenderCommand;
import pandorum.commands.client.SyncCommand;
import pandorum.commands.client.TeamChatCommand;
import pandorum.commands.client.TeamCommand;
import pandorum.commands.client.TranslatorCommand;
import pandorum.commands.client.UnbanCommand;
import pandorum.commands.client.UnitsCommand;
import pandorum.commands.client.UnitsDespawnCommand;
import pandorum.commands.client.VNWCommand;
import pandorum.commands.client.VoteCommand;
import pandorum.commands.client.VoteKickCommand;
import pandorum.commands.client.VotingCommand;
import pandorum.commands.server.ClearAdminsCommand;
import pandorum.commands.server.ClearBansCommand;
import pandorum.commands.server.DespawnCommand;
import pandorum.commands.server.ReloadCommand;
import pandorum.commands.server.RestartCommand;
import pandorum.commands.server.SayCommand;
import pandorum.comp.Config;
import pandorum.comp.Config.PluginType;
import pandorum.comp.IpInfo;
import pandorum.comp.Loader;
import pandorum.comp.RainbowPlayerEntry;
import pandorum.comp.Translator;
import pandorum.entry.HistoryEntry;
import pandorum.models.PlayerModel;
import pandorum.struct.CacheSeq;
import pandorum.vote.VoteKickSession;
import pandorum.vote.VoteSession;

public final class PandorumPlugin extends Plugin {

    public static final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
            .setPrettyPrinting()
            .serializeNulls()
            .disableHtmlEscaping()
            .create();

    public static VoteSession[] current = {null};
    public static VoteKickSession[] currentlyKicking = {null};
    public static Config config;
    public static Seq<IpInfo> forbiddenIps;

    public static final ObjectMap<String, Timekeeper> nominateCooldowns = new ObjectMap<>();
    public static final ObjectMap<String, Timekeeper> votekickCooldowns = new ObjectMap<>();
    public static final ObjectMap<String, Long> loginCooldowns = new ObjectMap<>();

    public static final ObjectMap<Team, Seq<String>> surrendered = new ObjectMap<>();
    public static final Seq<String> votesRTV = new Seq<>();
    public static final Seq<String> votesVNW = new Seq<>();
    public static final Seq<String> activeHistoryPlayers = new Seq<>();
    public static final Interval interval = new Interval(2);

    public static CacheSeq<HistoryEntry>[][] history;
    public static final Seq<RainbowPlayerEntry> rainbow = new Seq<>();

    public static MongoClient mongoClient;
    public static MongoCollection<Document> playersInfoCollection;

    public static final ObjectMap<String, String> codeLanguages = new ObjectMap<>();
    public static final OkHttpClient client = new OkHttpClient();

    public static Socket socket;
    public static final Seq<String> waiting = new Seq<>();

    public PandorumPlugin() throws IOException, URISyntaxException {
        socket = IO.socket("ws://127.0.0.1:9189");

        ConnectionString connString = new ConnectionString("mongodb://darkdustry:XCore2000@127.0.0.1:27017/?authSource=darkdustry");

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .retryWrites(true)
                .build();
        mongoClient = MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase("darkdustry");
        playersInfoCollection = database.getCollection("playersinfo");

        PlayerModel.setSourceCollection(playersInfoCollection);

        Fi file = dataDirectory.child("config.json");
        if (!file.exists()) {
            file.writeString(gson.toJson(config = new Config()));
            Log.info("Файл config.json успешно сгенерирован!");
        } else {
            config = gson.fromJson(file.reader(), Config.class);
        }

        JSONArray languages = Translator.getAllLanguages();
        for (int i = 0; i < languages.length(); i++) {
            String codeAlpha = languages.getJSONObject(i).getString("code_alpha_1");
            String fullCode = languages.getJSONObject(i).getString("full_code");
            codeLanguages.put(codeAlpha, fullCode);
        }
    }

    @Override
    public void init() {
        Loader.init();
    }
   
    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.removeCommand("say");

        handler.register("reload-config", "Перезапустить файл конфигов.", ReloadCommand::run);
        handler.register("despw", "Убить всех юнитов на карте.", DespawnCommand::run);
        handler.register("clear-bans", "Разбанить всех.", ClearBansCommand::run);
        handler.register("clear-admins", "Снять все админки.", ClearAdminsCommand::run);
        handler.register("rr", "Перезапустить сервер.", RestartCommand::run);
        handler.register("say", "<сообщение...>", "Сказать в чат от имени сервера.", SayCommand::run);
    }

    @Override
    public void registerClientCommands(CommandHandler handler){
        handler.removeCommand("a");
        handler.removeCommand("t");
        handler.removeCommand("help");
        handler.removeCommand("votekick");
        handler.removeCommand("vote");
        handler.removeCommand("js");
        handler.removeCommand("sync");

        handler.register("help", "[page]", "Список всех команд.", HelpCommand::run);
        handler.register("a", "<message...>", "Отправить сообщение админам.", AdminChatCommand::run);
        handler.register("t", "<message...>", "Отправить сообщение игрокам твоей команды.", TeamChatCommand::run);
        handler.register("pl", "[page]", "Вывести список игроков и их ID.", PlayerListCommand::run);
        handler.register("despw", "Убить юнитов на карте.", UnitsDespawnCommand::run);
        handler.register("hub", "Выйти в Хаб.", HubCommand::run);
        handler.register("units", "<list/change/name> [unit]", "Действия с юнитами.", UnitsCommand::run);
        handler.register("unban", "<ip/uuid...>", "Разбанить игрока.", UnbanCommand::run);
        handler.register("ban", "<ip/uuid...>", "Забанить игрока.", BanCommand::run);
        handler.register("votekick", "<player...>", "Проголосовать за кик игрока.", VoteKickCommand::run);
        handler.register("vote", "<y/n>", "Решить судьбу игрока.", VoteCommand::run);
        handler.register("sync", "Синхронизация с сервером.", SyncCommand::run);
        handler.register("tr", "<off/auto/current/locale>", "Переключение переводчика чата.", TranslatorCommand::run);
        handler.register("info", "[player...]", "Посмотреть информацию о себе.", InfoCommand::run);
        handler.register("login", "Зайти на сервер как администратор.", LoginCommand::run);
        handler.register("rank", "Информация о рангах.", RankCommand::run);

        // Все команды ниже не используются в PluginType.other
        if (PandorumPlugin.config.mode != Config.Gamemode.hexed && PandorumPlugin.config.mode != Config.Gamemode.hub && PandorumPlugin.config.mode != Config.Gamemode.castle) {
            handler.register("history", "Переключение отображения истории при нажатии на тайл.", HistoryCommand::run);
            handler.register("rtv", "Проголосовать за смену карты.", RTVCommand::run);
            handler.register("vnw", "Проголосовать за пропуск волны.", VNWCommand::run);
            handler.register("artv", "Принудительно завершить игру.", ARTVCommand::run);
            handler.register("core", "<small/medium/big>", "Заспавнить ядро.", CoreCommand::run);
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
            handler.register("rainbow", "Радуга!", RainbowCommand::run);
        }

        // Команды ниже используются в PluginType.pvp
        if (config.mode == Config.Gamemode.pvp || config.mode == Config.Gamemode.siege) {
            handler.register("surrender", "Сдаться.", SurrenderCommand::run);
        }

        // Команды ниже используются в PluginType.sand
        if (config.mode == Config.Gamemode.sandbox) {
            handler.register("fill", "<width> <height> <floor> [overlay/ore/wall]", "Заполнить область данным типом блока.", FillCommand::run);
        }
    }
}
