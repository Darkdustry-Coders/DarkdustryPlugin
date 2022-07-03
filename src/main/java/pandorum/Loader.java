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
import pandorum.commands.discord.BanCommand;
import pandorum.commands.discord.GameOverCommand;
import pandorum.commands.discord.KickCommand;
import pandorum.commands.discord.StatusCommand;
import pandorum.commands.discord.*;
import pandorum.commands.server.*;
import pandorum.components.Bundle;
import pandorum.components.Icons;
import pandorum.components.MapParser;
import pandorum.components.PluginConfig;
import pandorum.data.Database;
import pandorum.discord.Bot;
import pandorum.features.Ranks;
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

        Database.connect();
        Bot.connect();

        dangerousBuildBlocks.put(Blocks.incinerator, () -> !state.rules.infiniteResources);
        dangerousBuildBlocks.put(Blocks.thoriumReactor, () -> state.rules.reactorExplosions);

        dangerousDepositBlocks.put(Blocks.combustionGenerator, Items.blastCompound);
        dangerousDepositBlocks.put(Blocks.steamGenerator, Items.blastCompound);
        dangerousDepositBlocks.put(Blocks.thoriumReactor, Items.thorium);

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
        handler.register("players", "[страница]", "Список всех игроков на сервере.", new pandorum.commands.discord.PlayersListCommand());
        handler.register("status", "Состояние сервера.", new StatusCommand());

        handler.register("kick", "<ID/никнейм...>", "Выгнать игрока с сервера.", new KickCommand());
        handler.register("ban", "<ID/никнейм...>", "Забанить игрока на сервере.", new BanCommand());

        if (config.mode != hexed) {
            handler.register("map", "<название...>", "Получить карту с сервера.", new pandorum.commands.discord.MapCommand());
            handler.register("maps", "[страница]", "Список всех карт сервера.", new pandorum.commands.discord.MapsListCommand());

            handler.register("addmap", "Добавить карту на сервер.", new AddMapCommand());
            handler.register("removemap", "<название...>", "Удалить карту с сервера.", new RemoveMapCommand());
        }

        if (defaultModes.contains(config.mode)) {
            handler.register("gameover", "Принудительно завершить игру.", new GameOverCommand());
        }
    }

    public static void registerServerCommands(CommandHandler handler) {
        handler.register("help", "Список всех команд.", new pandorum.commands.server.HelpCommand());
        handler.register("version", "Информация о версии сервера.", new VersionCommand());
        handler.register("exit", "Выключить сервер.", new ExitCommand());
        handler.register("stop", "Остановить сервер.", new StopCommand());
        handler.register("host", "[карта] [режим]", "Запустить сервер на выбранной карте.", new HostCommand());
        handler.register("maps", "Список всех карт сервера.", new pandorum.commands.server.MapsListCommand());
        handler.register("saves", "Список всех сохранений сервера.", new pandorum.commands.server.SavesListCommand());
        handler.register("status", "Состояние сервера.", new pandorum.commands.server.StatusCommand());
        handler.register("say", "<сообщение...>", "Отправить сообщение всем игрокам.", new SayCommand());
        handler.register("rules", "[remove/add] [название] [значение...]", "Изменить глобальные правила сервера.", new RulesCommand());
        handler.register("config", "[название] [значение...]", "Изменить конфигурацию сервера.", new ConfigCommand());
        handler.register("whitelist", "[add/remove] [uuid...]", "Добавить или удалить игрока из белого списка.", new WhiteListCommand());
        handler.register("nextmap", "<карта...>", "Задать следующую карту.", new NextMapCommand());
        handler.register("kick", "<ID/никнейм...>", "Выгнать игрока с сервера.", new pandorum.commands.server.KickCommand());
        handler.register("ban", "<никнейм/ip/uuid...>", "Забанить игрока на сервере.", new pandorum.commands.server.BanCommand());
        handler.register("bans", "[clear]", "Список всех забаненных игроков.", new BansListCommand());
        handler.register("unban", "<ip/uuid...>", "Разбанить игрока на сервере.", new UnbanCommand());
        handler.register("pardon", "<ip/uuid...>", "Снять кик с игрока.", new PardonCommand());
        handler.register("admin", "<add/remove> <никнейм/uuid...>", "Выдать права админа игроку.", new AdminCommand());
        handler.register("admins", "[clear]", "Список всех админов.", new AdminsListCommand());
        handler.register("players", "Список всех игроков на сервере.", new pandorum.commands.server.PlayersListCommand());
        handler.register("save", "<файл...>", "Сохранить карту в выбранный файл.", new SaveCommand());
        handler.register("load", "<файл...>", "Загрузить сохранение из файла.", new LoadCommand());
        handler.register("gameover", "Принудительно завершить игру.", new pandorum.commands.server.GameOverCommand());
        handler.register("info", "<никнейм/ip/uuid...>", "Поиск информации об игроке.", new InfoCommand());
        handler.register("search", "<никнейм...>", "Поиск игроков, которые использовали часть данного никнейма.", new SearchCommand());

        handler.register("despawn", "Убить всех юнитов.", new pandorum.commands.server.DespawnCommand());
        handler.register("restart", "Перезапустить сервер.", new RestartCommand());
        handler.register("setrank", "<ранг> <ID/никнейм...>", "Изменить ранг игрока.", new SetRankCommand());
    }
}
