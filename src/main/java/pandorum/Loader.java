package pandorum;

import arc.Events;
import arc.files.Fi;
import arc.graphics.Color;
import arc.graphics.Colors;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Timer;
import mindustry.core.Version;
import mindustry.game.EventType.*;
import mindustry.gen.Call;
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
import pandorum.components.*;
import pandorum.data.Database;
import pandorum.discord.Bot;
import pandorum.features.Alerts;
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

        Translator.loadLanguages();

        MenuHandler.load();
        Alerts.load();
        Ranks.load();

        Database.connect();
        Bot.connect();

        Version.build = -1;

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
        net.handleServer(Connect.class, new ConnectHandler());
        net.handleServer(ConnectPacket.class, new ConnectPacketHandler());

        netServer.admins.addActionFilter(new ActionManager());
        netServer.admins.addChatFilter(new ChatManager());
        netServer.invalidHandler = new InvalidCommandResponseHandler();

        netServer.addPacketHandler("WhoIsUsingSS", (player, content) -> {
            if (player.name.contains("[#0096FF]xzxADIxzx") && content.equals(schematicBaseStart))
                Call.clientPacketReliable("AreYouUsingSS", ""); // remove it later
        });

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

        Timer.schedule(new Updater(), 0f, 1f);
    }

    public static void registerClientCommands(CommandHandler handler) {
        handler.register("help", Bundle.get("commands.help.params"), Bundle.get("commands.help.description"), new HelpCommand());
        handler.register("discord", Bundle.get("commands.discord.description"), new DiscordLinkCommand());
        handler.register("a", Bundle.get("commands.a.params"), Bundle.get("commands.a.description"), new AdminChatCommand());
        handler.register("t", Bundle.get("commands.t.params"), Bundle.get("commands.t.description"), new TeamChatCommand());
        handler.register("votekick", Bundle.get("commands.votekick.params"), Bundle.get("commands.votekick.description"), new VoteKickCommand());
        handler.register("vote", Bundle.get("commands.vote.params"), Bundle.get("commands.vote.description"), new VoteCommand());
        handler.register("sync", Bundle.get("commands.sync.description"), new SyncCommand());
        handler.register("tr", Bundle.get("commands.tr.params"), Bundle.get("commands.tr.description"), new TranslatorCommand());
        handler.register("stats", Bundle.get("commands.stats.params"), Bundle.get("commands.stats.description"), new StatsCommand());
        handler.register("rank", Bundle.get("commands.rank.params"), Bundle.get("commands.rank.description"), new RankCommand());
        handler.register("players", Bundle.get("commands.players.params"), Bundle.get("commands.players.description"), new PlayersListCommand());
        handler.register("login", Bundle.get("commands.login.description"), new LoginCommand());

        if (config.mode != hub) {
            handler.register("hub", Bundle.get("commands.hub.description"), new HubCommand());
        }

        if (defaultModes.contains(config.mode)) {
            handler.register("rtv", Bundle.get("commands.rtv.description"), new RtvCommand());
            handler.register("vnw", Bundle.get("commands.vnw.description"), new VnwCommand());

            if (config.mode == pvp) {
                handler.register("surrender", Bundle.get("commands.surrender.description"), new SurrenderCommand());
            }

            handler.register("history", Bundle.get("commands.history.description"), new HistoryCommand());
            handler.register("alerts", Bundle.get("commands.alerts.description"), new AlertsCommand());

            handler.register("maps", Bundle.get("commands.maps.params"), Bundle.get("commands.maps.description"), new MapsListCommand());
            handler.register("saves", Bundle.get("commands.saves.params"), Bundle.get("commands.saves.description"), new SavesListCommand());
            handler.register("nominate", Bundle.get("commands.nominate.params"), Bundle.get("commands.nominate.description"), new NominateCommand());
            handler.register("voting", Bundle.get("commands.voting.params"), Bundle.get("commands.voting.description"), new VotingCommand());

            handler.register("artv", Bundle.get("commands.artv.description"), new ArtvCommand());
            handler.register("despawn", Bundle.get("commands.despawn.description"), new DespawnCommand());
            handler.register("core", Bundle.get("commands.core.params"), Bundle.get("commands.core.description"), new CoreCommand());
            handler.register("give", Bundle.get("commands.give.params"), Bundle.get("commands.give.description"), new GiveCommand());
            handler.register("spawn", Bundle.get("commands.spawn.params"), Bundle.get("commands.spawn.description"), new SpawnCommand());
            handler.register("effect", Bundle.get("commands.effect.params"), Bundle.get("commands.effect.description"), new EffectCommand());
            handler.register("team", Bundle.get("commands.team.params"), Bundle.get("commands.team.description"), new TeamCommand());
            handler.register("unit", Bundle.get("commands.unit.params"), Bundle.get("commands.unit.description"), new UnitCommand());
            handler.register("spectate", Bundle.get("commands.spectate.params"), Bundle.get("commands.spectate.description"), new SpectateCommand());
            handler.register("tp", Bundle.get("commands.tp.params"), Bundle.get("commands.tp.description"), new TeleportCommand());
            handler.register("fill", Bundle.get("commands.fill.params"), Bundle.get("commands.fill.description"), new FillCommand());
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
        handler.removeCommand("mod");
        handler.removeCommand("mods");
        handler.removeCommand("fillitems");
        handler.removeCommand("pause");
        handler.removeCommand("shuffle");
        handler.removeCommand("runwave");

        handler.register("help", "Список всех команд.", new pandorum.commands.server.HelpCommand());
        handler.register("version", "Информация о версии сервера.", new VersionCommand());
        handler.register("exit", "Выключить сервер.", new ExitCommand());
        handler.register("stop", "Остановить сервер.", new StopCommand());
        handler.register("host", "[карта] [режим]", "Запустить сервер на выбранной карте.", new HostCommand());
        handler.register("reloadmaps", "Перезагрузить список карт.", new ReloadMapsCommand());
        handler.register("maps", "Список всех карт сервера.", new pandorum.commands.server.MapsListCommand());
        handler.register("saves", "Список всех сохранений сервера.", new pandorum.commands.server.SavesListCommand());
        handler.register("status", "Состояние сервера.", new pandorum.commands.server.StatusCommand());
        handler.register("say", "<сообщение...>", "Отправить сообщение всем игрокам.", new SayCommand());
        handler.register("rules", "[remove/add] [название] [значение...]", "Изменить глобальные правила сервера.", new RulesCommand());
        handler.register("playerlimit", "[off/число]", "Настроить лимит игроков на сервере.", new PlayerLimitCommand());
        handler.register("config", "[название] [значение...]", "Изменить конфигурацию сервера.", new ConfigCommand());
        handler.register("whitelist", "[add/remove] [uuid...]", "Добавить или удалить игрока из белого списка.", new WhiteListCommand());
        handler.register("subnet-ban", "[add/remove] [ip...]", "Добавить или удалить IP адрес из списка забаненных сабнетов.", new SubnetBanCommand());
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
