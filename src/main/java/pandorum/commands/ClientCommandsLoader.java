package pandorum.commands;

import arc.struct.Seq;
import arc.util.CommandHandler;
import pandorum.commands.client.*;
import pandorum.comp.Config.Gamemode;

public class ClientCommandsLoader {

    public static void registerClientCommands(CommandHandler handler) {
        CommandsHelper.registerClient(handler, "help", "[page]", false, HelpCommand::run);
        CommandsHelper.registerClient(handler, "discord", false, DiscordLinkCommand::run);
        CommandsHelper.registerClient(handler, "a", "<message...>", true, AdminChatCommand::run);
        CommandsHelper.registerClient(handler, "t", "<message...>", false, TeamChatCommand::run);
        CommandsHelper.registerClient(handler, "votekick", "<ID/username...>", false, VoteKickCommand::run);
        CommandsHelper.registerClient(handler, "vote", "<y/n>", false, VoteCommand::run);
        CommandsHelper.registerClient(handler, "sync", false, SyncCommand::run);
        CommandsHelper.registerClient(handler, "tr", "<current/list/off/auto/locale>", false, TranslatorCommand::run);
        CommandsHelper.registerClient(handler, "info", "[ID/username...]", false, InfoCommand::run);
        CommandsHelper.registerClient(handler, "rank", "[ID/username...]", false, RankCommand::run);
        CommandsHelper.registerClient(handler, "players", "[page]", false, PlayersListCommand::run);

        CommandsHelper.registerClient(handler, "login", false, LoginCommand::run);

        CommandsHelper.registerClient(handler, "hub", false, Seq.with(Gamemode.attack, Gamemode.castle, Gamemode.crawler, Gamemode.hexed, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), HubCommand::run);

        CommandsHelper.registerClient(handler, "rtv", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), RtvCommand::run);
        CommandsHelper.registerClient(handler, "vnw", false, Seq.with(Gamemode.attack, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), VnwCommand::run);
        CommandsHelper.registerClient(handler, "surrender", false, Seq.with(Gamemode.pvp), SurrenderCommand::run);

        CommandsHelper.registerClient(handler, "history", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), HistoryCommand::run);
        CommandsHelper.registerClient(handler, "alert", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), AlertCommand::run);
        CommandsHelper.registerClient(handler, "map", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), MapCommand::run);
        CommandsHelper.registerClient(handler, "maps", "[page]", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), MapsListCommand::run);
        CommandsHelper.registerClient(handler, "saves", "[page]", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), SavesListCommand::run);
        CommandsHelper.registerClient(handler, "nominate", "<map/save/load> <name...>", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), NominateCommand::run);
        CommandsHelper.registerClient(handler, "voting", "<y/n>", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), VotingCommand::run);

        CommandsHelper.registerClient(handler, "spawn", "<unit> [amount] [team]", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), SpawnCommand::run);
        CommandsHelper.registerClient(handler, "core", "[small/medium/big] [team]", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), CoreCommand::run);
        CommandsHelper.registerClient(handler, "give", "<item> <amount>", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), GiveCommand::run);
        CommandsHelper.registerClient(handler, "unit", "<unit> [ID/username...]", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), UnitCommand::run);
        CommandsHelper.registerClient(handler, "team", "<team> [ID/username...]", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), TeamCommand::run);
        CommandsHelper.registerClient(handler, "spectate", "[ID/username...]", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), SpectateCommand::run);

        CommandsHelper.registerClient(handler, "artv", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), ArtvCommand::run);
        CommandsHelper.registerClient(handler, "despw", true, DespwCommand::run);
        CommandsHelper.registerClient(handler, "fill", "<width> <height> <block>", true, Seq.with(Gamemode.sandbox), FillCommand::run);
    }
}
