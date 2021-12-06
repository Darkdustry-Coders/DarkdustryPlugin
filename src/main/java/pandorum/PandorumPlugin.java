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
        handler.removeCommand("exit");
        handler.removeCommand("say");
        handler.removeCommand("kick");
        handler.removeCommand("pardon");

        handler.register("reload-config", "Reload the configuration file.", ConsoleReloadCommand::run);
        handler.register("despw", "Kill all units.", ConsoleDespawnCommand::run);
        handler.register("rr", "Restart the server.", ConsoleRestartCommand::run);
        handler.register("exit", "Shut down the server.", ConsoleExitCommand::run);
        handler.register("say", "<message...>", "Send a message as a server..", ConsoleSayCommand::run);
        handler.register("kick", "<player...>", "Kick a player from the server.", ConsoleKickCommand::run);
        handler.register("pardon", "<uuid...>", "Pardon a kicked player.", ConsolePardonCommand::run);
        handler.register("ban", "<ip/name/id> <ip/username/uuid...>", "Ban a player by ip, name or uuid.", ConsoleBanCommand::run);
        handler.register("unban", "<ip/all/uuid>", "Unban a player by ip or uuid.", ConsoleUnbanCommand::run);
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        handler.removeCommand("a");
        handler.removeCommand("t");
        handler.removeCommand("help");
        handler.removeCommand("votekick");
        handler.removeCommand("vote");
        handler.removeCommand("sync");

        handler.register("help", "[page]", "List of all commands.", HelpCommand::run);
        handler.register("discord", "v", DiscordLinkCommand::run);
        handler.register("a", "<message...>", "Send message to admins.", AdminChatCommand::run);
        handler.register("t", "<message...>", "Send message to teammates", TeamChatCommand::run);
        handler.register("votekick", "<player...>", "Start a voting to kick a player.", VoteKickCommand::run);
        handler.register("vote", "<y/n>", "Vote to kick a player.", VoteCommand::run);
        handler.register("sync", "Re-synchronize world state.", SyncCommand::run);
        handler.register("tr", "<off/auto/current/locale>", "Manage chat translator.", TranslatorCommand::run);
        handler.register("info", "[player...]", "See some info about a player.", InfoCommand::run);
        handler.register("rank", "See information about your rank.", RankCommand::run);
        handler.register("players", "[page]", "List of all players.", PlayerListCommand::run);

        handler.register("ban", "<uuid...>", "Ban a player.", BanCommand::run);
        handler.register("unban", "<uuid...>", "Unban a player.", UnbanCommand::run);
        handler.register("despw", "Kill units.", DespawnCommand::run);
        handler.register("unit", "<unit> [player...]", "Change a unit.", UnitCommand::run);
        handler.register("login", "Send confirmation to get admin.", LoginCommand::run);

        if (config.mode.isSimple) {
            handler.register("rtv", "Vote to skip the map.", RTVCommand::run);

            if (!config.mode.isPvP) handler.register("vnw", "Vote to skip a wave.", VNWCommand::run);
            else handler.register("surrender", "Vote to surrender.", SurrenderCommand::run);

            handler.register("history", "Enable tile inspector.", HistoryCommand::run);
            handler.register("alert", "Enable/disable alerts.", AlertCommand::run);
            handler.register("map", "Information about current map.",  MapCommand::run);
            handler.register("maps", "[page]", "List of all maps.", MapsListCommand::run);
            handler.register("saves", "[page]", "List of all saves.", SavesListCommand::run);
            handler.register("nominate", "<map/save/load> <name...>", "Vote for load a save/map.", NominateCommand::run);
            handler.register("voting", "<y/n>", "Vote «yes» or «no».", VotingCommand::run);

            handler.register("spawn", "<unit> [count] [team]", "Spawn units.", SpawnCommand::run);
            handler.register("artv", "Force a gameover.", ARTVCommand::run);
            handler.register("core", "[small/medium/big]", "Spawn a core.", CoreCommand::run);
            handler.register("give", "<item> [count]", "Add items to the core.", GiveCommand::run);
            handler.register("team", "<team> [player...]", "Change team.", TeamCommand::run);
            handler.register("spectate", "Spectator mode.", SpectateCommand::run);

            if (config.mode == Config.Gamemode.sandbox) {
                handler.register("fill", "<width> <height> <block_1> [block_2]", "Fill an area with some floor.", FillCommand::run);
            }
        }

        if (config.mode != Config.Gamemode.hub) {
            handler.register("hub", "Connect to HUB.", HubCommand::run);
        }
    }
}
