package pandorum.commands;

import arc.struct.Seq;
import arc.util.CommandHandler;
import pandorum.commands.client.*;
import pandorum.comp.Config;

public class ClientCommandsLoader {

    public static void registerClientCommands(CommandHandler handler) {
        CommandsHelper.register(handler, "help", "[page]", "List of all commands.", false, HelpCommand::run);
        CommandsHelper.register(handler, "discord", "Get a link to our Discord server.", false, DiscordLinkCommand::run);
        CommandsHelper.register(handler, "a", "<message...>", "Send message to admins.", false, AdminChatCommand::run);
        CommandsHelper.register(handler, "t", "<message...>", "Send message to teammates.", false, TeamChatCommand::run);
        CommandsHelper.register(handler, "votekick", "<player...>", "Start a voting to kick a player.", false, VoteKickCommand::run);
        CommandsHelper.register(handler, "vote", "<y/n>", "Vote to kick a player.", false, VoteCommand::run);
        CommandsHelper.register(handler, "sync", "Re-synchronize world state.", false, SyncCommand::run);
        CommandsHelper.register(handler, "tr", "<off/auto/current/locale>", "Manage chat translator.", false, TranslatorCommand::run);
        CommandsHelper.register(handler, "info", "[player...]", "See some info about a player.", false, InfoCommand::run);
        CommandsHelper.register(handler, "rank", "See information about your rank.", false, RankCommand::run);
        CommandsHelper.register(handler, "players", "[page]", "List of all players.", false, PlayerListCommand::run);

        CommandsHelper.register(handler, "ban", "<uuid...>", "Ban a player.", true, BanCommand::run);
        CommandsHelper.register(handler, "unban", "<uuid...>", "Unban a player.", true, UnbanCommand::run);
        CommandsHelper.register(handler, "despw", "Kill units.", true, DespwCommand::run);
        CommandsHelper.register(handler, "unit", "<unit> [player...]", "Change a unit.", true, UnitCommand::run);
        CommandsHelper.register(handler, "login", "Send confirmation to get admin.", false, LoginCommand::run);

        CommandsHelper.register(handler, "hub", "Connect to HUB.", false, Seq.with(Config.Gamemode.attack, Config.Gamemode.castle, Config.Gamemode.crawler, Config.Gamemode.hexed, Config.Gamemode.pvp, Config.Gamemode.sandbox, Config.Gamemode.siege, Config.Gamemode.survival, Config.Gamemode.tower), HubCommand::run);

        CommandsHelper.register(handler, "rtv", "Vote to skip the map.", false, Seq.with(Config.Gamemode.attack, Config.Gamemode.pvp, Config.Gamemode.sandbox, Config.Gamemode.siege, Config.Gamemode.survival, Config.Gamemode.tower), RTVCommand::run);
        CommandsHelper.register(handler, "vnw", "Vote to skip a wave.", false, Seq.with(Config.Gamemode.attack, Config.Gamemode.sandbox, Config.Gamemode.survival, Config.Gamemode.tower), VNWCommand::run);
        CommandsHelper.register(handler, "surrender", "Vote to surrender.", false, Seq.with(Config.Gamemode.pvp, Config.Gamemode.siege), SurrenderCommand::run);

        CommandsHelper.register(handler, "history", "Enable tile inspector.", false, Seq.with(Config.Gamemode.attack, Config.Gamemode.pvp, Config.Gamemode.sandbox, Config.Gamemode.siege, Config.Gamemode.survival, Config.Gamemode.tower), HistoryCommand::run);
        CommandsHelper.register(handler, "alert", "Enable/disable alerts.", false, Seq.with(Config.Gamemode.attack, Config.Gamemode.pvp, Config.Gamemode.sandbox, Config.Gamemode.siege, Config.Gamemode.survival, Config.Gamemode.tower), AlertCommand::run);
        CommandsHelper.register(handler, "map", "Information about current map.", false, Seq.with(Config.Gamemode.attack, Config.Gamemode.pvp, Config.Gamemode.sandbox, Config.Gamemode.siege, Config.Gamemode.survival, Config.Gamemode.tower), MapCommand::run);
        CommandsHelper.register(handler, "maps", "[page]", "List of all maps.", false, Seq.with(Config.Gamemode.attack, Config.Gamemode.pvp, Config.Gamemode.sandbox, Config.Gamemode.siege, Config.Gamemode.survival, Config.Gamemode.tower), MapsListCommand::run);
        CommandsHelper.register(handler, "saves", "[page]", "List of all saves.", false, Seq.with(Config.Gamemode.attack, Config.Gamemode.pvp, Config.Gamemode.sandbox, Config.Gamemode.siege, Config.Gamemode.survival, Config.Gamemode.tower), SavesListCommand::run);
        CommandsHelper.register(handler, "nominate", "<map/save/load> <name...>", "Vote for load a save/map.", false, Seq.with(Config.Gamemode.attack, Config.Gamemode.pvp, Config.Gamemode.sandbox, Config.Gamemode.siege, Config.Gamemode.survival, Config.Gamemode.tower), NominateCommand::run);
        CommandsHelper.register(handler, "voting", "<y/n>", "Vote «yes» or «no».", false, Seq.with(Config.Gamemode.attack, Config.Gamemode.pvp, Config.Gamemode.sandbox, Config.Gamemode.siege, Config.Gamemode.survival, Config.Gamemode.tower), VotingCommand::run);

        CommandsHelper.register(handler, "spawn", "<unit> [count] [team]", "Spawn units.", true, Seq.with(Config.Gamemode.attack, Config.Gamemode.pvp, Config.Gamemode.sandbox, Config.Gamemode.siege, Config.Gamemode.survival, Config.Gamemode.tower), SpawnCommand::run);
        CommandsHelper.register(handler, "artv", "Force a gameover.", true, Seq.with(Config.Gamemode.attack, Config.Gamemode.pvp, Config.Gamemode.sandbox, Config.Gamemode.siege, Config.Gamemode.survival, Config.Gamemode.tower), ARTVCommand::run);
        CommandsHelper.register(handler, "core", "<small/medium/big>", "Spawn a core.", true, Seq.with(Config.Gamemode.attack, Config.Gamemode.pvp, Config.Gamemode.sandbox, Config.Gamemode.siege, Config.Gamemode.survival, Config.Gamemode.tower), CoreCommand::run);
        CommandsHelper.register(handler, "give", "<item> [count]", "Add items to the core.", true, Seq.with(Config.Gamemode.attack, Config.Gamemode.pvp, Config.Gamemode.sandbox, Config.Gamemode.siege, Config.Gamemode.survival, Config.Gamemode.tower), GiveCommand::run);
        CommandsHelper.register(handler, "team", "<team> [player...]", "Change team.", true, TeamCommand::run);
        CommandsHelper.register(handler, "spectate", "Spectator mode.", true, Seq.with(Config.Gamemode.attack, Config.Gamemode.pvp, Config.Gamemode.sandbox, Config.Gamemode.siege, Config.Gamemode.survival, Config.Gamemode.tower), SpectateCommand::run);
        CommandsHelper.register(handler, "fill", "<width> <height> <block_1> [block_2]", "Fill an area with some floor.",true, Seq.with(Config.Gamemode.sandbox), FillCommand::run);
    }
}
