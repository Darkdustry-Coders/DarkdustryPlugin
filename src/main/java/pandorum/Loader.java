package pandorum;

import arc.Events;
import arc.files.Fi;
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
import mindustry.net.Administration;
import mindustry.net.Packets.*;
import pandorum.commands.ClientCommandsHandler;
import pandorum.commands.DiscordCommandsHandler;
import pandorum.commands.ServerCommandsHandler;
import pandorum.commands.client.*;
import pandorum.commands.client.HelpCommand;
import pandorum.commands.client.MapsListCommand;
import pandorum.commands.client.PlayersListCommand;
import pandorum.commands.client.SavesListCommand;
import pandorum.commands.discord.AddMapCommand;
import pandorum.commands.discord.IpCommand;
import pandorum.commands.discord.RemoveMapCommand;
import pandorum.commands.discord.StatusCommand;
import pandorum.commands.server.*;
import pandorum.components.*;
import pandorum.discord.Bot;
import pandorum.events.Updater;
import pandorum.events.filters.ActionManager;
import pandorum.events.filters.ChatManager;
import pandorum.events.handlers.ConnectHandler;
import pandorum.events.handlers.ConnectPacketHandler;
import pandorum.events.handlers.InvalidCommandResponse;
import pandorum.events.handlers.MenuHandler;
import pandorum.events.listeners.*;

import static mindustry.Vars.*;
import static mindustry.Vars.state;
import static pandorum.PluginVars.*;

public class Loader {

    public static void init() {
        writeBuffer = Reflect.get(NetServer.class, netServer, "writeBuffer");
        outputBuffer = Reflect.get(NetServer.class, netServer, "outputBuffer");

        net.handleServer(Connect.class, new ConnectHandler());
        net.handleServer(ConnectPacket.class, new ConnectPacketHandler());

        netServer.admins.addActionFilter(new ActionManager());
        netServer.admins.addChatFilter(new ChatManager());
        netServer.invalidHandler = new InvalidCommandResponse();

        Events.on(AdminRequestEvent.class, new AdminRequestListener());
        Events.on(BlockBuildEndEvent.class, new BlockBuildEndListener());
        Events.on(BuildSelectEvent.class, new BuildSelectListener());
        Events.on(ConfigEvent.class, new ConfigListener());
        Events.on(DepositEvent.class, new DepositListener());
        Events.on(PlayerJoin.class, new PlayerJoinListener());
        Events.on(PlayerLeave.class, new PlayerLeaveListener());
        Events.on(TapEvent.class, new TapListener());
        Events.on(WithdrawEvent.class, new WithdrawListener());

        Events.run(GameOverEvent.class, new GameOverListener());
        Events.run(ServerLoadEvent.class, new ServerLoadListener());
        Events.run(WaveEvent.class, new WaveListener());
        Events.run(WorldLoadEvent.class, new WorldLoadListener());

        Events.run(Trigger.update, new TriggerUpdateListener());
        Events.run("HexedGameOver", new GameOverListener());
        Events.run("CastleGameOver", new GameOverListener());

        Administration.Config.motd.set("off");
        Administration.Config.interactRateWindow.set(3);
        Administration.Config.interactRateLimit.set(50);
        Administration.Config.interactRateKick.set(1000);
        Administration.Config.showConnectMessages.set(false);
        Administration.Config.logging.set(true);
        Administration.Config.strict.set(true);
        Administration.Config.enableVotekick.set(true);

        Version.build = -1;

        Timer.schedule(Updater::update, 0f, 1f);
    }

    public static void load() {
        MenuHandler.load();
        Icons.load();
        MapParser.load();
        Ranks.load();

        Translator.loadLanguages();

        Bot.run();

        dangerousBuildBlocks.put(Blocks.incinerator, () -> !state.rules.infiniteResources);
        dangerousBuildBlocks.put(Blocks.thoriumReactor, () -> state.rules.reactorExplosions);
        dangerousDepositBlocks.putAll(Blocks.combustionGenerator, Items.blastCompound, Blocks.steamGenerator, Items.blastCompound, Blocks.thoriumReactor, Items.thorium);
    }

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

    public static void registerClientCommands() {
        ClientCommandsHandler handler = new ClientCommandsHandler(clientCommands);

        handler.register("help", "[page]", false, HelpCommand::run);
        handler.register("discord", false, DiscordLinkCommand::run);
        handler.register("a", "<message...>", true, AdminChatCommand::run);
        handler.register("t", "<message...>", false, TeamChatCommand::run);
        handler.register("votekick", "<ID/username...>", false, VoteKickCommand::run);
        handler.register("vote", "<y/n>", false, VoteCommand::run);
        handler.register("sync", false, SyncCommand::run);
        handler.register("tr", "<current/list/off/auto/locale>", false, TranslatorCommand::run);
        handler.register("stats", "[ID/username...]", false, StatsCommand::run);
        handler.register("rank", "[ID/username...]", false, RankCommand::run);
        handler.register("players", "[page]", false, PlayersListCommand::run);

        handler.register("hub", false, Seq.with(Gamemode.attack, Gamemode.castle, Gamemode.crawler, Gamemode.hexed, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), HubCommand::run);

        handler.register("rtv", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), RtvCommand::run);
        handler.register("vnw", false, Seq.with(Gamemode.attack, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), VnwCommand::run);
        handler.register("surrender", false, Seq.with(Gamemode.pvp), SurrenderCommand::run);

        handler.register("history", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), HistoryCommand::run);
        handler.register("alert", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), AlertCommand::run);
        handler.register("map", false, MapCommand::run);
        handler.register("maps", "[page]", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), MapsListCommand::run);
        handler.register("saves", "[page]", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), SavesListCommand::run);
        handler.register("nominate", "<map/save/load> <name...>", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), NominateCommand::run);
        handler.register("voting", "<y/n>", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), VotingCommand::run);

        handler.register("spawn", "<unit> [amount] [team]", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), SpawnCommand::run);
        handler.register("core", "[small/medium/big] [team]", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), CoreCommand::run);
        handler.register("give", "<item> <amount>", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), GiveCommand::run);
        handler.register("unit", "<unit> [ID/username...]", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), UnitCommand::run);
        handler.register("team", "<team> [ID/username...]", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), TeamCommand::run);
        handler.register("spectate", "[ID/username...]", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), SpectateCommand::run);

        handler.register("artv", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), ArtvCommand::run);
        handler.register("despw", true, DespwCommand::run);
        handler.register("fill", "<width> <height> <block>", true, Seq.with(Gamemode.attack, Gamemode.sandbox, Gamemode.survival), FillCommand::run);

        handler.register("login", false, LoginCommand::run);
    }

    public static void registerDiscordCommands() {
        discordHandler = new CommandHandler(config.prefix);
        DiscordCommandsHandler handler = new DiscordCommandsHandler(discordCommands);

        handler.register("help", "Список всех команд.", false, pandorum.commands.discord.HelpCommand::run);
        handler.register("ip", "IP адрес сервера.", false, IpCommand::run);
        handler.register("map", "<название...>", "Получить карту с сервера.", false, pandorum.commands.discord.MapCommand::run);
        handler.register("maps", "[страница]", "Список карт сервера.", false, pandorum.commands.discord.MapsListCommand::run);
        handler.register("players", "[страница]", "Список игроков сервера.", false, pandorum.commands.discord.PlayersListCommand::run);
        handler.register("status", "Состояние сервера.", false, StatusCommand::run);

        handler.register("addmap", "Добавить карту на сервер.", true, AddMapCommand::run);
        handler.register("removemap", "<название...>", "Удалить карту с сервера.", true, RemoveMapCommand::run);
    }

    public static void registerServerCommands() {
        ServerCommandsHandler handler = new ServerCommandsHandler(serverCommands);

        handler.register("help", "List of all commands.", pandorum.commands.server.HelpCommand::run);
        handler.register("exit", "Shut down the server.", ExitCommand::run);
        handler.register("host", "[map] [mode]", "Open the server. Will default to a random map and survival gamemode if not specified.", HostCommand::run);
        handler.register("maps", "List of all available maps.", pandorum.commands.server.MapsListCommand::run);
        handler.register("saves", "List of all available saves.", pandorum.commands.server.SavesListCommand::run);
        handler.register("status", "Display server status.", pandorum.commands.server.StatusCommand::run);
        handler.register("say", "<message...>", "Send a message to all players.", SayCommand::run);
        handler.register("rules", "[remove/add] [name] [value...]", "List, add or remove global rules.", RulesCommand::run);
        handler.register("config", "[name] [value...]", "Configure server settings.", ConfigCommand::run);
        handler.register("nextmap", "<map...>", "Set the next map to be played after a gameover. Overrides shuffling.", NextMapCommand::run);
        handler.register("kick", "<ID/username...>", "Kick a player from the server.", KickCommand::run);
        handler.register("ban", "<type> <uuid/username/ip...>", "Ban a player by UUID, name or IP.", BanCommand::run);
        handler.register("bans", "List of all banned IPs and UUIDs.", BansListCommand::run);
        handler.register("unban", "<uuid/all/ip...>", "Unban a player by UUID or IP.", UnbanCommand::run);
        handler.register("pardon", "<uuid/ip...>", "Pardon a kicked player.", PardonCommand::run);
        handler.register("admin", "<add/remove> <uuid/username...>", "Make an online user admin.", AdminCommand::run);
        handler.register("admins", "List of all admins.", AdminsListCommand::run);
        handler.register("players", "List of all online players.", pandorum.commands.server.PlayersListCommand::run);
        handler.register("save", "<save...>", "Save game state to a slot.", SaveCommand::run);
        handler.register("load", "<save...>", "Load a save from a slot.", LoadCommand::run);

        handler.register("despawn", "Kill all units.", DespawnCommand::run);
        handler.register("restart", "Restart the server.", RestartCommand::run);
        handler.register("setrank", "<rank> <ID/username...>", "Set a rank for player.", SetRankCommand::run);
    }
}
