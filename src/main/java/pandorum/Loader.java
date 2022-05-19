package pandorum;

import arc.Events;
import arc.files.Fi;
import arc.graphics.Color;
import arc.graphics.Colors;
import arc.struct.Seq;
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
import mindustry.net.Administration;
import mindustry.net.Packets.Connect;
import mindustry.net.Packets.ConnectPacket;
import pandorum.commands.ClientCommandsHandler;
import pandorum.commands.DiscordCommandsHandler;
import pandorum.commands.ServerCommandsHandler;
import pandorum.commands.client.DespawnCommand;
import pandorum.commands.client.HelpCommand;
import pandorum.commands.client.MapsListCommand;
import pandorum.commands.client.PlayersListCommand;
import pandorum.commands.client.SavesListCommand;
import pandorum.commands.client.*;
import pandorum.commands.discord.AddMapCommand;
import pandorum.commands.discord.IpCommand;
import pandorum.commands.discord.RemoveMapCommand;
import pandorum.commands.discord.StatusCommand;
import pandorum.commands.server.*;
import pandorum.components.Config;
import pandorum.components.Gamemode;
import pandorum.components.Icons;
import pandorum.components.MapParser;
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

public class Loader {

    public static void loadConfig() {
        Fi configFile = dataDirectory.child(configFileName);
        if (configFile.exists()) {
            config = gson.fromJson(configFile.reader(), Config.class);
            Log.info("[Darkdustry] Конфигурация загружена. (@)", configFile.absolutePath());
        } else {
            configFile.writeString(gson.toJson(config = new Config()));
            Log.info("[Darkdustry] Файл конфигурации сгенерирован. (@)", configFile.absolutePath());
        }
    }

    public static void load() {
        MenuHandler.load();
        Icons.load();
        MapParser.load();
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
        Events.on(WaveEvent.class, new OnWave());
        Events.on(WithdrawEvent.class, new OnWithdraw());
        Events.on(WorldLoadEvent.class, new OnWorldLoad());

        Events.run(Trigger.update, new OnTriggerUpdate());
        Events.run("HexedGameOver", new OnGameOver());
        Events.run("CastleGameOver", new OnGameOver());

        Administration.Config.motd.set("off");
        Administration.Config.interactRateWindow.set(3);
        Administration.Config.interactRateLimit.set(50);
        Administration.Config.interactRateKick.set(1000);
        Administration.Config.showConnectMessages.set(false);
        Administration.Config.logging.set(true);
        Administration.Config.strict.set(true);
        Administration.Config.enableVotekick.set(true);

        Version.build = -1;

        Timer.schedule(new Updater(), 0f, 1f);
    }

    public static void registerClientCommands() {
        ClientCommandsHandler handler = new ClientCommandsHandler(clientCommands);

        handler.register("help", "[page]", false, new HelpCommand());
        handler.register("discord", false, new DiscordLinkCommand());
        handler.register("a", "<message...>", true, new AdminChatCommand());
        handler.register("t", "<message...>", false, new TeamChatCommand());
        handler.register("votekick", "<ID/username...>", false, new VoteKickCommand());
        handler.register("vote", "<y/n>", false, new VoteCommand());
        handler.register("sync", false, new SyncCommand());
        handler.register("tr", "<current/list/off/auto/locale>", false, new TranslatorCommand());
        handler.register("stats", "[ID/username...]", false, new StatsCommand());
        handler.register("rank", "[ID/username...]", false, new RankCommand());
        handler.register("players", "[page]", false, new PlayersListCommand());

        handler.register("hub", false, Seq.with(Gamemode.attack, Gamemode.castle, Gamemode.crawler, Gamemode.hexed, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), new HubCommand());

        handler.register("rtv", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), new RtvCommand());
        handler.register("vnw", false, Seq.with(Gamemode.attack, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), new VnwCommand());
        handler.register("surrender", false, Seq.with(Gamemode.pvp), new SurrenderCommand());

        handler.register("history", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), new HistoryCommand());
        handler.register("alert", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), new AlertCommand());
        handler.register("map", false, new MapCommand());
        handler.register("maps", "[page]", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), new MapsListCommand());
        handler.register("saves", "[page]", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), new SavesListCommand());
        handler.register("nominate", "<map/save/load> <name...>", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), new NominateCommand());
        handler.register("voting", "<y/n>", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), new VotingCommand());

        handler.register("spawn", "<unit> [amount] [team]", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), new SpawnCommand());
        handler.register("core", "[small/medium/big] [team]", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), new CoreCommand());
        handler.register("give", "<item> <amount>", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), new GiveCommand());
        handler.register("unit", "<unit> [ID/username...]", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), new UnitCommand());
        handler.register("team", "<team> [ID/username...]", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), new TeamCommand());
        handler.register("spectate", "[ID/username...]", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), new SpectateCommand());

        handler.register("artv", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), new ArtvCommand());
        handler.register("despawn", true, new DespawnCommand());
        handler.register("fill", "<width> <height> <block>", true, Seq.with(Gamemode.attack, Gamemode.sandbox, Gamemode.survival), new FillCommand());

        handler.register("login", false, new LoginCommand());
    }

    public static void registerDiscordCommands() {
        discordCommands = new CommandHandler(config.discordBotPrefix);
        DiscordCommandsHandler handler = new DiscordCommandsHandler(discordCommands);

        handler.register("help", "Список всех команд.", false, new pandorum.commands.discord.HelpCommand());
        handler.register("ip", "IP адрес сервера.", false, new IpCommand());
        handler.register("map", "<название...>", "Получить карту с сервера.", false, new pandorum.commands.discord.MapCommand());
        handler.register("maps", "[страница]", "Список карт сервера.", false, new pandorum.commands.discord.MapsListCommand());
        handler.register("players", "[страница]", "Список игроков сервера.", false, new pandorum.commands.discord.PlayersListCommand());
        handler.register("status", "Состояние сервера.", false, new StatusCommand());

        handler.register("addmap", "Добавить карту на сервер.", true, new AddMapCommand());
        handler.register("removemap", "<название...>", "Удалить карту с сервера.", true, new RemoveMapCommand());
    }

    public static void registerServerCommands() {
        ServerCommandsHandler handler = new ServerCommandsHandler(serverCommands);

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
        handler.register("rmadmins", "Remove all admins on this server.", new RemoveAdmins());
        handler.register("players", "List of all online players.", new pandorum.commands.server.PlayersListCommand());
        handler.register("save", "<save...>", "Save game state to a slot.", new SaveCommand());
        handler.register("load", "<save...>", "Load a save from a slot.", new LoadCommand());

        handler.register("despawn", "Kill all units.", new pandorum.commands.server.DespawnCommand());
        handler.register("restart", "Restart the server.", new RestartCommand());
        handler.register("setrank", "<rank> <ID/username...>", "Set a rank for player.", new SetRankCommand());
    }
}
