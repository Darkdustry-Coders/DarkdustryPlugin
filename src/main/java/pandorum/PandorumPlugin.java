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
import mindustry.gen.Player;
import mindustry.mod.Plugin;
import org.bson.Document;
import pandorum.annotations.commands.ClientCommand;
import pandorum.annotations.commands.OverrideCommand;
import pandorum.annotations.commands.ServerCommand;
import pandorum.annotations.commands.admin.RequireAdmin;
import pandorum.annotations.commands.admin.RequireNotAdmin;
import pandorum.comp.AntiVPN;
import pandorum.comp.Config;
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
        //Loader.init();
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        Log.info("Server commands default length " + handler.getCommandList().size);

        Reflection.getServerCommands("pandorum.commands.server").each(method -> {
            ServerCommand commandAnnotation = method.getAnnotation(ServerCommand.class);

            if (method.isAnnotationPresent(OverrideCommand.class))
                if (handler.getCommandList().contains(command -> command.text.equals(commandAnnotation.name())))
                    handler.removeCommand(commandAnnotation.name());

            handler.register(
                    commandAnnotation.name(),
                    commandAnnotation.args(),
                    commandAnnotation.description(),
                    (String[] args) -> {
                        try {
                            method.invoke(null, (Object) args);
                        } catch (Exception e) { Log.err(e.getMessage()); }
                    }
            );
        });

        Log.info("Server commands " + handler.getCommandList().size);
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        Log.info("Client commands default length " + handler.getCommandList().size);

        Reflection.getClientCommands("pandorum.commands.client", config.mode).each(method -> {
            ClientCommand commandAnnotation = method.getAnnotation(ClientCommand.class);

            if (method.isAnnotationPresent(OverrideCommand.class))
                if (handler.getCommandList().contains(command -> command.text.equals(commandAnnotation.name())))
                    handler.removeCommand(commandAnnotation.name());
            boolean admin = method.isAnnotationPresent(RequireAdmin.class);
            boolean notAdmin = method.isAnnotationPresent(RequireNotAdmin.class);
            handler.register(
                    commandAnnotation.name(),
                    commandAnnotation.args(),
                    commandAnnotation.description(),
                    (String[] args, Player player) -> {
                        if (admin && !player.admin) return;
                        if (notAdmin && player.admin) return;
                        try {
                            method.invoke(null, args, player);
                        } catch (Exception e) { Log.err(e.getMessage()); }
                    }
            );
        });

        Log.info("Client commands " + handler.getCommandList().size);
    }
}
