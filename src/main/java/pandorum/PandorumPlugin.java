package pandorum;

import arc.files.Fi;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Interval;
import arc.util.Log;
import arc.util.Timekeeper;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.changestream.FullDocument;
import com.mongodb.client.model.changestream.OperationType;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import io.socket.client.IO;
import io.socket.client.Socket;
import mindustry.game.Team;
import mindustry.gen.Player;
import mindustry.mod.Plugin;
import okhttp3.OkHttpClient;
import org.bson.BsonDocument;
import org.bson.Document;
import org.json.JSONArray;
import pandorum.commands.client.*;
import pandorum.commands.server.*;
import pandorum.comp.*;
import pandorum.database.ArrowSubscriber;
import pandorum.entry.HistoryEntry;
import pandorum.models.PlayerInfo;
import pandorum.struct.CacheSeq;
import pandorum.vote.VoteKickSession;
import pandorum.vote.VoteSession;

import java.io.IOException;
import java.net.URISyntaxException;

import static mindustry.Vars.dataDirectory;

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
    public static Seq<Document> playersInfo = new Seq<>();
    public static PlayerInfo playerInfoSchema;

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
        playerInfoSchema = new PlayerInfo(playersInfoCollection);

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

        playersInfoCollection.find().subscribe(new ArrowSubscriber<>(
                next -> {
                    Document player = playerInfoSchema.tryApplySchema(next);

                    if (next == null) return;
                    if (player == null) {
                        playersInfoCollection
                                .deleteOne(new BasicDBObject("id", next.getObjectId("_id")))
                                .subscribe(new ArrowSubscriber<>());
                        return;
                    }
                    playersInfo.add(player);
                }
        ));

        playersInfoCollection.watch().fullDocument(FullDocument.UPDATE_LOOKUP).subscribe(new ArrowSubscriber<>(
                changeStreamDocument -> {
                    OperationType operation = changeStreamDocument.getOperationType();
                    BsonDocument changedDocumentKey = changeStreamDocument.getDocumentKey();
                    Document changedDocument = changeStreamDocument.getFullDocument();

                    if (changedDocumentKey == null) return;

                    int playerInfoIndex = playersInfo.indexOf(document -> document
                            .toBsonDocument()
                            .getObjectId("_id")
                            .equals(changedDocumentKey.getObjectId("_id"))
                    );

                    try {
                        switch (operation) {
                            case DELETE -> playersInfo.remove(document -> document
                                .toBsonDocument()
                                .getObjectId("_id")
                                .equals(changedDocumentKey.getObjectId("_id"))
                            );
                            case INSERT -> {
                                assert changedDocument != null;
                                Document SCHPlayerInfoDocument = playerInfoSchema.applySchema(changedDocument);
                                playersInfo.add(SCHPlayerInfoDocument);
                            }
                            case UPDATE, REPLACE -> {
                                assert changedDocument != null;
                                Document SCHPlayerInfoDocument = playerInfoSchema.applySchema(changedDocument);
                                playersInfo.set(playerInfoIndex, SCHPlayerInfoDocument);
                            }
                        }
                    } catch (Exception e) {
                        Log.err(e);
                    }
                }
        ));
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

    /**
     * @param uuid - Uuid конкретного игрока, которого надо сохранить в базу данных
     */
    public static void savePlayerStats(String uuid) {
        Document player = playersInfo.find((docPlayer) -> docPlayer.getString("uuid").equals(uuid));
        BasicDBObject filter = new BasicDBObject("uuid", uuid);

        if (player == null) {
            playersInfoCollection.deleteOne(filter).subscribe(new ArrowSubscriber<>());
            return;
        }

        try {
            Document SCHPlayerInfoDocument = playerInfoSchema.applySchema(player);
            SCHPlayerInfoDocument.remove("_id");
            SCHPlayerInfoDocument.remove("__v");
            playersInfoCollection.replaceOne(
                    Filters.eq("_id", player.getObjectId("_id")),
                    SCHPlayerInfoDocument
            ).subscribe(new ArrowSubscriber<>());
        } catch(Exception e) {
            Log.err(e);
        }
    }

    public static Document createInfo(Player player) {
        Document playerInfo = playersInfo.find((info) -> info.getString("uuid").equals(player.uuid()));
        if (playerInfo == null) {
            playerInfo = playerInfoSchema.create(player.uuid(), true, true, "off", 0, 0, 0, 0, 0);
            playersInfo.add(playerInfo);
            savePlayerStats(player.uuid());
        }
        return playerInfo;
    }
}
