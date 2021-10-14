package pandorum;

import arc.Events;
import arc.files.Fi;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.*;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
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
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.EventType.*;
import mindustry.game.Team;
import mindustry.game.Teams.TeamData;
import mindustry.gen.*;
import mindustry.maps.Map;
import mindustry.mod.Plugin;
import mindustry.net.Administration;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import okhttp3.OkHttpClient;
import org.bson.BsonDocument;
import org.bson.Document;
import org.json.JSONArray;
import pandorum.comp.*;
import pandorum.comp.Config.PluginType;
import pandorum.database.ArrowSubscriber;
import pandorum.entry.HistoryEntry;
import pandorum.models.PlayerInfo;
import pandorum.struct.CacheSeq;
import pandorum.vote.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static mindustry.Vars.*;
import static pandorum.Misc.*;

public final class PandorumPlugin extends Plugin {

    public final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
            .setPrettyPrinting()
            .serializeNulls()
            .disableHtmlEscaping()
            .create();

    public static VoteSession[] current = {null};
    public static VoteKickSession[] currentlyKicking = {null};
    public static Config config;
    public static Seq<IpInfo> forbiddenIps;

    public static final ObjectMap<Team, Seq<String>> surrendered = new ObjectMap<>();
    public static final Seq<String> votesRTV = new Seq<>();
    public static final Seq<String> votesVNW = new Seq<>();
    public static final Seq<String> activeHistoryPlayers = new Seq<>();
    public static final Interval interval = new Interval();

    public static CacheSeq<HistoryEntry>[][] history;
    public static final Seq<RainbowPlayerEntry> rainbow = new Seq<>();

    public static MongoClient mongoClient;
    public static MongoCollection<Document> playersInfoCollection;
    public static Seq<Document> playersInfo = new Seq<>();
    public static PlayerInfo playerInfoSchema;

    public static final ObjectMap<String, String> codeLanguages = new ObjectMap<>();
    public static final OkHttpClient client = new OkHttpClient();

    public static Socket socket;
    public static final ObjectMap<String, Long> loginCooldowns = new ObjectMap<>();
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

        handler.register("reload-config", "Перезапустить файл конфигов.", args -> {
            config = gson.fromJson(dataDirectory.child("config.json").readString(), Config.class);
            Log.info("Перезагружено.");
        });

        handler.register("despw", "Убить всех юнитов на карте.", args -> {
            int amount = Groups.unit.size();
            Groups.unit.each(Unitc::kill);
            Log.info("Ты убил @ юнитов!", amount);
            WebhookEmbedBuilder despwEmbedBuilder = new WebhookEmbedBuilder()
                .setColor(0xFF0000)
                .setTitle(new WebhookEmbed.EmbedTitle("Все юниты убиты!", null));
            DiscordWebhookManager.client.send(despwEmbedBuilder.build());
        });

        handler.register("clear-bans", "Разбанить всех.", args -> {
            netServer.admins.getBanned().each(unban -> netServer.admins.unbanPlayerID(unban.id));
            netServer.admins.getBannedIPs().each(ip -> netServer.admins.unbanPlayerIP(ip));
            Log.info("Все игроки разбанены!");
        });

        handler.register("clear-admins", "Снять все админки.", arg -> {
            netServer.admins.getAdmins().each(admin -> netServer.admins.unAdminPlayer(admin.id));
            Groups.player.each(player -> player.admin(false));
            Log.info("Админов больше нет!");
        });

        handler.register("rr", "Перезапустить сервер.", args -> {
            Log.info("Перезапуск сервера...");
            WebhookEmbedBuilder restartEmbedBuilder = new WebhookEmbedBuilder()
                .setColor(0xFF0000)
                .setTitle(new WebhookEmbed.EmbedTitle("Сервер выключился для перезапуска!", null));
            DiscordWebhookManager.client.send(restartEmbedBuilder.build());

            Groups.player.each(Misc::connectToHub);
            Timer.schedule(() -> System.exit(2), 10f);
        });

        handler.register("say", "<сообщение...>", "Сказать в чат от имени сервера.", args -> {
            sendToChat("commands.say.chat", args[0]);
            Log.info("Server: &ly@", args[0]);
            DiscordWebhookManager.client.send(String.format("**[Сервер]:** %s", args[0].replaceAll("https?://|@", "")));
        });
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

        handler.<Player>register("help", "[page]", "Список всех команд.", (args, player) -> {
            if (args.length > 0 && !Strings.canParseInt(args[0])) {
                bundled(player, "commands.page-not-int");
                return;
            }
            int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
            int pages = Mathf.ceil((float)netServer.clientCommands.getCommandList().size / 6.0f);

            if (--page >= pages || page < 0) {
                bundled(player, "commands.under-page", String.valueOf(pages));
                return;
            }

            StringBuilder result = new StringBuilder();
            result.append(Bundle.format("commands.help.page", findLocale(player.locale), page + 1, pages)).append("\n");

            for (int i = 6 * page; i < Math.min(6 * (page + 1), netServer.clientCommands.getCommandList().size); i++) {
                CommandHandler.Command command = netServer.clientCommands.getCommandList().get(i);
                String desc = Bundle.has(Strings.format("commands.@.description", command.text), findLocale(player.locale)) ? Bundle.format(Strings.format("commands.@.description", command.text), findLocale(player.locale)) : command.description;
                result.append("[orange] /").append(command.text).append("[white] ").append(command.paramText).append("[lightgray] - ").append(desc).append("\n");
            }
            player.sendMessage(result.toString());
        });

        handler.<Player>register("a", "<message...>", "Отправить сообщение админам.", (args, player) -> {
            if (Misc.adminCheck(player)) return;
            Groups.player.each(Player::admin, admin -> bundled(admin, "commands.admin.a.chat", Misc.colorizedName(player), args[0]));
        });

        handler.<Player>register("t", "<message...>", "Отправить сообщение игрокам твоей команды.", (args, player) -> {
            String teamColor = "[#" + player.team().color + "]";
            Groups.player.each(p -> p.team() == player.team(), teammate -> bundled(teammate, "commands.t.chat", teamColor, Misc.colorizedName(player), args[0]));
        });

        handler.<Player>register("pl", "[page]", "Вывести список игроков и их ID.", (args, player) -> {
            if (args.length > 0 && !Strings.canParseInt(args[0])) {
                bundled(player, "commands.page-not-int");
                return;
            }
            int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
            int pages = Mathf.ceil((float)Groups.player.size() / 6.0f);

            if (--page >= pages || page < 0) {
                bundled(player, "commands.under-page", pages);
                return;
            }

            StringBuilder result = new StringBuilder();
            result.append(Bundle.format("commands.pl.page", findLocale(player.locale), page + 1, pages)).append("\n");

            for (int i = 6 * page; i < Math.min(6 * (page + 1), Groups.player.size()); i++) {
                Player p = Groups.player.index(i);
                result.append("[#9c88ee]* [white]").append(p.admin ? Iconc.admin  + " " : "").append(Misc.colorizedName(p)).append(" [accent]/ [cyan]ID: ").append(p.id()).append(Bundle.format("commands.pl.locale", findLocale(player.locale), p.locale)).append("\n");
            }
            player.sendMessage(result.toString());
        });

        handler.<Player>register("despw", "Убить всех юнитов на карте.", (args, player) -> {
            if (Misc.adminCheck(player)) return;
            String[][] options = {{Bundle.format("events.menu.yes", findLocale(player.locale)), Bundle.format("events.menu.no", findLocale(player.locale))}, {Bundle.format("commands.admin.despw.menu.players", findLocale(player.locale))}, {Bundle.format("commands.admin.despw.menu.sharded", findLocale(player.locale))}, {Bundle.format("commands.admin.despw.menu.crux", findLocale(player.locale))}};
            Call.menu(player.con, 2, Bundle.format("commands.admin.despw.menu.header", findLocale(player.locale)), Bundle.format("commands.admin.despw.menu.content", findLocale(player.locale), Groups.unit.size()), options);
        });

        handler.<Player>register("rainbow", "РАДУГА!", (args, player) -> {
            RainbowPlayerEntry old = rainbow.find(r -> r.player.uuid().equals(player.uuid()));
            if(old != null) {
                rainbow.remove(old);
                player.name = netServer.admins.getInfo(player.uuid()).lastName;
                bundled(player, "commands.rainbow.off");
                return;
            }
            bundled(player, "commands.rainbow.on");
            RainbowPlayerEntry entry = new RainbowPlayerEntry();
            entry.player = player;
            entry.stripedName = Strings.stripColors(player.name);
            rainbow.add(entry);
        });

        handler.<Player>register("hub", "Выйти в Хаб.", (args, player) -> Misc.connectToHub(player));

        handler.<Player>register("units", "<list/change/name> [unit]", "Действия с юнитами.", (args, player) -> {
            switch (args[0]) {
                case "name" -> {
                    if (!player.dead()) bundled(player, "commands.unit-name", player.unit().type().name);
                    else bundled(player, "commands.unit-name.null");
                }
                case "list" -> {
                    StringBuilder units = new StringBuilder();
                    content.units().each(unit -> {
                        if (!unit.name.equals("block")) units.append(" ").append(Icons.icons.get(unit.name)).append(unit.name);
                    });
                    bundled(player, "commands.units.list", units.toString());
                }
                case "change" -> {
                    if (Misc.adminCheck(player)) return;
                    if (args.length == 1 || args[1].equals("block")) {
                        bundled(player, "commands.units.incorrect");
                        return;
                    }
                    UnitType found = content.units().find(u -> u.name.equalsIgnoreCase(args[1]));
                    if (found == null) {
                        bundled(player, "commands.unit-not-found");
                        return;
                    }
                    Unit spawn = found.spawn(player.team(), player.x(), player.y());
                    spawn.spawnedByCore(true);
                    player.unit(spawn);
                    bundled(player, "commands.units.change.success");
                }
                default -> bundled(player, "commands.units.incorrect");
            }
        });

        handler.<Player>register("unban", "<ip/uuid...>", "Разбанить игрока.", (args, player) -> {
            if (Misc.adminCheck(player)) return;
            if (netServer.admins.unbanPlayerIP(args[0]) || netServer.admins.unbanPlayerID(args[0])) {
                bundled(player, "commands.admin.unban.success", netServer.admins.getInfo(args[0]).lastName);
            } else {
                bundled(player, "commands.admin.unban.not-banned");
            }
        });

        handler.<Player>register("votekick", "<name...>", "Проголосовать за кик игрока.", (args, player) -> {
            if (!Administration.Config.enableVotekick.bool()) {
                bundled(player, "commands.votekick.disabled");
                return;
            }

            if (Groups.player.size() < 3) {
                bundled(player, "commands.not-enough-players");
                return;
            }

            if (currentlyKicking[0] != null) {
                bundled(player, "commands.vote-already-started");
                return;
            }

            Player found = Misc.findByName(args[0]);
            if (found == null) {
                bundled(player, "commands.player-not-found");
                return;
            }

            if (found.admin) {
                bundled(player, "commands.votekick.cannot-kick-admin");
                return;
            }

            if (found.team() != player.team()) {
                bundled(player, "commands.votekick.cannot-kick-another-team");
                return;
            }

            if (found == player) {
                bundled(player, "commands.votekick.cannot-vote-for-yourself");
                return;
            }

            VoteKickSession session = new VoteKickSession(currentlyKicking, found);
            session.vote(player, 1);
            currentlyKicking[0] = session;
        });

        handler.<Player>register("vote", "<y/n>", "Решить судьбу игрока.", (args, player) -> {
            if (currentlyKicking[0] == null) {
                bundled(player, "commands.no-voting");
                return;
            }

            if (currentlyKicking[0].voted().contains(player.uuid())) {
                bundled(player, "commands.already-voted");
                return;
            }

            if (currentlyKicking[0].target() == player) {
                bundled(player, "commands.vote.cannot-vote-for-yourself");
                return;
            }

            if (currentlyKicking[0].target().team() != player.team()) {
                bundled(player, "commands.vote.cannot-vote-another-team");
                return;
            }

            int sign = switch(args[0].toLowerCase()) {
                case "y", "yes", "да" ->  1;
                case "n", "no", "нет" -> -1;
                default -> 0;
            };

            if (sign == 0) {
                bundled(player, "commands.vote.incorrect-sign");
                return;
            }

            currentlyKicking[0].vote(player, sign);
        });

        handler.<Player>register("sync", "Синхронизация с сервером.", (args, player) -> {
            if (Time.timeSinceMillis(player.getInfo().lastSyncTime) < 1000 * 15) {
                bundled(player, "commands.sync.time");
                return;
            }

            player.getInfo().lastSyncTime = Time.millis();
            Call.worldDataBegin(player.con);
            netServer.sendWorldData(player);
        });

        handler.<Player>register("tr", "<off/auto/current/locale>", "Переключение переводчика чата.", (args, player) -> {
            Document playerInfo = createInfo(player);
            switch (args[0].toLowerCase()) {
                case "current" -> {
                    String locale = playerInfo.getString("locale");
                    bundled(player, "commands.tr.current", locale == null ? "off" : locale);
                }
                case "list" -> {
                    StringBuilder builder = new StringBuilder();
                    codeLanguages.keys().forEach(locale -> builder.append(" ").append(locale));
                    bundled(player, "commands.tr.list", builder.toString());
                }
                case "off" -> {
                    playerInfo.replace("locale", "off");
                    bundled(player, "commands.tr.disabled");
                }
                case "auto" -> {
                    playerInfo.replace("locale", "auto");
                    bundled(player, "commands.tr.auto");
                }
                default -> {
                    if (!codeLanguages.containsKey(args[0])) {
                        bundled(player, "commands.tr.incorrect");
                        break;
                    }

                    playerInfo.replace("locale", args[0]);
                    bundled(player, "commands.tr.changed", args[0]);
                }
            }

            savePlayerStats(player.uuid());
        });

        handler.<Player>register("status", "Посмотреть информацию о себе.", (args, player) -> {
            Document playerInfo = createInfo(player);
            savePlayerStats(player.uuid());
            bundled(player, "commands.status.info", TimeUnit.MILLISECONDS.toMinutes(playerInfo.getLong("playtime")), playerInfo.getLong("buildings"));
        });

        handler.<Player>register("login", "Зайти на сервер как администратор.", (args, player) -> {
            if (waiting.contains(player.uuid())) {
                bundled(player, "commands.login.waiting");
                return;
            }

            if (player.admin()) {
                bundled(player, "commands.login.already");
                return;
            }

            if (loginCooldowns.containsKey(player.uuid())) {
                if (Time.timeSinceMillis(loginCooldowns.get(player.uuid())) < 1000 * 60 * 15L) return;
                loginCooldowns.remove(player.uuid());
            }

            waiting.add(player.uuid());
            socket.emit("registerAsAdmin", player.uuid(), player.name());
            bundled(player, "commands.login.sent");
        });

        // Все команды ниже не используются в PluginType.other
        if (config.type != PluginType.other) {
            handler.<Player>register("history", "Переключение отображения истории при нажатии на тайл.", (args, player) -> {
                if (activeHistoryPlayers.contains(player.uuid())) {
                    activeHistoryPlayers.remove(player.uuid());
                    bundled(player, "commands.history.off");
                    return;
                }
                activeHistoryPlayers.add(player.uuid());
                bundled(player, "commands.history.on");
            });

            handler.<Player>register("rtv", "Проголосовать за смену карты.", (args, player) -> {
                if (votesRTV.contains(player.uuid())) {
                    bundled(player, "commands.already-voted");
                    return;
                }

                votesRTV.add(player.uuid());
                int cur = votesRTV.size;
                int req = (int)Math.ceil(config.voteRatio * Groups.player.size());
                sendToChat("commands.rtv.ok", Misc.colorizedName(player), cur, req);

                if (cur < req) {
                    return;
                }

                sendToChat("commands.rtv.successful");
                votesRTV.clear();
                Events.fire(new GameOverEvent(Team.crux));
            });

            handler.<Player>register("vnw", "Проголосовать за пропуск волны.", (args, player) -> {
                if (votesVNW.contains(player.uuid())) {
                    bundled(player, "commands.already-voted");
                    return;
                }

                votesVNW.add(player.uuid());
                int cur = votesVNW.size;
                int req = (int)Math.ceil(config.voteRatio * Groups.player.size());
                sendToChat("commands.vnw.ok", Misc.colorizedName(player), cur, req);

                if (cur < req) {
                    return;
                }

                sendToChat("commands.vnw.successful");
                votesVNW.clear();
                state.wavetime = 0f;
            });

            handler.<Player>register("artv", "Принудительно завершить игру.", (args, player) -> {
                if (Misc.adminCheck(player)) return;
                String[][] options = {{Bundle.format("events.menu.yes", findLocale(player.locale)), Bundle.format("events.menu.no", findLocale(player.locale))}};
                Call.menu(player.con, 3, Bundle.format("commands.admin.artv.menu.header", findLocale(player.locale)), Bundle.format("commands.admin.artv.menu.content", findLocale(player.locale)), options);
            });

            handler.<Player>register("core", "<small/medium/big>", "Заспавнить ядро.", (args, player) -> {
                if (Misc.adminCheck(player)) return;

                Block core = switch(args[0].toLowerCase()){
                    case "medium" -> Blocks.coreFoundation;
                    case "big" -> Blocks.coreNucleus;
                    case "small" -> Blocks.coreShard;
                    default -> null;
                };

                if (core == null) {
                    bundled(player, "commands.admin.core.core-not-found");
                    return;
                }
                Call.constructFinish(player.tileOn(), core, player.unit(), (byte)0, player.team(), false);
                bundled(player, player.tileOn().block() == core ? "commands.admin.core.success" : "commands.admin.core.failed");
            });

            handler.<Player>register("give", "<item> [count]", "Выдать ресурсы в ядро.", (args, player) -> {
                if (Misc.adminCheck(player)) return;

                if (args.length > 1 && !Strings.canParseInt(args[1])) {
                    bundled(player, "commands.non-int");
                    return;
                }

                int count = args.length > 1 ? Strings.parseInt(args[1]) : 1000;

                Item item = content.items().find(b -> b.name.equalsIgnoreCase(args[0]));
                if (item == null) {
                    bundled(player, "commands.admin.give.item-not-found");
                    return;
                }

                TeamData team = state.teams.get(player.team());
                if (!team.hasCore()) {
                    bundled(player, "commands.admin.give.core-not-found");
                    return;
                }

                team.core().items.add(item, count);
                bundled(player, "commands.admin.give.success");
            });

            handler.<Player>register("alert", "Включить или отключить предупреждения о постройке реакторов вблизи к ядру.", (args, player) -> {
                Document playerInfo = createInfo(player);
                if (playerInfo.getBoolean("alerts")) {
                    playerInfo.replace("alerts", false);
                    bundled(player, "commands.alert.off");
                    savePlayerStats(player.uuid());
                    return;
                }

                playerInfo.replace("alerts", true);
                bundled(player, "commands.alert.on");
                savePlayerStats(player.uuid());
            });

            handler.<Player>register("team", "<team> [name...]", "Смена команды для админов.", (args, player) -> {
                if (Misc.adminCheck(player)) return;

                Team team = Structs.find(Team.all, t -> t.name.equalsIgnoreCase(args[0]));
                if (team == null) {
                    bundled(player, "commands.teams");
                    return;
                }

                Player target = args.length > 1 ? Misc.findByName(args[1]) : player;
                if (target == null) {
                    bundled(player, "commands.player-not-found");
                    return;
                }

                bundled(target, "commands.admin.team.success", Misc.colorizedTeam(team));
                target.team(team);

                String text = args.length > 1 ? "Команда игрока " + Strings.stripColors(target.name()) + " изменена на " + team + "." : "Команда изменена на " + team + ".";
                WebhookEmbedBuilder teamEmbedBuilder = new WebhookEmbedBuilder()
                        .setColor(0xFF0000)
                        .setTitle(new WebhookEmbed.EmbedTitle(text, null))
                        .addField(new WebhookEmbed.EmbedField(true, "Администратором", Strings.stripColors(player.name)));
                DiscordWebhookManager.client.send(teamEmbedBuilder.build());
            });

            handler.<Player>register("spectate", "Режим наблюдателя.", (args, player) -> {
                if (Misc.adminCheck(player)) return;
                player.clearUnit();
                bundled(player, player.team() == Team.derelict ? "commands.admin.spectate.disabled" : "commands.admin.spectate.enabled");
                player.team(player.team() == Team.derelict ? state.rules.defaultTeam : Team.derelict);
            });

            handler.<Player>register("map", "Информация о карте.", (args, player) -> bundled(player, "commands.mapname", state.map.name(), state.map.author()));

            handler.<Player>register("maps", "[page]", "Вывести список карт.", (args, player) -> {
                if (args.length > 0 && !Strings.canParseInt(args[0])) {
                    bundled(player, "commands.page-not-int");
                    return;
                }

                Seq<Map> mapList = Vars.maps.all();
                int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
                int pages = Mathf.ceil(mapList.size / 6.0f);

                if (--page >= pages || page < 0) {
                    bundled(player, "commands.under-page", pages);
                    return;
                }

                StringBuilder result = new StringBuilder();
                result.append(Bundle.format("commands.maps.page", findLocale(player.locale), page + 1, pages)).append("\n");
                for (int i = 6 * page; i < Math.min(6 * (page + 1), mapList.size); i++) {
                    result.append("[lightgray] ").append(i + 1).append("[orange] ").append(mapList.get(i).name()).append("[white] ").append("\n");
                }

                player.sendMessage(result.toString());
            });

            handler.<Player>register("saves", "[page]", "Вывести список сохранений на сервере.", (args, player) -> {
                if (args.length > 0 && !Strings.canParseInt(args[0])) {
                    bundled(player, "commands.page-not-int");
                    return;
                }

                Seq<Fi> saves = Seq.with(Vars.saveDirectory.list()).filter(f -> Objects.equals(f.extension(), Vars.saveExtension));
                int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
                int pages = Mathf.ceil(saves.size / 6.0f);

                if (--page >= pages || page < 0) {
                    bundled(player, "commands.under-page", pages);
                    return;
                }

                StringBuilder result = new StringBuilder();
                result.append(Bundle.format("commands.saves.page", findLocale(player.locale), page + 1, pages)).append("\n");
                for (int i = 6 * page; i < Math.min(6 * (page + 1), saves.size); i++) {
                    result.append("[lightgray] ").append(i + 1).append("[orange] ").append(saves.get(i).nameWithoutExtension()).append("[white] ").append("\n");
                }

                player.sendMessage(result.toString());
            });

            handler.<Player>register("nominate", "<map/save/load> <name...>", "Начать голосование за смену карты/загрузку карты.", (args, player) -> {
                if (Groups.player.size() < 3) {
                    bundled(player, "commands.not-enough-players");
                    return;
                }

                if (current[0] != null) {
                    bundled(player, "commands.vote-already-started");
                    return;
                }

                switch (args[0].toLowerCase()) {
                    case "map" -> {
                        Map map = Misc.findMap(args[1]);
                        if (map == null) {
                            bundled(player, "commands.nominate.map.not-found");
                            return;
                        }
                        VoteSession session = new VoteMapSession(current, map);
                        current[0] = session;
                        session.vote(player, 1);
                    }
                    case "save" -> {
                        VoteSession session = new VoteSaveSession(current, args[1]);
                        current[0] = session;
                        session.vote(player, 1);
                    }
                    case "load" -> {
                        Fi save = Misc.findSave(args[1]);
                        if (save == null) {
                            bundled(player, "commands.nominate.load.not-found");
                            return;
                        }
                        VoteSession session = new VoteLoadSession(current, save);
                        current[0] = session;
                        session.vote(player, 1);
                    }
                    default -> bundled(player, "commands.nominate.incorrect-mode");
                }
            });

            handler.<Player>register("voting", "<y/n>", "Проголосовать.", (args, player) -> {
                if (current[0] == null) {
                    bundled(player, "commands.no-voting");
                    return;
                }

                if (current[0].voted().contains(player.uuid())) {
                    bundled(player, "commands.already-voted");
                    return;
                }

                int sign = switch(args[0].toLowerCase()) {
                    case "y", "yes", "да" ->  1;
                    case "n", "no", "нет" -> -1;
                    default -> 0;
                };

                if (sign == 0) {
                    bundled(player, "commands.voting.incorrect-sign");
                    return;
                }

                current[0].vote(player, sign);
            });

            handler.<Player>register("spawn", "<unit> [count] [team]", "Заспавнить юнитов.", (args, player) -> {
                if (Misc.adminCheck(player)) return;

                if (args.length > 1 && !Strings.canParseInt(args[1])){
                    bundled(player, "commands.non-int");
                    return;
                }

                int count = args.length > 1 ? Strings.parseInt(args[1]) : 1;
                if (count > 25 || count < 1) {
                    bundled(player, "commands.admin.spawn.limit");
                    return;
                }

                Team team = args.length > 2 ? Structs.find(Team.all, t -> t.name.equalsIgnoreCase(args[2])) : player.team();
                if (team == null) {
                    bundled(player, "commands.teams");
                    return;
                }

                UnitType unit = content.units().find(b -> b.name.equals(args[0]));
                if (unit == null || args[0].equals("block")) {
                    bundled(player, "commands.unit-not-found");
                    return;
                }

                for (int i = 0; count > i; i++) unit.spawn(team, player.x, player.y);
                bundled(player, "commands.admin.spawn.success", count, unit.name, Misc.colorizedTeam(team));

                WebhookEmbedBuilder spawnEmbedBuilder = new WebhookEmbedBuilder()
                        .setColor(0xFF0000)
                        .setTitle(new WebhookEmbed.EmbedTitle("Юниты заспавнены для команды " + team + ".", null))
                        .addField(new WebhookEmbed.EmbedField(true, "Администратором", Strings.stripColors(player.name)))
                        .addField(new WebhookEmbed.EmbedField(true, "Название", unit.name))
                        .addField(new WebhookEmbed.EmbedField(true, "Количетво", Integer.toString(count)));
                DiscordWebhookManager.client.send(spawnEmbedBuilder.build());
            });
        }

        // Команды ниже используются в PluginType.pvp
        if (config.type == PluginType.pvp) {
            handler.<Player>register("surrender", "Сдаться.", (args, player) -> {
                Team team = player.team();
                Seq<String> teamVotes = surrendered.get(team, Seq::new);
                if (teamVotes.contains(player.uuid())) {
                    bundled(player, "commands.already-voted");
                    return;
                }

                teamVotes.add(player.uuid());
                int cur = teamVotes.size;
                int req = (int)Math.ceil(config.voteRatio * Groups.player.count(p -> p.team() == team));
                sendToChat("commands.surrender.ok", Misc.colorizedTeam(team), Misc.colorizedName(player), cur, req);

                if (cur < req) {
                    return;
                }

                surrendered.remove(team);
                sendToChat("commands.surrender.successful", Misc.colorizedTeam(team));
                Groups.unit.each(u -> u.team == team, u -> Time.run(Mathf.random(360), u::kill));
                for (Tile tile : world.tiles) {
                    if (tile.build != null && tile.team() == team) {
                        Time.run(Mathf.random(360), tile.build::kill);
                    }
                }
            });
        }

        if (config.type == PluginType.sand) {
            handler.<Player>register("fill", "<width> <height> <floor>", "Заполнить область данным типом блока.", (args, player) -> {
                if (adminCheck(player)) return;

                if (!Strings.canParsePositiveInt(args[0]) || !Strings.canParsePositiveInt(args[1]) || Strings.parseInt(args[0]) > 50 || Strings.parseInt(args[1]) > 50) {
                    bundled(player, "commands.admin.fill.incorrect-number-format");
                    return;
                }

                int w = Mathf.clamp(Strings.parseInt(args[0]), 0, 50) + player.tileX();
                int h = Mathf.clamp(Strings.parseInt(args[1]), 0, 50) + player.tileY();

                Floor floor = (Floor)content.blocks().find(b -> b.isFloor() && b.name.equalsIgnoreCase(args[2]));
                if (floor == null) {
                    bundled(player, "commands.admin.fill.incorrect-floor");
                    return;
                }

                for (int x = player.tileX(); x < w; x++) {
                    for (int y = player.tileY(); y < h; y++) {
                        if (world.tile(x, y) != null) world.tile(x, y).setFloorNet(floor);
                    }
                }
                bundled(player, "commands.admin.fill.success", floor);
            });
        }
    }

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
            playerInfo = playerInfoSchema.create(player.uuid(), true, true, "off", 0, 0);
            playersInfo.add(playerInfo);
            savePlayerStats(player.uuid());
        }
        return playerInfo;
    }
}
