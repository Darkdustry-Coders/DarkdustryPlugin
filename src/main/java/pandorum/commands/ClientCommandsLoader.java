package pandorum.commands;

import arc.struct.Seq;
import arc.util.CommandHandler;
import pandorum.commands.client.*;
import pandorum.comp.Config.Gamemode;

public class ClientCommandsLoader {

    public static void registerClientCommands(CommandHandler handler) {
        CommandsHelper.register(handler, "help", "[page]", "commands.help.description", false, HelpCommand::run);
        CommandsHelper.register(handler, "discord", "commands.discord.description", false, DiscordLinkCommand::run);
        CommandsHelper.register(handler, "a", "<message...>", "commands.a.description", true, AdminChatCommand::run);
        CommandsHelper.register(handler, "t", "<message...>", "commands.t.description", false, TeamChatCommand::run);
        CommandsHelper.register(handler, "votekick", "<ID/username...>", "commands.votekick.description", false, VoteKickCommand::run);
        CommandsHelper.register(handler, "vote", "<y/n>", "commands.vote.description", false, VoteCommand::run);
        CommandsHelper.register(handler, "sync", "commands.sync.description", false, SyncCommand::run);
        CommandsHelper.register(handler, "tr", "<current/list/off/auto/locale>", "commands.tr.description", false, TranslatorCommand::run);
        CommandsHelper.register(handler, "info", "[ID/username...]", "commands.info.description", false, InfoCommand::run);
        CommandsHelper.register(handler, "rank", "commands.rank.description", false, RankCommand::run);
        CommandsHelper.register(handler, "players", "[page]", "commands.players.description", false, PlayersListCommand::run);

        CommandsHelper.register(handler, "despw", "commands.despw.description", true, DespwCommand::run);
        CommandsHelper.register(handler, "unit", "<unit> [ID/username...]", "commands.unit.description", true, UnitCommand::run);
        CommandsHelper.register(handler, "login", "commands.login.description", false, LoginCommand::run);

        CommandsHelper.register(handler, "hub", "commands.hub.description", false, Seq.with(Gamemode.attack, Gamemode.castle, Gamemode.crawler, Gamemode.hexed, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), HubCommand::run);

        CommandsHelper.register(handler, "rtv", "commands.rtv.description", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), RTVCommand::run);
        CommandsHelper.register(handler, "vnw", "commands.vnw.description", false, Seq.with(Gamemode.attack, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), VNWCommand::run);
        CommandsHelper.register(handler, "surrender", "commands.surrender.description", false, Seq.with(Gamemode.pvp), SurrenderCommand::run);

        CommandsHelper.register(handler, "history", "commands.history.description", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), HistoryCommand::run);
        CommandsHelper.register(handler, "alert", "commands.alert.description", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), AlertCommand::run);
        CommandsHelper.register(handler, "map", "commands.map.description", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), MapCommand::run);
        CommandsHelper.register(handler, "maps", "[page]", "commands.maps.description", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), MapsListCommand::run);
        CommandsHelper.register(handler, "saves", "[page]", "commands.saves.description", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), SavesListCommand::run);
        CommandsHelper.register(handler, "nominate", "<map/save/load> <name...>", "commands.nominate.description", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), NominateCommand::run);
        CommandsHelper.register(handler, "voting", "<y/n>", "commands.voting.description", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), VotingCommand::run);

        CommandsHelper.register(handler, "spawn", "<unit> [amount] [team]", "commands.spawn.description", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), SpawnCommand::run);
        CommandsHelper.register(handler, "artv", "commands.artv.description", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), ARTVCommand::run);
        CommandsHelper.register(handler, "core", "[small/medium/big] [team]", "commands.core.description", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), CoreCommand::run);
        CommandsHelper.register(handler, "give", "<item> <amount>", "commands.give.description", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), GiveCommand::run);
        CommandsHelper.register(handler, "team", "<team> [ID/username...]", "commands.team.description", true, TeamCommand::run);
        CommandsHelper.register(handler, "spectate", "[ID/username...]", "commands.spectate.description", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), SpectateCommand::run);
        CommandsHelper.register(handler, "fill", "<width> <height> <block>", "commands.fill.description", true, Seq.with(Gamemode.sandbox), FillCommand::run);
    }
}
