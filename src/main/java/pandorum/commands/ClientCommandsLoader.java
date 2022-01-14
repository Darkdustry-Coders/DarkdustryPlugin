package pandorum.commands;

import arc.struct.Seq;
import arc.util.CommandHandler;
import pandorum.commands.client.*;
import pandorum.comp.Config.Gamemode;

public class ClientCommandsLoader {

    public static void registerClientCommands(CommandHandler handler) {
        CommandsHelper.register(handler, "help", "[page]", false, HelpCommand::run);
        CommandsHelper.register(handler, "discord", false, DiscordLinkCommand::run);
        CommandsHelper.register(handler, "a", "<message...>", true, AdminChatCommand::run);
        CommandsHelper.register(handler, "t", "<message...>", false, TeamChatCommand::run);
        CommandsHelper.register(handler, "votekick", "<ID/username...>", false, VoteKickCommand::run);
        CommandsHelper.register(handler, "vote", "<y/n>", false, VoteCommand::run);
        CommandsHelper.register(handler, "sync", false, SyncCommand::run);
        CommandsHelper.register(handler, "tr", "<current/list/off/auto/locale>", false, TranslatorCommand::run);
        CommandsHelper.register(handler, "info", "[ID/username...]", false, InfoCommand::run);
        CommandsHelper.register(handler, "rank", "[ID/username...]", false, RankCommand::run);
        CommandsHelper.register(handler, "players", "[page]", false, PlayersListCommand::run);

        CommandsHelper.register(handler, "login", false, LoginCommand::run);

        CommandsHelper.register(handler, "hub", false, Seq.with(Gamemode.attack, Gamemode.castle, Gamemode.crawler, Gamemode.hexed, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), HubCommand::run);

        CommandsHelper.register(handler, "rtv", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), RTVCommand::run);
        CommandsHelper.register(handler, "vnw", false, Seq.with(Gamemode.attack, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), VNWCommand::run);
        CommandsHelper.register(handler, "surrender", false, Seq.with(Gamemode.pvp), SurrenderCommand::run);

        CommandsHelper.register(handler, "history", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), HistoryCommand::run);
        CommandsHelper.register(handler, "alert", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), AlertCommand::run);
        CommandsHelper.register(handler, "map", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), MapCommand::run);
        CommandsHelper.register(handler, "maps", "[page]", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), MapsListCommand::run);
        CommandsHelper.register(handler, "saves", "[page]", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), SavesListCommand::run);
        CommandsHelper.register(handler, "nominate", "<map/save/load> <name...>", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), NominateCommand::run);
        CommandsHelper.register(handler, "voting", "<y/n>", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), VotingCommand::run);

        CommandsHelper.register(handler, "spawn", "<unit> [amount] [team]", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), SpawnCommand::run);
        CommandsHelper.register(handler, "core", "[small/medium/big] [team]", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), CoreCommand::run);
        CommandsHelper.register(handler, "give", "<item> <amount>", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), GiveCommand::run);
        CommandsHelper.register(handler, "unit", "<unit> [ID/username...]", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), UnitCommand::run);
        CommandsHelper.register(handler, "team", "<team> [ID/username...]", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), TeamCommand::run);
        CommandsHelper.register(handler, "spectate", "[ID/username...]", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), SpectateCommand::run);

        CommandsHelper.register(handler, "artv", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), ARTVCommand::run);
        CommandsHelper.register(handler, "despw", true, DespwCommand::run);
        CommandsHelper.register(handler, "fill", "<width> <height> <block>", true, Seq.with(Gamemode.sandbox), FillCommand::run);
    }
}
