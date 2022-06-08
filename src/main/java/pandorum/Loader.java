package pandorum;

import arc.Events;
import arc.files.Fi;
import arc.graphics.Color;
import arc.graphics.Colors;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Reflect;
import arc.util.Timer;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.core.NetServer;
import mindustry.core.Version;
import mindustry.game.EventType.*;
import mindustry.graphics.Pal;
import mindustry.net.Administration.Config;
import mindustry.net.Packets.Connect;
import mindustry.net.Packets.ConnectPacket;
import pandorum.commands.client.DespawnCommand;
import pandorum.commands.client.HelpCommand;
import pandorum.commands.client.MapsListCommand;
import pandorum.commands.client.PlayersListCommand;
import pandorum.commands.client.SavesListCommand;
import pandorum.commands.client.*;
import pandorum.commands.discord.*;
import pandorum.commands.discord.BanCommand;
import pandorum.commands.discord.KickCommand;
import pandorum.commands.discord.StatusCommand;
import pandorum.commands.server.*;
import pandorum.components.Bundle;
import pandorum.components.Icons;
import pandorum.components.MapParser;
import pandorum.components.PluginConfig;
import pandorum.data.Database;
import pandorum.discord.Bot;
import pandorum.features.Ranks;
import pandorum.features.Translator;
import pandorum.listeners.Updater;
import pandorum.listeners.events.*;
import pandorum.listeners.filters.ActionManager;
import pandorum.listeners.filters.ChatManager;
import pandorum.listeners.handlers.ConnectHandler;
import pandorum.listeners.handlers.ConnectPacketHandler;
import pandorum.listeners.handlers.InvalidCommandResponseHandler;
import pandorum.listeners.handlers.MenuHandler;

import static mindustry.Vars.*;
import static pandorum.PluginVars.*;
import static pandorum.components.Gamemode.*;

public class Loader {

    public static void loadConfig() {
        Fi configFile = dataDirectory.child(configFileName);
        if (configFile.exists()) {
            config = gson.fromJson(configFile.reader(), PluginConfig.class);
            Log.info("[Darkdustry] Конфигурация загружена. (@)", configFile.absolutePath());
        } else {
            configFile.writeString(gson.toJson(config = new PluginConfig()));
            Log.info("[Darkdustry] Файл конфигурации сгенерирован. (@)", configFile.absolutePath());
        }
    }

    public static void load() {
        Bundle.load();
        Icons.load();
        MapParser.load();

        MenuHandler.load();
        Ranks.load();

        Translator.loadLanguages();

        Database.connect();
        Bot.connect();

        dangerousBuildBlocks.put(Blocks.incinerator, () -> !state.rules.infiniteResources);
        dangerousBuildBlocks.put(Blocks.thoriumReactor, () -> state.rules.reactorExplosions);
        dangerousDepositBlocks.putAll(Blocks.combustionGenerator, Items.blastCompound, Blocks.steamGenerator, Items.blastCompound, Blocks.thoriumReactor, Items.thorium);

        Colors.put("accent", Pal.accent);
        Colors.put("unlaunched", Color.valueOf("8982ed"));
        Colors.put("highlight", Pal.accent.cpy().lerp(Color.white, 0.3f));
        Colors.put("stat", Pal.stat);

        Colors.put("ACCENT", Pal.accent);
        Colors.put("UNLAUNCHED", Color.valueOf("8982ed"));
        Colors.put("HIGHLIGHT", Pal.accent.cpy().lerp(Color.white, 0.3f));
        Colors.put("STAT", Pal.stat);
    }

    public static void init() {
        writeBuffer = Reflect.get(NetServer.class, netServer, "writeBuffer");
        outputBuffer = Reflect.get(NetServer.class, netServer, "outputBuffer");

        net.handleServer(Connect.class, new ConnectHandler());
        net.handleServer(ConnectPacket.class, new ConnectPacketHandler());

        netServer.admins.addActionFilter(new ActionManager());
        netServer.admins.addChatFilter(new ChatManager());
        netServer.invalidHandler = new InvalidCommandResponseHandler();

        Events.on(AdminRequestEvent.class, new OnAdminRequest());
        Events.on(BlockBuildEndEvent.class, new OnBlockBuildEnd());
        Events.on(BuildSelectEvent.class, new OnBuildSelect());
        Events.on(ConfigEvent.class, new OnConfig());
        Events.on(DepositEvent.class, new OnDeposit());
        Events.on(GameOverEvent.class, new OnGameOver());
        Events.on(PlayerJoin.class, new OnPlayerJoin());
        Events.on(PlayerLeave.class, new OnPlayerLeave());
        Events.on(ServerLoadEvent.class, new OnServerLoad());
        Events.on(TapEvent.class, new OnTap());
        Events.on(WithdrawEvent.class, new OnWithdraw());
        Events.on(WorldLoadEvent.class, new OnWorldLoad());

        Events.run(Trigger.update, new OnTriggerUpdate());
        Events.run("HexedGameOver", new OnGameOver());
        Events.run("CastleGameOver", new OnGameOver());

        Config.motd.set("off");
        Config.interactRateWindow.set(3);
        Config.interactRateLimit.set(50);
        Config.interactRateKick.set(1000);
        Config.showConnectMessages.set(false);
        Config.logging.set(true);
        Config.strict.set(true);
        Config.enableVotekick.set(true);

        Version.build = -1;

        Timer.schedule(new Updater(), 0f, 1f);
    }

    public static void registerClientCommands(CommandHandler handler) {
        handler.register("help", "[page]", "commands.help.description", new HelpCommand());
        handler.register("discord", "commands.discord.description", new DiscordLinkCommand());
        handler.register("a", "<message...>", "commands.a.description", new AdminChatCommand());
        handler.register("t", "<message...>", "commands.t.description", new TeamChatCommand());
        handler.register("votekick", "<ID/username...>", "commands.votekick.description", new VoteKickCommand());
        handler.register("vote", "<y/n>", "commands.vote.description", new VoteCommand());
        handler.register("sync", "commands.sync.description", new SyncCommand());
        handler.register("tr", "<current/list/off/auto/locale>", "commands.tr.description", new TranslatorCommand());
        handler.register("stats", "[ID/username...]", "commands.stats.description", new StatsCommand());
        handler.register("rank", "[ID/username...]", "commands.rank.description", new RankCommand());
        handler.register("players", "[page]", "commands.players.description", new PlayersListCommand());
        handler.register("login", "commands.login.description", new LoginCommand());

        if (config.mode != hub) {
            handler.register("hub", "commands.hub.description", new HubCommand());
        }

        if (config.mode == pvp) {
            handler.register("surrender", "commands.surrender.description", new SurrenderCommand());
        }

        if (defaultModes.contains(config.mode)) {
            handler.register("rtv", "commands.rtv.description", new RtvCommand());
            handler.register("vnw", "commands.vnw.description", new VnwCommand());

            handler.register("history", "commands.history.description", new HistoryCommand());
            handler.register("alert", "commands.alert.description", new AlertCommand());

            handler.register("maps", "[page]", "commands.maps.description", new MapsListCommand());
            handler.register("saves", "[page]", "commands.saves.description", new SavesListCommand());
            handler.register("nominate", "<map/save/load> <name...>", "commands.nominate.description", new NominateCommand());
            handler.register("voting", "<y/n>", "commands.voting.description", new VotingCommand());

            handler.register("artv", "commands.artv.description", new ArtvCommand());
            handler.register("despawn", "commands.despawn.description", new DespawnCommand());
            handler.register("fill", "<width> <height> <block>", "commands.fill.description", new FillCommand());
            handler.register("spawn", "<unit> [amount] [team]", "commands.spawn.description", new SpawnCommand());
            handler.register("core", "[small/medium/big] [team]", "commands.core.description", new CoreCommand());
            handler.register("give", "<item> <amount>", "commands.give.description", new GiveCommand());
            handler.register("unit", "<unit> [ID/username...]", "commands.unit.description", new UnitCommand());
            handler.register("team", "<team> [ID/username...]", "commands.team.description", new TeamCommand());
            handler.register("spectate", "[ID/username...]", "commands.spectate.description", new SpectateCommand());
        }
    }

    public static void registerDiscordCommands(CommandHandler handler) {
        handler.register("help", "Список всех команд.", new pandorum.commands.discord.HelpCommand());
        handler.register("ip", "IP адрес сервера.", new IpCommand());
        handler.register("players", "[страница]", "Список игроков сервера.", new pandorum.commands.discord.PlayersListCommand());
        handler.register("status", "Состояние сервера.", new StatusCommand());

        /* Administration commands */
        handler.register("kick", "<игрок>", "Позволяет выгнать игрока с игрового сервера.", new KickCommand());
        handler.register("ban", "<type> <uuid/username/ip...>", "Позволяет забанить игрока на игровом сервере, используя никнейм, IP или UUID.", new BanCommand());

        if (config.mode != hexed) {
            handler.register("map", "<название...>", "Получить карту с сервера.", new pandorum.commands.discord.MapCommand());
            handler.register("maps", "[страница]", "Список карт сервера.", new pandorum.commands.discord.MapsListCommand());

            handler.register("addmap", "Добавить карту на сервер.", new AddMapCommand());
            handler.register("removemap", "<название...>", "Удалить карту с сервера.", new RemoveMapCommand());
        }

        if (defaultModes.contains(config.mode)) {
            handler.register("gameover", "Принудительно завершить игру.", new GameOverCommand());
        }
    }

    public static void registerServerCommands(CommandHandler handler) {
        handler.register("help", "List of all commands.", new pandorum.commands.server.HelpCommand());
        handler.register("exit", "Shut down the server.", new ExitCommand());
        handler.register("host", "[map] [mode]", "Open the server. Will default to a random map and survival gamemode if not specified.", new HostCommand());
        handler.register("maps", "List of all available maps.", new pandorum.commands.server.MapsListCommand());
        handler.register("saves", "List of all available saves.", new pandorum.commands.server.SavesListCommand());
        handler.register("status", "Display server status.", new pandorum.commands.server.StatusCommand());
        handler.register("say", "<message...>", "Send a message to all players.", new SayCommand());
        handler.register("rules", "[remove/add] [name] [value...]", "List, add or remove global rules.", new RulesCommand());
        handler.register("config", "[name] [value...]", "Configure server settings.", new ConfigCommand());
        handler.register("nextmap", "<map...>", "Set the next map to be played after a gameover. Overrides shuffling.", new NextMapCommand());
        handler.register("kick", "<ID/username...>", "Kick a player from the server.", new KickCommand());
        handler.register("ban", "<type> <uuid/username/ip...>", "Ban a player by UUID, name or IP.", new BanCommand());
        handler.register("bans", "[clear]", "List of all banned IPs and UUIDs.", new BansListCommand());
        handler.register("unban", "<uuid/all/ip...>", "Unban a player by UUID or IP.", new UnbanCommand());
        handler.register("pardon", "<uuid/ip...>", "Pardon a kicked player.", new PardonCommand());
        handler.register("admin", "<add/remove> <uuid/username...>", "Make an online user admin.", new AdminCommand());
        handler.register("admins", "[clear]", "List of all admins.", new AdminsListCommand());
        handler.register("players", "List of all online players.", new pandorum.commands.server.PlayersListCommand());
        handler.register("save", "<save...>", "Save game state to a slot.", new SaveCommand());
        handler.register("load", "<save...>", "Load a save from a slot.", new LoadCommand());

        handler.register("despawn", "Kill all units.", new pandorum.commands.server.DespawnCommand());
        handler.register("restart", "Restart the server.", new RestartCommand());
        handler.register("setrank", "<rank> <ID/username...>", "Set a rank for player.", new SetRankCommand());

        handler.register("setdata", "<uuid> <playtime> <buildings> <games>", "Shiza?", new SetDataCommand());
    }
}
