package pandorum;

import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.ArcRuntimeException;
import arc.util.CommandHandler;
import arc.util.Interval;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Structs;
import arc.util.Timer;
import arc.util.Time;
import arc.util.io.Streams;
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
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.EventType.BlockBuildEndEvent;
import mindustry.game.EventType.BuildSelectEvent;
import mindustry.game.EventType.ConfigEvent;
import mindustry.game.EventType.DepositEvent;
import mindustry.game.EventType.GameOverEvent;
import mindustry.game.EventType.PlayerBanEvent;
import mindustry.game.EventType.PlayerJoin;
import mindustry.game.EventType.PlayerLeave;
import mindustry.game.EventType.PlayerUnbanEvent;
import mindustry.game.EventType.ServerLoadEvent;
import mindustry.game.EventType.TapEvent;
import mindustry.game.EventType.Trigger;
import mindustry.game.EventType.WorldLoadEvent;
import mindustry.game.Team;
import mindustry.game.Teams.TeamData;
import mindustry.gen.*;
import mindustry.ui.Menus;
import mindustry.maps.Map;
import mindustry.mod.Plugin;
import mindustry.net.Administration;
import mindustry.type.*;
import mindustry.world.Block;
import mindustry.world.Tile;
import okhttp3.OkHttpClient;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import pandorum.comp.*;
import pandorum.comp.Config.PluginType;
import pandorum.database.ArrowSubscriber;
import pandorum.entry.ConfigEntry;
import pandorum.entry.HistoryEntry;
import pandorum.events.*;
import pandorum.models.PlayerInfo;
import pandorum.struct.CacheSeq;
import pandorum.struct.Tuple2;
import pandorum.vote.*;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

import static mindustry.Vars.*;
import static pandorum.Misc.*;

public final class PandorumPlugin extends Plugin{

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
    public static final Seq<String> alertIgnores = new Seq<>();
    public static final Seq<String> activeHistoryPlayers = new Seq<>();
    public static final Interval interval = new Interval(2);

    public static CacheSeq<HistoryEntry>[][] history;
    public static final Seq<RainbowPlayerEntry> rainbow = new Seq<>();

    public static ObjectMap<Unit, Float> timer = new ObjectMap<>();

    public static MongoClient mongoClient;
    public static MongoCollection<Document> playersInfoCollection;
    public static Seq<Document> playersInfo = new Seq<>();
    public static PlayerInfo playerInfoSchema;

    public static final ObjectMap<String, String> codeLanguages = new ObjectMap<>();
    public static final OkHttpClient client = new OkHttpClient();
    public PandorumPlugin() throws IOException {
        ConnectionString connString = new ConnectionString("mongodb+srv://host:BmnP4NEpht8wQFqv@darkdustry.aztzv.mongodb.net");

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .retryWrites(true)
                .build();
        mongoClient = MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase("darkdustry");
        playersInfoCollection = database.getCollection("playersinfo");
        playerInfoSchema = new PlayerInfo(playersInfoCollection);

        Fi file = dataDirectory.child("config.json");
        if(!file.exists()){
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
                            case DELETE -> playersInfo.remove(playerInfoIndex);
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
        try {
            forbiddenIps = Seq.with(Streams.copyString(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("vpn-ipv4.txt"))).split(System.lineSeparator())).map(IpInfo::new);
        } catch(Exception e) {
            throw new ArcRuntimeException(e);
        }

        Administration.Config.showConnectMessages.set(false);
        Administration.Config.strict.set(true);
        Administration.Config.motd.set("off");

        netServer.admins.addActionFilter(ActionFilter::call);
        netServer.admins.addChatFilter(ChatFilter::call);

        Events.on(PlayerUnbanEvent.class, pandorum.events.PlayerUnbanEvent::call);
        Events.on(PlayerBanEvent.class, pandorum.events.PlayerBanEvent::call);
        Events.on(ServerLoadEvent.class, pandorum.events.ServerLoadEvent::call);
        Events.on(WorldLoadEvent.class, pandorum.events.WorldLoadEvent::call);
        Events.on(BlockBuildEndEvent.class, pandorum.events.BlockBuildEndEvent::call);
        Events.on(ConfigEvent.class, pandorum.events.ConfigEvent::call);
        Events.on(TapEvent.class, pandorum.events.TapEvent::call);
        Events.on(DepositEvent.class, pandorum.events.DepositEvent::call);
        Events.on(BuildSelectEvent.class, pandorum.events.BuildSelectEvent::call);
        Events.on(PlayerJoin.class, PlayerJoinEvent::call);
        Events.on(PlayerLeave.class, PlayerLeaveEvent::call);
        Events.on(GameOverEvent.class, pandorum.events.GameOverEvent::call);
        Events.run(Trigger.update, TriggerUpdate::call);

        Timer.schedule(() -> rainbow.each(r -> Groups.player.contains(p -> p == r.player), r -> {
            int hue = r.hue;
            if(hue < 360) hue++;
            else hue = 0;

            String hex = "[#" + Integer.toHexString(Color.getHSBColor(hue / 360f, 1f, 1f).getRGB()).substring(2) + "]";
            r.player.name = hex + r.stripedName;
            r.hue = hue;
        }), 0f, 0.05f);

        Menus.registerMenu(1, (player, option) -> {
            if (option == 1) {
                Document playerInfo = playersInfo.find((playerInfo2) -> playerInfo2.getString("uuid").equals(player.uuid()));
                playerInfo.replace("hellomsg", false);
                savePlayerStats(player.uuid());
                bundled(player, "events.hellomsg.disabled");
            }
        });
    }
   
    @Override
    public void registerServerCommands(CommandHandler handler) {

        handler.register("reload-config", "Перезапустить файл конфигов.", args -> {
            config = gson.fromJson(dataDirectory.child("config.json").readString(), Config.class);
            Log.info("Перезагружено.");
        });

        handler.register("despw", "Убить всех юнитов на карте.", args -> {
            int amount = Groups.unit.size();
            Groups.unit.each(Unitc::kill);
            Log.info("Ты убил " + amount + " юнитов!");
            WebhookEmbedBuilder despwEmbedBuilder = new WebhookEmbedBuilder()
                .setColor(0xFF0000)
                .setTitle(new WebhookEmbed.EmbedTitle("Все юниты убиты!", null));
            DiscordWebhookManager.client.send(despwEmbedBuilder.build());
        });

        handler.register("unban-all", "Разбанить всех.", args -> {
            netServer.admins.getBanned().each(unban -> netServer.admins.unbanPlayerID(unban.id));
            netServer.admins.getBannedIPs().each(ip -> netServer.admins.unbanPlayerIP(ip));
            Log.info("Все игроки разбанены!");
        });

        handler.register("rr", "Перезапустить сервер.", args -> {
            Log.info("Перезапуск сервера...");
            WebhookEmbedBuilder banEmbedBuilder = new WebhookEmbedBuilder()
                .setColor(0xFF0000)
                .setTitle(new WebhookEmbed.EmbedTitle("Сервер выключился для перезапуска!", null));
            DiscordWebhookManager.client.send(banEmbedBuilder.build());
            
            Timer.schedule(() -> System.exit(2), 5f);
        });

        handler.removeCommand("say");
        handler.register("say", "<сообщение...>", "Сказать в чат от имени сервера.", args -> {
            Call.sendMessage("[lime]Server[white]: " + args[0]);
            Log.info("Server: &ly@", args[0]);
            DiscordWebhookManager.client.send(String.format("**[Сервер]:** %s", args[0].replaceAll("https?://|@", "")));
        });

        if(config.type == PluginType.sand || config.type == PluginType.anarchy) {
            handler.register("despawndelay", "[новое_значение]", "Изменить/показать текущую продолжительность жизни юнитов.", args -> {
                if (args.length == 0) {
                    Log.info("Задержка деспавна юнитов сейчас: @", Core.settings.getFloat("despawndelay", 36000f));
                    return;
                }
                if (!Strings.canParsePositiveInt(args[0])) {
                    Log.err("Новое значение должно быть положительным целым числом.");
                    return;
                }
                Core.settings.put("despawndelay", Strings.parseFloat(args[0]));
            });
        }
    }

    @Override
    public void registerClientCommands(CommandHandler handler){
        handler.removeCommand("a");
        handler.removeCommand("t");

        handler.removeCommand("help");
        handler.removeCommand("votekick");
        handler.removeCommand("vote");

        handler.<Player>register("help", "[page]", "Список всех команд.", (args, player) -> {
            if(args.length > 0 && !Strings.canParseInt(args[0])) {
                bundled(player, "commands.page-not-int");
                return;
            }
            int commandsPerPage = 6;
            int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
            int pages = Mathf.ceil((float)netServer.clientCommands.getCommandList().size / commandsPerPage);

            page--;

            if(page >= pages || page < 0){
                bundled(player, "commands.under-page", String.valueOf(pages));
                return;
            }

            StringBuilder result = new StringBuilder();
            result.append(Bundle.format("commands.help.page", findLocale(player.locale), page + 1, pages)).append("\n");

            for(int i = commandsPerPage * page; i < Math.min(commandsPerPage * (page + 1), netServer.clientCommands.getCommandList().size); i++){
                CommandHandler.Command command = netServer.clientCommands.getCommandList().get(i);
                String desc = Bundle.format("commands." + command.text + ".description", findLocale(player.locale));
                if(desc.startsWith("?")) {
                    desc = command.description;
                }
                result.append("[orange] /").append(command.text).append("[white] ").append(command.paramText).append("[lightgray] - ").append(desc).append("\n");
            }
            player.sendMessage(result.toString());
        });

        handler.<Player>register("a", "<message...>", "Отправить сообщение админам.", (args, player) -> {
            if (Misc.adminCheck(player)) return;
            Groups.player.each(Player::admin, otherPlayer -> bundled(otherPlayer, "commands.admin.a.chat", Misc.colorizedName(player), args[0]));
        });

        handler.<Player>register("t", "<message...>", "Отправить сообщение игрокам твоей команды.", (args, player) -> {
            String teamColor = "[#" + player.team().color + "]";
            Groups.player.each(o -> o.team() == player.team(), otherPlayer -> bundled(otherPlayer, "commands.t.chat", teamColor, Misc.colorizedName(player), args[0]));
        });

        handler.<Player>register("history", "Переключение отображения истории при нажатии на тайл.", (args, player) -> {
            String uuid = player.uuid();
            if(activeHistoryPlayers.contains(uuid)){
                activeHistoryPlayers.remove(uuid);
                bundled(player, "commands.history.off");
            }else{
                activeHistoryPlayers.add(uuid);
                bundled(player, "commands.history.on");
            }
        });

        handler.<Player>register("pl", "[page]", "Вывести список игроков и их ID.", (args, player) -> {
            if(args.length > 0 && !Strings.canParseInt(args[0])){
                bundled(player, "commands.page-not-int");
                return;
            }

            int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
            int pages = Mathf.ceil((float)Groups.player.size() / 6);

            page--;

            if(page >= pages || page < 0){
                bundled(player, "commands.under-page", pages);
                return;
            }

            StringBuilder result = new StringBuilder();
            result.append(Bundle.format("commands.pl.page", findLocale(player.locale), page + 1, pages)).append("\n");

            for(int i = 6 * page; i < Math.min(6 * (page + 1), Groups.player.size()); i++){
                Player t = Groups.player.index(i);
                result.append("[#9c88ee]* []").append(t.name).append(" [accent]/ [cyan]ID: ").append(t.id());

                if(player.admin){
                    result.append(" [accent]/ [cyan]raw: ").append(t.name.replaceAll("\\[", "[["));
                }
                result.append("\n");
            }
            player.sendMessage(result.toString());
        });

        handler.<Player>register("despw", "Убить всех юнитов на карте.", (args, player) -> {
            int amount = Groups.unit.size();
            if(Misc.adminCheck(player)) return;
            Groups.unit.each(Unitc::kill);
            bundled(player, "commands.admin.despw.success", amount);
            WebhookEmbedBuilder despwEmbedBuilder = new WebhookEmbedBuilder()
                .setColor(0xFF0000)
                .setTitle(new WebhookEmbed.EmbedTitle("Все юниты убиты!", null))
                .addField(new WebhookEmbed.EmbedField(true, "Imposter", Strings.stripColors(player.name)));
            DiscordWebhookManager.client.send(despwEmbedBuilder.build());
        });

        if(config.type != PluginType.other) {
            handler.<Player>register("rtv", "Проголосовать за смену карты.", (args, player) -> {
                if(votesRTV.contains(player.uuid())){
                    bundled(player, "commands.already-voted");
                    return;
                }

                votesRTV.add(player.uuid());
                int cur = votesRTV.size;
                int req = (int)Math.ceil(config.voteRatio * Groups.player.size());
                sendToChat("commands.rtv.ok", Misc.colorizedName(player), cur, req);

                if(cur < req){
                    return;
                }

                sendToChat("commands.rtv.successful");
                votesRTV.clear();
                Events.fire(new GameOverEvent(Team.crux));
            });

            handler.<Player>register("vnw", "Проголосовать за пропуск волны.", (args, player) -> {
                if(votesVNW.contains(player.uuid())){
                    bundled(player, "commands.already-voted");
                    return;
                }

                votesVNW.add(player.uuid());
                int cur = votesVNW.size;
                int req = (int)Math.ceil(config.voteRatio * Groups.player.size());
                sendToChat("commands.vnw.ok", Misc.colorizedName(player), cur, req);

                if(cur < req){
                    return;
                }

                sendToChat("commands.vnw.successful");
                votesVNW.clear();
                Vars.logic.runWave();
            });

            handler.<Player>register("artv", "Принудительно завершить игру.", (args, player) -> {
                if(Misc.adminCheck(player)) return;
                Events.fire(new GameOverEvent(Team.crux));
                sendToChat("commands.admin.artv.info");
                WebhookEmbedBuilder artvEmbedBuilder = new WebhookEmbedBuilder()
                    .setColor(0xFF0000)
                    .setTitle(new WebhookEmbed.EmbedTitle("Игра принудительно завершена!", null))
                    .addField(new WebhookEmbed.EmbedField(true, "Imposter", Strings.stripColors(player.name)));
                DiscordWebhookManager.client.send(artvEmbedBuilder.build());
            });

            handler.<Player>register("core", "<small/medium/big>", "Заспавнить ядро.", (args, player) -> {
                if(Misc.adminCheck(player)) return;

                Block core = switch(args[0].toLowerCase()){
                    case "medium" -> Blocks.coreFoundation;
                    case "big" -> Blocks.coreNucleus;
                    default -> Blocks.coreShard;
                };

                Call.constructFinish(player.tileOn(), core, player.unit(), (byte)0, player.team(), false);

                bundled(player, player.tileOn().block() == core ? "commands.admin.core.success" : "commands.admin.core.failed");
            });

            handler.<Player>register("give", "<item> [count]", "Выдать ресурсы в ядро.", (args, player) -> {
                if(Misc.adminCheck(player)) return;

                if(args.length > 1 && !Strings.canParseInt(args[1])){
                    bundled(player, "commands.non-int");
                    return;
                }

                int count = args.length > 1 ? Strings.parseInt(args[1]) : 1000;

                Item item = content.items().find(b -> b.name.equalsIgnoreCase(args[0]));
                if(item == null){
                    bundled(player, "commands.admin.give.item-not-found");
                    return;
                }

                TeamData team = state.teams.get(player.team());
                if(!team.hasCore()){
                    bundled(player, "commands.admin.give.core-not-found");
                    return;
                }

                team.core().items.add(item, count);

                bundled(player, "commands.admin.give.success");
            });

            handler.<Player>register("alert", "Включить или отключить предупреждения о постройке реакторов вблизи к ядру.", (args, player) -> {
                if(alertIgnores.contains(player.uuid())){
                    alertIgnores.remove(player.uuid());
                    bundled(player, "commands.alert.on");
                }else{
                    alertIgnores.add(player.uuid());
                    bundled(player, "commands.alert.off");
                }
            });

            handler.<Player>register("team", "<team> [name]", "Смена команды для админов.", (args, player) -> {
                if(Misc.adminCheck(player)) return;

                Team team = Structs.find(Team.all, t -> t.name.equalsIgnoreCase(args[0]));
                if(team == null){
                    bundled(player, "commands.teams");
                    return;
                }
            
                Player target = args.length > 1 ? Misc.findByName(args[1]) : player;
                if(target == null){
                    bundled(player, "commands.player-not-found");
                    return;
                }

                bundled(target, "commands.admin.team.success", Misc.colorizedTeam(team));
                target.team(team);
                String text = args.length > 1 ? "Команда игрока " + target.name() + " изменена на " + team + "." : "Команда изменена на " + team + ".";
                WebhookEmbedBuilder artvEmbedBuilder = new WebhookEmbedBuilder()
                    .setColor(0xFF0000)
                    .setTitle(new WebhookEmbed.EmbedTitle(text, null))
                    .addField(new WebhookEmbed.EmbedField(true, "Администратором", Strings.stripColors(player.name)));
                DiscordWebhookManager.client.send(artvEmbedBuilder.build());
            });

            handler.<Player>register("spectate", "Режим наблюдателя.", (args, player) -> {
                if(Misc.adminCheck(player)) return;
                player.clearUnit();
                player.team(player.team() == Team.derelict ? Team.sharded : Team.derelict);
            });

            handler.<Player>register("map", "Информация о карте.", (args, player) -> bundled(player, "commands.mapname", Vars.state.map.name(), Vars.state.map.author()));

            handler.<Player>register("maps", "[page]", "Вывести список карт.", (args, player) -> {
                if(args.length > 0 && !Strings.canParseInt(args[0])){
                    bundled(player, "commands.page-not-int");
                    return;
                }

                Seq<Map> mapList = Vars.maps.all();
                int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
                int pages = Mathf.ceil(mapList.size / 6f);

                if(--page >= pages || page < 0){
                    bundled(player, "commands.under-page", pages);
                    return;
                }

                StringBuilder result = new StringBuilder();
                result.append(Bundle.format("commands.maps.page", findLocale(player.locale), page + 1, pages)).append("\n");
                for(int i = 6 * page; i < Math.min(6 * (page + 1), mapList.size); i++){
                    result.append("[lightgray] ").append(i + 1).append("[orange] ").append(mapList.get(i).name()).append("[white] ").append("\n");
                }

                player.sendMessage(result.toString());
            });

            handler.<Player>register("saves", "[page]", "Вывести список сохранений.", (args, player) -> {
                if(args.length > 0 && !Strings.canParseInt(args[0])){
                    bundled(player, "commands.page-not-int");
                    return;
                }

                Seq<Fi> saves = Seq.with(Vars.saveDirectory.list()).filter(f -> Objects.equals(f.extension(), Vars.saveExtension));
                int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
                int pages = Mathf.ceil(saves.size / 6.0F);

                if(--page >= pages || page < 0){
                    bundled(player, "commands.under-page", pages);
                    return;
                }

                StringBuilder result = new StringBuilder();
                result.append(Bundle.format("commands.saves.page", findLocale(player.locale), page + 1, pages)).append("\n");
                for(int i = 6 * page; i < Math.min(6 * (page + 1), saves.size); i++){
                    result.append("[lightgray] ").append(i + 1).append("[orange] ").append(saves.get(i).nameWithoutExtension()).append("[white] ").append("\n");
                }

                player.sendMessage(result.toString());
            });

            handler.<Player>register("nominate", "<map/save/load> [name...]", "Начать голосование за смену карты/загрузку карты.", (args, player) -> {
                VoteMode mode;
                try {
                    mode = VoteMode.valueOf(args[0].toLowerCase());
                } catch(Throwable t) {
                    bundled(player, "commands.nominate.incorrect-mode");
                    return;
                }

                if (current[0] != null) {
                    bundled(player, "commands.vote-already-started");
                    return;
                }

                if (args.length == 1) {
                    bundled(player, "commands.nominate.required-second-arg");
                    return;
                }

                switch(mode){
                    case map -> {
                        Map map = Misc.findMap(args[1]);
                        if(map == null){
                            bundled(player, "commands.nominate.map.not-found");
                            return;
                        }
                        VoteSession session = new VoteMapSession(current, map);
                        current[0] = session;
                        session.vote(player, 1);
                    }
                    case save -> {                    
                        VoteSession session = new VoteSaveSession(current, args[1]);
                        current[0] = session;
                        session.vote(player, 1);
                    }
                    case load -> {
                        Fi save = Misc.findSave(args[1]);
                        if(save == null){
                            bundled(player, "commands.nominate.load.not-found");
                            return;
                        }
                        VoteSession session = new VoteLoadSession(current, save);
                        current[0] = session;
                        session.vote(player, 1);
                    }
                }
            });

            handler.<Player>register("y", "Проголосовать.", (args, player) -> {
                 if(current[0] == null) {
                     bundled(player, "commands.no-voting");
                     return;
                 }
                 if(current[0].voted().contains(player.uuid()) || current[0].voted().contains(netServer.admins.getInfo(player.uuid()).lastIP)){
                     bundled(player, "commands.already-voted");
                     return;
                 }
                 current[0].vote(player, 1);
             });

            handler.<Player>register("n", "Проголосовать.", (args, player) -> {
                if(current[0] == null) {
                    bundled(player, "commands.no-voting");
                    return;
                }
                if(current[0].voted().contains(player.uuid()) || current[0].voted().contains(netServer.admins.getInfo(player.uuid()).lastIP)){
                    bundled(player, "commands.already-voted");
                    return;
                }
                current[0].vote(player, -1);
            });
        }

        if(config.type == PluginType.pvp){
            handler.<Player>register("surrender", "Сдаться.", (args, player) -> {
                Team team = player.team();
                Seq<String> teamVotes = surrendered.get(team, Seq::new);
                if(teamVotes.contains(player.uuid())){
                    bundled(player, "commands.already-voted");
                    return;
                }

                teamVotes.add(player.uuid());
                int cur = teamVotes.size;
                int req = (int)Math.ceil(config.voteRatio * Groups.player.count(p -> p.team() == team));
                sendToChat("commands.surrender.ok", Misc.colorizedTeam(team), Misc.colorizedName(player), cur, req);

                if(cur < req){
                    return;
                }

                surrendered.remove(player.team());
                sendToChat("commands.surrender.successful", Misc.colorizedTeam(team));
                Groups.unit.each(u -> u.team == team, u -> Time.run(Mathf.random(360), u::kill));
                for(Tile tile : world.tiles){
                    if(tile.build != null && tile.team() == team){
                        Time.run(Mathf.random(360), tile.build::kill);
                    }
                }
            });
        }

        handler.<Player>register("rainbow", "РАДУГА!", (args, player) -> {
            RainbowPlayerEntry old = rainbow.find(r -> r.player.uuid().equals(player.uuid()));
            if(old != null){
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

        handler.<Player>register("js", "<script...>", "Запустить JS скрипт.", (args, player) -> {
            if (Misc.adminCheck(player)) return;

            String output = Vars.mods.getScripts().runConsole(args[0]);
            player.sendMessage("[lightgray] [accent]" + output);
        });

        handler.<Player>register("hub", "Выйти в Хаб.", (args, player) -> {
            Tuple2<String, Integer> hub = config.parseIp();
            Call.connect(player.con, hub.t1, hub.t2);
        });

        handler.<Player>register("spawn", "<unit> [count] [team]", "Заспавнить юнитов.", (args, player) -> {
            if (Misc.adminCheck(player)) return;

            if(args.length > 1 && !Strings.canParseInt(args[1])){
                bundled(player, "commands.non-int");
                return;
            }

            int count = args.length > 1 ? Strings.parseInt(args[1]) : 1;
            if (count > 25 || count < 1) {
                bundled(player, "commands.admin.spawn.limit");
                return;
            }

            Team team = args.length > 2 ? Structs.find(Team.baseTeams, t -> t.name.equalsIgnoreCase(args[2])) : player.team();
            if (team == null) {
                bundled(player, "commands.teams");
                return;
            }

            UnitType unit = Vars.content.units().find(b -> b.name.equals(args[0]));
            if (unit == null) bundled(player, "commands.unit-not-found");
            else {
                for (int i = 0; count > i; i++) {
                    unit.spawn(team, player.x, player.y);
                }
                bundled(player, "commands.admin.spawn.success", count, unit.name, Misc.colorizedTeam(team));
                WebhookEmbedBuilder artvEmbedBuilder = new WebhookEmbedBuilder()
                        .setColor(0xFF0000)
                        .setTitle(new WebhookEmbed.EmbedTitle("Юниты заспавнены для команды " + team + ".", null))
                        .addField(new WebhookEmbed.EmbedField(true, "Администратором", Strings.stripColors(player.name)))
                        .addField(new WebhookEmbed.EmbedField(true, "Название", unit.name))
                        .addField(new WebhookEmbed.EmbedField(true, "Количетво", Integer.toString(count)));
                DiscordWebhookManager.client.send(artvEmbedBuilder.build());
            }
        });

        handler.<Player>register("units", "<all/change/name> [unit]", "Действия с юнитами.", (args, player) -> {
            switch (args[0]) {
                case "name" -> {
                    if (!player.dead()) bundled(player, "commands.unit-name", player.unit().type().name);
                    else bundled(player, "commands.unit-name.null");
                }
                case "all" -> {
                    StringBuilder builder = new StringBuilder();
                    content.units().each(unit -> {
                        if (!unit.name.equals("block"))
                            builder.append(" ").append(ConfigEntry.icons.get(unit.name)).append(unit.name);
                    });
                    bundled(player, "commands.units.all", builder.toString());
                }
                case "change" -> {
                    if (Misc.adminCheck(player)) return;
                    if (args.length == 1 || args[1].equals("block")) {
                        bundled(player, "commands.units.incorrect");
                        return;
                    }
                    UnitType founded = Vars.content.units().find(u -> u.name.equals(args[1]));
                    if (founded == null) {
                        bundled(player, "commands.unit-not-found");
                        return;
                    }
                    final Unit spawn = founded.spawn(player.team(), player.x(), player.y());
                    spawn.spawnedByCore(true);
                    player.unit(spawn);
                    bundled(player, "commands.units.change.success");
                }
                default -> bundled(player, "commands.units.incorrect");
            }
        });

        handler.<Player>register("unban", "<ip/ID>", "Разбанить игрока.", (args, player) -> {
            if(Misc.adminCheck(player)) return;
            if(netServer.admins.unbanPlayerIP(args[0]) || netServer.admins.unbanPlayerID(args[0])) {
                bundled(player, "commands.admin.unban.success", netServer.admins.getInfo(args[0]).lastName);
            }else{
                bundled(player, "commands.admin.unban.not-banned");
            }
        });

        handler.<Player>register("votekick", "<player...>", "Проголосовать за кик игрока.", (args, player) -> {
            if(!Administration.Config.enableVotekick.bool()){
                bundled(player, "commands.votekick.disabled");
                return;
            }

            if(Groups.player.size() < 3){
                bundled(player, "commands.votekick.not-enough-players");
                return;
            }

            if(currentlyKicking[0] != null){
                bundled(player, "commands.vote-already-started");
                return;
            }

            Player found = Misc.findByName(Strings.stripColors(args[0]));

            if(found != null){
                if(found.admin){
                    bundled(player, "commands.votekick.cannot-kick-admin");
                } else if(found.team() != player.team()) {
                    bundled(player, "commands.votekick.cannot-kick-another-team");
                } else {
                    VoteKickSession session = new VoteKickSession(currentlyKicking, found);
                    session.vote(player, 1);
                    currentlyKicking[0] = session;
                }
            } else {
                bundled(player, "commands.player-not-found");
            }
        });

        handler.<Player>register("vote", "<y/n>", "Решить судьбу игрока.", (arg, player) -> {
            if(currentlyKicking[0] == null) {
                bundled(player, "commands.no-voting");
                return;
            }

            if((currentlyKicking[0].voted().contains(player.uuid()) || currentlyKicking[0].voted().contains(netServer.admins.getInfo(player.uuid()).lastIP))){
                bundled(player, "commands.already-voted");
                return;
            }

            if(currentlyKicking[0].target() == player){
                bundled(player, "commands.vote.cannot-vote-for-yourself");
                return;
            }

            if(currentlyKicking[0].target().team() != player.team()){
                bundled(player, "commands.vote.cannot-vote-another-team");
                return;
            }

            int sign = switch(arg[0].toLowerCase()){
                case "y", "yes" ->  1;
                case "n", "no" -> -1;
                default -> 0;
            };

            if(sign == 0){
                bundled(player, "commands.vote.incorrect-args");
                return;
            }

            currentlyKicking[0].vote(player, sign);
        });

        handler.<Player>register("sync", "Синхронизация с сервером.", (args, player) -> {
            if(Time.timeSinceMillis(player.getInfo().lastSyncTime) < 1000 * 15) {
                bundled(player, "commands.sync.time");
                return;
            }

            player.getInfo().lastSyncTime = Time.millis();
            Call.worldDataBegin(player.con);
            netServer.sendWorldData(player);
        });

        handler.<Player>register("tr", "<off/auto/current/locale>", "Переключение переводчика чата.", (args, player) -> {
            Document playerInfo = playersInfo.find((playerInfo2) -> playerInfo2.getString("uuid").equals(player.uuid()));
            if (playerInfo == null) {
                playerInfo = playerInfoSchema.create(player.uuid(), true, false, "off");
                playersInfo.add(playerInfo);
            }

            switch (args[0]) {
                case "current" -> {
                    String locale = playerInfo.getString("locale");
                    bundled(player, "commands.tr.current", locale == null ? "off" : locale);
                }
                case "list" -> {
                    StringBuilder builder = new StringBuilder();
                    codeLanguages.keys().forEach(locale -> builder.append(" " + locale));
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

    // TODO впихнуть радугу в отдельный класс
    public static class RainbowPlayerEntry {
        public Player player;
        public int hue;
        public String stripedName;
    }
}
