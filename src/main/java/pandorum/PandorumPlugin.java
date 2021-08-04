package pandorum;

import arc.Events;
import arc.Core;
import arc.files.Fi;
import arc.math.Mathf;
import arc.struct.*;
import arc.struct.ObjectMap.Entry;
import arc.util.*;

import com.google.gson.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.maps.Map;
import mindustry.mod.Plugin;
import mindustry.type.*;
import mindustry.net.*;
import mindustry.net.Administration.PlayerInfo;
import mindustry.net.Packets.KickReason;
import mindustry.world.*;
import mindustry.world.blocks.logic.LogicBlock;
import pandorum.comp.*;
import pandorum.comp.Config.PluginType;
import pandorum.entry.*;
import pandorum.struct.*;

import java.io.IOException;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

import static mindustry.Vars.*;
import static pandorum.Misc.*;

@SuppressWarnings("unchecked")
public final class PandorumPlugin extends Plugin{

    public static final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
            .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
            .disableHtmlEscaping()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    public static VoteSession[] current = {null};
    public static Config config;

    private final ObjectMap<Team, ObjectSet<String>> surrendered = new ObjectMap<>();
    private final ObjectSet<String> votesRTV = new ObjectSet<>();
    private final ObjectSet<String> votesVNW = new ObjectSet<>();
    private final ObjectSet<String> alertIgnores = new ObjectSet<>();
    private final ObjectSet<String> activeHistoryPlayers = new ObjectSet<>();
    private final Interval interval = new Interval(2);

    private CacheSeq<HistoryEntry>[][] history;

    private Seq<IpInfo> forbiddenIps;

    ObjectMap<Unit, Float> timer;
    float defDelay;

    public PandorumPlugin(){

        Fi cfg = dataDirectory.child("config.json");
        if(!cfg.exists()){
            cfg.writeString(gson.toJson(config = new Config()));
            Log.info("Config created...");
        }else{
            config = gson.fromJson(cfg.reader(), Config.class);
        }

        if(config.type == PluginType.sand) {
            this.timer = (ObjectMap<Unit, Float>)new ObjectMap();
            this.defDelay = 36000.0f;
        }
    }

    @Override
    public void init(){

        try{
            forbiddenIps = Seq.with(Streams.copyString(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("vpn-ipv4.txt"))).split(System.lineSeparator())).map(IpInfo::new);
        }catch(Throwable t){
            throw new ArcRuntimeException(t);
        }

        Administration.Config.showConnectMessages.set(false);

        netServer.admins.addActionFilter(action -> {
            if(action.type == Administration.ActionType.rotate){
                Building building = action.tile.build;
                CacheSeq<HistoryEntry> entries = history[action.tile.x][action.tile.y];
                HistoryEntry entry = new RotateEntry(Misc.colorizedName(action.player), building.block, action.rotation);
                entries.add(entry);
            }
            return true;
        });

        Events.on(WorldLoadEvent.class, event -> {
            if(config.type == PluginType.sand) this.timer.clear();
            history = new CacheSeq[world.width()][world.height()];

            for(Tile tile : world.tiles){
                history[tile.x][tile.y] = Seqs.newBuilder()
                        .maximumSize(config.historyLimit)
                        .expireAfterWrite(Duration.ofMillis(config.expireDelay))
                        .build();
            }
        });

        Events.on(BlockBuildEndEvent.class, event -> {
            HistoryEntry historyEntry = new BlockEntry(event);

            Seq<Tile> linkedTile = event.tile.getLinkedTiles(new Seq<>());
            for(Tile tile : linkedTile){
                history[tile.x][tile.y].add(historyEntry);
            }
        });

        Events.on(ConfigEvent.class, event -> {
            if(event.tile.block instanceof LogicBlock || event.player == null || event.tile.tileX() > world.width() || event.tile.tileX() > world.height()){
                return;
            }

            CacheSeq<HistoryEntry> entries = history[event.tile.tileX()][event.tile.tileY()];
            boolean connect = true;

            HistoryEntry last = entries.peek();
            if(!entries.isEmpty() && last instanceof ConfigEntry){
                ConfigEntry lastConfigEntry = (ConfigEntry)last;

                connect = !event.tile.getPowerConnections(new Seq<>()).isEmpty() &&
                          !(lastConfigEntry.value instanceof Integer && event.value instanceof Integer &&
                          (int)lastConfigEntry.value == (int)event.value && lastConfigEntry.connect);
            }

            HistoryEntry entry = new ConfigEntry(event, connect);

            Seq<Tile> linkedTile = event.tile.tile.getLinkedTiles(new Seq<>());
            for(Tile tile : linkedTile){
                history[tile.x][tile.y].add(entry);
            }
        });

        Events.on(TapEvent.class, event -> {
            if(activeHistoryPlayers.contains(event.player.uuid())){
                CacheSeq<HistoryEntry> entries = history[event.tile.x][event.tile.y];

                StringBuilder message = new StringBuilder(bundle.format("events.history.title", findLocale(event.player.locale), event.tile.x, event.tile.y));

                entries.cleanUp();
                if(entries.isOverflown()){
                    message.append(bundle.get("events.history.overflow", findLocale(event.player.locale)));
                }

                int i = 0;
                for(HistoryEntry entry : entries){
                    message.append("\n").append(entry.getMessage(event.player));
                    if(++i > 6){
                        break;
                    }
                }

                if(entries.isEmpty()){
                    message.append(bundle.get("events.history.empty", findLocale(event.player.locale)));
                }

                event.player.sendMessage(message.toString());
            }
        });

        Events.on(PlayerConnect.class, event -> {
            Player player = event.player;
            if(config.bannedNames.contains(player.name())){
                player.con.kick(bundle.get("events.unofficial-mindustry", findLocale(player.locale)), 60000);
            }
        });

        Events.on(DepositEvent.class, event -> {
            Building building = event.tile;
            Player target = event.player;
            if(building.block() == Blocks.thoriumReactor && event.item == Items.thorium && target.team().cores().contains(c -> event.tile.dst(c.x, c.y) < config.alertDistance)){
                Groups.player.each(p -> !alertIgnores.contains(p.uuid()), p -> bundled(p, "events.withdraw-thorium", Misc.colorizedName(target), building.tileX(), building.tileY()));
            }
        });

        Events.on(BuildSelectEvent.class, event -> {
            if(!event.breaking && event.builder != null && event.builder.buildPlan() != null &&
                event.builder.buildPlan().block == Blocks.thoriumReactor && event.builder.isPlayer() &&
                event.team.cores().contains(c -> event.tile.dst(c.x, c.y) < config.alertDistance)){
                Player target = event.builder.getPlayer();

                if(interval.get(300)){
                    Groups.player.each(p -> !alertIgnores.contains(p.uuid()), p -> bundled(p, "events.alert", target.name, event.tile.x, event.tile.y));
                }
            }
        });

        Events.on(PlayerJoin.class, event -> {
            forbiddenIps.each(i -> i.matchIp(event.player.con.address), i -> event.player.con.kick(bundle.get("events.vpn-ip", findLocale(event.player.locale)))); 
            sendToChat("server.player-join", colorizedName(event.player));
            Log.info(event.player.name + " зашёл на сервер, IP: " + event.player.ip() + ", ID: " + event.player.uuid());
        });

        Events.on(PlayerLeave.class, event -> {
            activeHistoryPlayers.remove(event.player.uuid());
            sendToChat("server.player-leave", colorizedName(event.player));
            Log.info(event.player.name + " вышел с сервера, IP: " + event.player.ip() + ", ID: " + event.player.uuid());

            if(votesRTV.contains(event.player.uuid())) {
                votesRTV.remove(event.player.uuid());
                int curRTV = votesRTV.size;
                int reqRTV = (int) Math.ceil(config.voteRatio * Groups.player.size());
                sendToChat("commands.rtv.left", Misc.colorizedName(event.player), curRTV, reqRTV);
            }
            if(votesVNW.contains(event.player.uuid())) {
                votesVNW.remove(event.player.uuid());
                int curVNW = votesVNW.size;
                int reqVNW = (int) Math.ceil(config.voteRatio * Groups.player.size());
                sendToChat("commands.vnw.left", Misc.colorizedName(event.player), curVNW, reqVNW);
            }
        });

        Events.on(GameOverEvent.class, e -> {
            votesRTV.clear();
            votesVNW.clear();
        });

        if(config.type == PluginType.pvp){
            Events.on(PlayerLeave.class, event -> {
                String uuid = event.player.uuid();
                ObjectSet<String> uuids = surrendered.get(event.player.team(), ObjectSet::new);
                if(uuids.contains(uuid)){
                    uuids.remove(uuid);
                }
            });

            Events.on(GameOverEvent.class, e -> surrendered.clear());
        }

        if(config.type == PluginType.sand) {
            Events.on(Trigger.class, e -> {
                if (e == Trigger.update) {
                    final float despawnDelay = Core.settings.getFloat("despawndelay", this.defDelay);
                    Groups.unit.each(unit -> {
                        if (Time.globalTime - (float)this.timer.get(unit, () -> Time.globalTime) >= despawnDelay) {
                            unit.spawnedByCore(true);
                        }
                    });
                    for (final Unit key : this.timer.keys()) {
                        if (key == null) return;
                        if (key.isValid()) continue;
                        this.timer.remove(key);
                    }
                }
            });
        }
    }

    @Override
    public void registerServerCommands(CommandHandler handler){

        handler.register("reload-config", "reload configuration", args -> {
            config = gson.fromJson(dataDirectory.child("config.json").readString(), Config.class);
            Log.info("Reloaded");
        });

        handler.register("tell", "<ID/Никнейм> <Сообщение...>", "отправить сообщение игроку", args -> {
            Player target = Groups.player.find(p -> p.name().equalsIgnoreCase(args[0]) || p.uuid().equalsIgnoreCase(args[0]));
            if(target == null){
                Log.info("Игрок не найден!");
                return;
            }
            target.sendMessage("[scarlet][[Server]:[] " + args[1]);
            Log.info("Сервер ---> " + target.name() + ": " + args[1]);
        });

        handler.register("despw", "убить всех юнитов на карте", args -> {
            Groups.unit.each(Unit::kill);
            Log.info("Все юниты убиты!");
        });

        if(config.type == PluginType.sand) {
            handler.register("despawndelay", "[новое_значение]", "Изменить/показать текущую продолжительность жизни юнитов.", args -> {
                if (args.length == 0) {
                    Log.info("DespawnDelay сейчас: @", new Object[] { Core.settings.getFloat("despawndelay", this.defDelay) });
                    return;
                }
                final String value = args[0];
                if (!Strings.canParsePositiveInt(value)) {
                    Log.err("Новое значение должно быть положительным целым числом.", new Object[0]);
                    return;
                }
                Core.settings.put("despawndelay", (Object)Strings.parseFloat(value));
            });
        }
    }

    @Override
    public void registerClientCommands(CommandHandler handler){

        handler.<Player>register("alert", "Включить или отключить предупреждения о постройке реакторов вблизи к ядру", (args, player) -> {
            if(alertIgnores.contains(player.uuid())){
                alertIgnores.remove(player.uuid());
                bundled(player, "commands.alert.on");
            }else{
                alertIgnores.add(player.uuid());
                bundled(player, "commands.alert.off");
            }
        });

        handler.<Player>register("history", "[page] [advanced_mode]", "Переключение отображения истории при нажатии на тайл", (args, player) -> {
            String uuid = player.uuid();
            if(args.length > 0 && activeHistoryPlayers.contains(uuid)){
                if(!Strings.canParseInt(args[0]) && !Misc.bool(args[0])){
                    bundled(player, "commands.page-not-int");
                    return;
                }

                boolean forward = !Strings.canParseInt(args[0]) ? Misc.bool(args[0]) : args.length > 1 && Misc.bool(args[1]);
                int mouseX = Mathf.clamp(Mathf.round(player.mouseX / 8), 1, world.width());
                int mouseY = Mathf.clamp(Mathf.round(player.mouseY / 8), 1, world.height());
                CacheSeq<HistoryEntry> entries = history[mouseX][mouseY];
                int page = Strings.canParseInt(args[0]) ? Strings.parseInt(args[0]) : 1;
                int pages = Mathf.ceil((float)entries.size / 6);

                page--;

                if((page >= pages || page < 0) && !entries.isEmpty()){
                    bundled(player, "commands.under-page", pages);
                    return;
                }

                StringBuilder result = new StringBuilder();
                result.append(bundle.format("commands.history.page", findLocale(player.locale), mouseX, mouseY, page + 1, pages)).append("\n");
                if(entries.isEmpty()){
                    result.append("events.history.empty");
                }

                for(int i = 6 * page; i < Math.min(6 * (page + 1), entries.size); i++){
                    HistoryEntry entry = entries.get(i);

                    result.append(entry.getMessage(player));
                    if(forward){
                        result.append(bundle.format("events.history.last-access-time", findLocale(player.locale), entry.getLastAccessTime(TimeUnit.SECONDS)));
                    }

                    result.append("\n");
                }

                player.sendMessage(result.toString());
            }else if(activeHistoryPlayers.contains(uuid)){
                activeHistoryPlayers.remove(uuid);
                bundled(player, "commands.history.off");
            }else{
                activeHistoryPlayers.add(uuid);
                bundled(player, "commands.history.on");
            }
        });

        if(config.type == PluginType.pvp){
            handler.<Player>register("surrender", "Сдаться", (args, player) -> {
                String uuid = player.uuid();
                Team team = player.team();
                ObjectSet<String> uuids = surrendered.get(team, ObjectSet::new);
                if(uuids.contains(uuid)){
                    bundled(player, "commands.already-voted");
                    return;
                }

                uuids.add(uuid);
                int cur = uuids.size;
                int req = (int)Math.ceil(config.voteRatio * Groups.player.count(p -> p.team() == team));
                sendToChat("commands.surrender.ok",
                                               Misc.colorizedTeam(team),
                                               Misc.colorizedName(player), cur, req);

                if(cur < req){
                    return;
                }

                surrendered.remove(team);
                sendToChat("commands.surrender.successful", Misc.colorizedTeam(team));
                Groups.unit.each(u -> u.team == team, u -> Time.run(Mathf.random(360), u::kill));
                for(Tile tile : world.tiles){
                    if(tile.build != null && tile.team() == team){
                        Time.run(Mathf.random(360), tile.build::kill);
                    }
                }
            });
        }

        handler.<Player>register("pl", "[page]", "Вывести список игроков и их ID", (args, player) -> {
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
            result.append(bundle.format("commands.pl.page", findLocale(player.locale), page + 1, pages)).append("\n");

            for(int i = 6 * page; i < Math.min(6 * (page + 1), Groups.player.size()); i++){
                Player t = Groups.player.index(i);
                result.append("[lightgray]* ").append(t.name).append(" [lightgray]/ ID: ").append(t.id());

                if(player.admin){
                    result.append(" / raw: ").append(t.name.replaceAll("\\[", "[["));
                }
                result.append("\n");
            }
            player.sendMessage(result.toString());
        });

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
            logic.runWave();
        });

        handler.<Player>register("artv", "Принудительно завершить игру.", (args, player) -> {
            if(!adminCheck(player)) return;
            Events.fire(new GameOverEvent(Team.crux));
            //TODO сообщение в чат
        });

        handler.<Player>register("core", "<small/medium/big>", "Заспавнить ядро.", (args, player) -> {
            if(!adminCheck(player)) return;

            Block core = switch(args[0].toLowerCase()){
                case "medium" -> Blocks.coreFoundation;
                case "big" -> Blocks.coreNucleus;
                default -> Blocks.coreShard;
            };

            Call.constructFinish(player.tileOn(), core, player.unit(), (byte)0, player.team(), false);

            bundled(player, player.tileOn().block() == core ? "commands.admin.core.success" : "commands.admin.core.failed");
        });

        handler.<Player>register("hub", "Выйти в Хаб.", (args, player) -> Call.connect(player.con, config.hubIp, config.hubPort));

        handler.<Player>register("team", "<team> [name]", "Смена команды для [scarlet]Админов", (args, player) -> {
            if(!adminCheck(player)) return;

            Team team = Structs.find(Team.all, t -> t.name.equalsIgnoreCase(args[0]));
            if(team == null){
                bundled(player, "commands.admin.team.teams");
                return;
            }

            Player target = args.length > 1 ? Groups.player.find(p -> Strings.stripColors(p.name).equalsIgnoreCase(args[1])) : player;
            if(target == null){
                bundled(player, "commands.player-not-found");
                return;
            }

            bundled(target, "commands.admin.team.success", team.name);
            target.team(team);
        });

        handler.<Player>register("spectate", "Секрет админов.", (args, player) -> {
            if(!adminCheck(player)) return;
            player.clearUnit();
            player.team(player.team() == Team.derelict ? Team.sharded : Team.derelict);
        });

        handler.<Player>register("spawn", "<unit> [count] [team]", "Spawn units.", (args, player) -> {
            if (!adminCheck(player)) return;

            if(args.length > 1 && !Strings.canParseInt(args[1])){
                bundled(player, "commands.spawn.non-int");
                return;
            }

            int count = args.length > 1 ? Strings.parseInt(args[1]) : 1;
            if (count > 25) {
                bundled(player, "commands.spawn.limit");
                return;
            }

            Team team = args.length > 2 ? Structs.find(Team.baseTeams, t -> t.name.equalsIgnoreCase(args[2])) : player.team();
            if (team == null) {
            	bundled(player, "commands.admin.team.teams");
            	return;
            }

            UnitType unit = content.units().find(b -> b.name.equals(args[0]));
            if (unit == null) bundled(player, "commands.units.unit-not-found");
            else {            
                for (int i = 0; count > i; i++) {
                    unit.spawn(team, player.x, player.y);
                }
                bundled(player, "commands.spawn.success", count, unit.name, colorizedTeam(team));
            }
        });

        handler.<Player>register("maps", "[page]", "Вывести список карт.", (args, player) -> {
            if(args.length > 0 && !Strings.canParseInt(args[0])){
                bundled(player, "commands.page-not-int");
                return;
            }

            Seq<Map> mapList = maps.all();
            int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
            int pages = Mathf.ceil(mapList.size / 6f);

            if(--page >= pages || page < 0){
                bundled(player, "commands.under-page", pages);
                return;
            }

            StringBuilder result = new StringBuilder();
            result.append(bundle.format("commands.maps.page", findLocale(player.locale), page + 1, pages)).append("\n");
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

            Seq<Fi> saves = Seq.with(saveDirectory.list()).filter(f -> Objects.equals(f.extension(), saveExtension));
            int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
            int pages = Mathf.ceil(saves.size / 6.0F);

            if(--page >= pages || page < 0){
                bundled(player, "commands.under-page", pages);
                return;
            }

            StringBuilder result = new StringBuilder();
            result.append(bundle.format("commands.saves.page", findLocale(player.locale), page + 1, pages)).append("\n");
            for(int i = 6 * page; i < Math.min(6 * (page + 1), saves.size); i++){
                result.append("[lightgray] ").append(i + 1).append("[orange] ").append(saves.get(i).nameWithoutExtension()).append("[white] ").append("\n");
            }

            player.sendMessage(result.toString());
        });

        handler.<Player>register("nominate", "<map/save/load> [название...]", "Начать голосование за смену карты/загрузку карты.", (args, player) -> {
            VoteMode mode;
            try{
                mode = VoteMode.valueOf(args[0].toLowerCase());
            }catch(Throwable t){
                bundled(player, "commands.nominate.incorrect-mode");
                return;
            }

            if(current[0] != null){
                bundled(player, "commands.nominate.already-started");
                return;
            }

            switch(mode){
                case map -> {
                    if(args.length == 1){
                        bundled(player, "commands.nominate.required-second-arg");
                        return;
                    }

                    Map map = findMap(args[1]);
                    if(map == null){
                        bundled(player, "commands.nominate.map.not-found");
                        return;
                    }

                    VoteSession session = new VoteMapSession(current, map);
                    current[0] = session;
                    session.vote(player, 1);
                }
                case save -> {
                    if(args.length == 1){
                        bundled(player, "commands.nominate.required-second-arg");
                        return;
                    }

                    VoteSession session = new VoteSaveSession(current, args[1]);
                    current[0] = session;
                    session.vote(player, 1);
                }
                case load -> {
                    if(args.length == 1){
                        bundled(player, "commands.nominate.required-second-arg");
                        return;
                    }

                    Fi save = findSave(args[1]);
                    if(save == null){
                        player.sendMessage("commands.nominate.load.not-found");
                        return;
                    }

                    VoteSession session = new VoteLoadSession(current, save);
                    current[0] = session;
                    session.vote(player, 1);
                }
            }
        });

        handler.<Player>register("playerinfo", "<name/ip/id...>", "Информация о игроке.", (args, player) -> {
            if(!adminCheck(player)) return;

            ObjectSet<Administration.PlayerInfo> infos = netServer.admins.findByName(args[0]);
            if (infos.size > 0) {
                Log.info("Players found: @", infos.size);
                int i = 0;
                for(PlayerInfo playerInfo : infos){
                    StringBuilder result = new StringBuilder();
                    result.append(Strings.format("[@] @ '@' / UUID @", i++, bundle.get("commands.playerinfo.header", findLocale(player.locale)), playerInfo.lastName, playerInfo.id));
                    result.append(Strings.format("  @: @", bundle.get("commands.playerinfo.names", findLocale(player.locale)), playerInfo.names));
                    if(player.admin){
                        result.append("  IP: ").append(playerInfo.lastIP);
                        result.append(Strings.format("  IPs : @", playerInfo.ips));
                    }
                    result.append("  ").append(bundle.get("commands.playerinfo.joined", findLocale(player.locale))).append(": ").append(playerInfo.timesJoined);
                    result.append("  ").append(bundle.get("commands.playerinfo.kicked", findLocale(player.locale))).append(": ").append(playerInfo.timesKicked);
                    Call.infoMessage(player.con(), result.toString());
                }
            } else {
                bundled(player, "commands.player-not-found");
            }
        });

        handler.<Player>register("y", "Проголосовать [lime]за", (args, player) -> {
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

        handler.<Player>register("n", "Проголосовать [scarlet]против", (args, player) -> {
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
}
