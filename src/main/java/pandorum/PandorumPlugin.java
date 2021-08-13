package pandorum;

import static mindustry.Vars.*;
import static pandorum.Misc.*;
import static pandorum.events.PlayerJoinEvent.call;
import static pandorum.events.PlayerLeaveEvent.call;
import static pandorum.events.GameOverEvent.call;
import static pandorum.events.TriggerUpdate.call;
import static pandorum.events.BuildSelectEvent.call;
import static pandorum.events.DepositEvent.call;
import static pandorum.events.TapEvent.call;
import static pandorum.events.ConfigEvent.call;
import static pandorum.events.BlockBuildEndEvent.call;
import static pandorum.events.WorldLoadEvent.call;
import static pandorum.events.ServerLoadEvent.call;
import static pandorum.events.PlayerBanEvent.call;
import static pandorum.events.PlayerUnbanEvent.call;
import static pandorum.events.ActionFilter.call;
import static pandorum.events.ChatFilter.call;

import java.awt.Color;
import java.util.Objects;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.math.Mathf;
import arc.struct.*;
import arc.util.*;
import arc.util.io.Streams;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.EventType.*;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.maps.Map;
import mindustry.mod.Plugin;
import mindustry.net.Administration;
import mindustry.net.Administration.PlayerInfo;
import mindustry.net.Packets.KickReason;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.Tile;
import pandorum.comp.*;
import pandorum.comp.Config.PluginType;
import pandorum.entry.*;
import pandorum.struct.*;
import pandorum.vote.*;

@SuppressWarnings("unchecked")
public final class PandorumPlugin extends Plugin{

    public final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
            .setPrettyPrinting()
            .serializeNulls()
            .disableHtmlEscaping()
            .create();

    public static VoteSession[] current = {null};
    public static Config config;
    public static Seq<IpInfo> forbiddenIps;

    public static final ObjectMap<Team, ObjectSet<String>> surrendered = new ObjectMap<>();
    public static final ObjectSet<String> votesRTV = new ObjectSet<>();
    public static final ObjectSet<String> votesVNW = new ObjectSet<>();
    public static final ObjectSet<String> alertIgnores = new ObjectSet<>();
    public static final ObjectSet<String> activeHistoryPlayers = new ObjectSet<>();
    public static final Interval interval = new Interval(2);

    public static CacheSeq<HistoryEntry>[][] history;

    public static final Seq<RainbowPlayerEntry> rainbow = new Seq<>();

    public static ObjectMap<Unit, Float> timer = new ObjectMap<Unit, Float>();
    public static float defDelay = 36000f;

    public PandorumPlugin(){

        Fi cfg = dataDirectory.child("config.json");
        if(!cfg.exists()){
            cfg.writeString(gson.toJson(config = new Config()));
            Log.info("Файл config.json успешно сгенерирован!");
        }else{
            config = gson.fromJson(cfg.reader(), Config.class);
        }
    }

    @Override
    public void init() {

        try{
            forbiddenIps = Seq.with(Streams.copyString(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("vpn-ipv4.txt"))).split(System.lineSeparator())).map(IpInfo::new);
        }catch(Throwable t){
            throw new ArcRuntimeException(t);
        }

        Administration.Config.showConnectMessages.set(false);
        Administration.Config.strict.set(true);
        Administration.Config.motd.set("off");

        netServer.admins.addActionFilter(action -> call(action));
        netServer.admins.addChatFilter((player, text) -> call(player, text));

        Events.on(PlayerUnbanEvent.class, event -> call(event));
        Events.on(PlayerBanEvent.class, event -> call(event));
        Events.on(ServerLoadEvent.class, event -> call(event));
        Events.on(WorldLoadEvent.class, event -> call(event));
        Events.on(BlockBuildEndEvent.class, event -> call(event));
        Events.on(ConfigEvent.class, event -> call(event));
        Events.on(TapEvent.class, event -> call(event));
        Events.on(DepositEvent.class, event -> call(event));
        Events.on(BuildSelectEvent.class, event -> call(event));
        Events.on(PlayerJoin.class, event -> call(event));
        Events.on(PlayerLeave.class, event -> call(event));
        Events.on(GameOverEvent.class, event -> call(event));
        Events.run(Trigger.update, () -> call());

        arc.util.Timer.schedule(() -> rainbow.each(r -> Groups.player.contains(p -> p == r.player), r -> {
            int hue = r.hue;
            if(hue < 360){
                hue++;
            }else{
                hue = 0;
            }

            String hex = "[#" + Integer.toHexString(Color.getHSBColor(hue / 360f, 1f, 1f).getRGB()).substring(2) + "]";
            r.player.name = hex + r.stripedName;
            r.hue = hue;
        }), 0f, 0.05f);
    }

    @Override
    public void registerServerCommands(CommandHandler handler){

        handler.register("reload-config", "Перезапустить файл конфигов.", args -> {
            config = gson.fromJson(dataDirectory.child("config.json").readString(), Config.class);
            Log.info("Reloaded");
        });

        handler.register("despw", "Убить всех юнитов на карте", args -> {
            int amount = Groups.unit.size();
            Groups.unit.each(unit -> unit.kill());
            Log.info("Ты убил " + amount + " юнитов"!);
            DiscordSender.send("Сервер", "Все юниты убиты!", new Color(253, 14, 53));
        });

        handler.register("unban-all", "Разбанить всех", arg -> {
            netServer.admins.getBanned().each(unban -> netServer.admins.unbanPlayerID(unban.id));
            netServer.admins.getBannedIPs().each(ip -> netServer.admins.unbanPlayerIP(ip));
            Log.info("Все игроки разбанены!");
        });

        handler.register("rr", "Перезапустить сервер", args -> {
            Log.info("Перезапуск сервера...");
            System.exit(2);
        });

        handler.removeCommand("say");
        handler.register("say", "<Сообщение...>", "Сказать от имени сервера.", arg -> {
            Call.sendMessage("[lime]Server[white]: " + arg[0]);
            Log.info("Server: &ly" + arg[0]);
            DiscordSender.send("Сервер ---> игрокам", arg[0]);
        });

        if(config.type == PluginType.sand || config.type == PluginType.anarchy) {
            handler.register("despawndelay", "[новое_значение]", "Изменить/показать текущую продолжительность жизни юнитов.", args -> {
                if (args.length == 0) {
                    Log.info("DespawnDelay сейчас: @", new Object[] { Core.settings.getFloat("despawndelay", defDelay) });
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
        handler.removeCommand("a");
        handler.removeCommand("t");

        handler.removeCommand("help");

        handler.<Player>register("help", "[page]", "Lists all commands.", (args, player) -> {
            if(args.length > 0 && !Strings.canParseInt(args[0])) {
                bundled(player, "commands.page-not-int");
                return;
            }
            int commandsPerPage = 6;
            int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
            int pages = Mathf.ceil((float)netServer.clientCommands.getCommandList().size / commandsPerPage);

            --page;

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

        handler.<Player>register("a", "<message...>", "Send a message to admins.", (args, player) -> {
            if (!Misc.adminCheck(player)) return;
            Groups.player.each(Player::admin, otherPlayer -> bundled(otherPlayer, "commands.a.chat", Misc.colorizedName(player), args[0]));
        });

        handler.<Player>register("t", "<message...>", "Send a message to teammates.", (args, player) -> {
            String teamColor = "[#" + player.team().color + "]";
            Groups.player.each(o -> o.team() == player.team(), otherPlayer -> bundled(otherPlayer, "commands.t.chat", teamColor, Misc.colorizedName(player), args[0]));
        });

        handler.<Player>register("history", "Переключение отображения истории при нажатии на тайл", (args, player) -> {
            String uuid = player.uuid();
            if(activeHistoryPlayers.contains(uuid)){
                activeHistoryPlayers.remove(uuid);
                bundled(player, "commands.history.off");
            }else{
                activeHistoryPlayers.add(uuid);
                bundled(player, "commands.history.on");
            }
        });

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
            result.append(Bundle.format("commands.pl.page", findLocale(player.locale), page + 1, pages)).append("\n");

            for(int i = 6 * page; i < Math.min(6 * (page + 1), Groups.player.size()); i++){
                Player t = Groups.player.index(i);
                result.append("[#9c88ee]* ").append(t.name).append(" [accent]/ [cyan]ID: ").append(t.id());

                if(player.admin){
                    result.append(" [accent]/ [cyan]raw: ").append(t.name.replaceAll("\\[", "[["));
                }
                result.append("\n");
            }
            player.sendMessage(result.toString());
        });

        handler.<Player>register("despw", "Убить всех юнитов на карте", (args, player) -> {
            if(!Misc.adminCheck(player)) return;
            Groups.unit.each(unit -> unit.kill());
            bundled(player, "commands.despw.success", amount);
            DiscordSender.send(Strings.stripColors(player.name), "Убил всех юнитов!", new Color(253, 14, 53));
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
                if(!Misc.adminCheck(player)) return;
                Events.fire(new GameOverEvent(Team.crux));
                sendToChat("commands.artv.info");
                DiscordSender.send(Strings.stripColors(player.name), "Принудительно завершил игру.", new Color(110, 237, 139));
            });

            handler.<Player>register("core", "<small/medium/big>", "Заспавнить ядро.", (args, player) -> {
                if(!Misc.adminCheck(player)) return;

                Block core = switch(args[0].toLowerCase()){
                    case "medium" -> Blocks.coreFoundation;
                    case "big" -> Blocks.coreNucleus;
                    default -> Blocks.coreShard;
                };

                Call.constructFinish(player.tileOn(), core, player.unit(), (byte)0, player.team(), false);

                bundled(player, player.tileOn().block() == core ? "commands.admin.core.success" : "commands.admin.core.failed");
            });

            handler.<Player>register("alert", "Включить или отключить предупреждения о постройке реакторов вблизи к ядру", (args, player) -> {
                if(alertIgnores.contains(player.uuid())){
                    alertIgnores.remove(player.uuid());
                    bundled(player, "commands.alert.on");
                }else{
                    alertIgnores.add(player.uuid());
                    bundled(player, "commands.alert.off");
                }
            });
        }

        handler.<Player>register("hub", "Выйти в Хаб.", (args, player) -> {
            Tuple2<String, Integer> hub = config.parseIp();
            Call.connect(player.con, hub.t1, hub.t2);
        });

        handler.<Player>register("team", "<team> [name]", "Смена команды для [scarlet]Админов", (args, player) -> {
            if(!Misc.adminCheck(player)) return;

            Team team = Structs.find(Team.all, t -> t.name.equalsIgnoreCase(args[0]));
            if(team == null){
                bundled(player, "commands.admin.team.teams");
                return;
            }
            
            Player target = args.length > 1 ? Misc.findByName(args[1]) : player;
            if(target == null){
                bundled(player, "commands.player-not-found");
                return;
            }

            bundled(target, "commands.admin.team.success", Misc.colorizedTeam(team));
            target.team(team);
            String text = args.length > 1 ? "Изменил команду игрока " + target.name() + " на " + team + "." : "Изменил свою команду на " + team + ".";
            DiscordSender.send(Strings.stripColors(player.name), text, new Color(204, 82, 27));
        });

        handler.<Player>register("admins", "Admins list", (arg, player) -> {
            Seq<Administration.PlayerInfo> admins = netServer.admins.getAdmins();

            if (admins.size == 0) {
                bundled(player, "commands.admins.no-admins");
                return;
            }
            bundled(player, "commands.admins");
            admins.each((admin) -> player.sendMessage(admin.lastName));
        });

        handler.<Player>register("spectate", "Admins secret.", (args, player) -> {
            if(!Misc.adminCheck(player)) return;
            player.clearUnit();
            player.team(player.team() == Team.derelict ? Team.sharded : Team.derelict);
        });

        handler.<Player>register("map", "Info about the map", (args, player) -> bundled(player, "commands.mapname", Vars.state.map.name(), Vars.state.map.author()));

        handler.<Player>register("spawn", "<unit> [count] [team]", "Spawn units.", (args, player) -> {
            if (!Misc.adminCheck(player)) return;

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

            UnitType unit = Vars.content.units().find(b -> b.name.equals(args[0]));
            if (unit == null) bundled(player, "commands.units.unit-not-found");
            else {
                for (int i = 0; count > i; i++) {
                    unit.spawn(team, player.x, player.y);
                }
                bundled(player, "commands.spawn.success", count, unit.name, Misc.colorizedTeam(team));
                DiscordSender.send(Strings.stripColors(player.name), "Заспавнил юнитов для команды " + team + ".", "Название:", unit.name, "Количество:", Integer.toString(count), new Color(204, 82, 27));
            }
        });

        handler.<Player>register("units", "<all/change/name> [unit]", "Actions with units.", (args, player) -> {
            if(args[0].equals("name")) {
                try { bundled(player, "commands.unit-name", player.unit().type().name); }
                catch (NullPointerException e) { bundled(player, "commands.unit-name.null"); }
            } else if (args[0].equals("all")) {
                StringBuilder builder = new StringBuilder();
                Vars.content.units().each(unit -> builder.append("[sky] > [white]" + unit.name));
                bundled(player, "commands.units.all", builder.toString());
            } else if (args[0].equals("change")) {
                if (!Misc.adminCheck(player)) return;
                if(args.length == 1 || args[1].equals("block")) {
                    bundled(player, "commands.units.incorrect");
                    return;
                }
                UnitType founded = Vars.content.units().find(b -> b.name.equals(args[1]));
                if (founded == null) {
                    bundled(player, "commands.units.unit-not-found");
                    return;
                }
                final Unit spawn = founded.spawn(player.team(), player.x(), player.y());
                spawn.spawnedByCore(true);
                player.unit(spawn);
                bundled(player, "commands.units.change.success");
            } else {
                bundled(player, "commands.units.incorrect");
            }
        });

        handler.<Player>register("unban", "<ip/ID>", "Completely unban a person by IP or ID.", (arg,player) -> {
            if(!Misc.adminCheck(player)) return;
            if(netServer.admins.unbanPlayerIP(arg[0]) || netServer.admins.unbanPlayerID(arg[0])) {
                Misc.bundled(player, "commands.unban.success", netServer.admins.getInfo(arg[0]).lastName);
            }else{
                Misc.bundled(player, "commands.unban.not-banned");
            }
        });

        if(config.type != PluginType.other) {
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

                if(args.length == 1){
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
                            player.sendMessage("commands.nominate.load.not-found");
                            return;
                        }

                        VoteSession session = new VoteLoadSession(current, save);
                        current[0] = session;
                        session.vote(player, 1);
                    }
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
                sendToChat("commands.surrender.ok", Misc.colorizedTeam(team), Misc.colorizedName(player), cur, req);

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

        handler.<Player>register("rainbow", "RAINBOW!", (args, player) -> {
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

        handler.<Player>register("js", "<script...>", "Load JavaScript script.", (args, player) -> {
            if (!Misc.adminCheck(player)) return;

            String output = Vars.mods.getScripts().runConsole(args[0]);
            player.sendMessage("> " + (Misc.isError(output) ? "[#ff341c]" + output : output));
        });
    }

    //TODO впихнуть радугу в отдельный класс
    public static class RainbowPlayerEntry {
        public Player player;
        public int hue;
        public String stripedName;
    }
}
