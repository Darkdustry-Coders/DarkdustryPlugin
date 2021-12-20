package pandorum.commands;

import arc.struct.Seq;
import arc.util.CommandHandler;
import pandorum.commands.client.*;
import pandorum.comp.Config;

public class ClientCommandsLoader {

    public static void registerClientCommands(CommandHandler handler) {
        CommandsHelper.register(handler, "help", "[page]", "commands.help.description", false, HelpCommand::run);
        CommandsHelper.register(handler, "discord", "commands.discord.description", false, DiscordLinkCommand::run);
        CommandsHelper.register(handler, "a", "<message...>", "commands.a.description", true, AdminChatCommand::run);
        CommandsHelper.register(handler, "t", "<message...>", "commands.t.description", false, TeamChatCommand::run);
        CommandsHelper.register(handler, "votekick", "<player...>", "commands.votekick.description", false, VoteKickCommand::run);
        CommandsHelper.register(handler, "vote", "<y/n>", "commands.vote.description", false, VoteCommand::run);
        CommandsHelper.register(handler, "sync", "commands.sync.description", false, SyncCommand::run);
        CommandsHelper.register(handler, "tr", "<off/auto/current/locale>", "commands.tr.description", false, TranslatorCommand::run);
        CommandsHelper.register(handler, "info", "[player...]", "commands.info.description", false, InfoCommand::run);
        CommandsHelper.register(handler, "rank", "commands.rank.description", false, RankCommand::run);
        CommandsHelper.register(handler, "players", "[page]", "commands.players.description", false, PlayerListCommand::run);

        CommandsHelper.register(handler, "ban", "<uuid...>", "commands.ban.description", true, BanCommand::run);
        CommandsHelper.register(handler, "unban", "<uuid...>", "commands.unban.description", true, UnbanCommand::run);
        CommandsHelper.register(handler, "despw", "commands.despw.description", true, DespwCommand::run);
        CommandsHelper.register(handler, "unit", "<unit> [player...]", "commands.unit.description", true, UnitCommand::run);
        CommandsHelper.register(handler, "login", "commands.login.description", false, LoginCommand::run);

        CommandsHelper.register(handler, "hub", "commands.hub.description", false, Seq.with(Config.Gamemode.attack, Config.Gamemode.castle, Config.Gamemode.crawler, Config.Gamemode.hexed, Config.Gamemode.pvp, Config.Gamemode.sandbox, Config.Gamemode.siege, Config.Gamemode.survival, Config.Gamemode.tower), HubCommand::run);

        CommandsHelper.register(handler, "rtv", "commands.rtv.description", false, Seq.with(Config.Gamemode.attack, Config.Gamemode.pvp, Config.Gamemode.sandbox, Config.Gamemode.siege, Config.Gamemode.survival, Config.Gamemode.tower), RTVCommand::run);
        CommandsHelper.register(handler, "vnw", "commands.vnw.description", false, Seq.with(Config.Gamemode.attack, Config.Gamemode.sandbox, Config.Gamemode.survival, Config.Gamemode.tower), VNWCommand::run);
        CommandsHelper.register(handler, "surrender", "commands.surrender.description", false, Seq.with(Config.Gamemode.pvp, Config.Gamemode.siege), SurrenderCommand::run);

        CommandsHelper.register(handler, "history", "commands.history.description", false, Seq.with(Config.Gamemode.attack, Config.Gamemode.pvp, Config.Gamemode.sandbox, Config.Gamemode.siege, Config.Gamemode.survival, Config.Gamemode.tower), HistoryCommand::run);
        CommandsHelper.register(handler, "alert", "commands.alert.description", false, Seq.with(Config.Gamemode.attack, Config.Gamemode.pvp, Config.Gamemode.sandbox, Config.Gamemode.siege, Config.Gamemode.survival, Config.Gamemode.tower), AlertCommand::run);
        CommandsHelper.register(handler, "map", "commands.map.description", false, Seq.with(Config.Gamemode.attack, Config.Gamemode.pvp, Config.Gamemode.sandbox, Config.Gamemode.siege, Config.Gamemode.survival, Config.Gamemode.tower), MapCommand::run);
        CommandsHelper.register(handler, "maps", "[page]", "commands.maps.description", false, Seq.with(Config.Gamemode.attack, Config.Gamemode.pvp, Config.Gamemode.sandbox, Config.Gamemode.siege, Config.Gamemode.survival, Config.Gamemode.tower), MapsListCommand::run);
        CommandsHelper.register(handler, "saves", "[page]", "commands.saves.description", false, Seq.with(Config.Gamemode.attack, Config.Gamemode.pvp, Config.Gamemode.sandbox, Config.Gamemode.siege, Config.Gamemode.survival, Config.Gamemode.tower), SavesListCommand::run);
        CommandsHelper.register(handler, "nominate", "<map/save/load> <name...>", "commands.nominate.description", false, Seq.with(Config.Gamemode.attack, Config.Gamemode.pvp, Config.Gamemode.sandbox, Config.Gamemode.siege, Config.Gamemode.survival, Config.Gamemode.tower), NominateCommand::run);
        CommandsHelper.register(handler, "voting", "<y/n>", "commands.voting.description", false, Seq.with(Config.Gamemode.attack, Config.Gamemode.pvp, Config.Gamemode.sandbox, Config.Gamemode.siege, Config.Gamemode.survival, Config.Gamemode.tower), VotingCommand::run);

        CommandsHelper.register(handler, "spawn", "<unit> [count] [team]", "commands.spawn.description", true, Seq.with(Config.Gamemode.attack, Config.Gamemode.pvp, Config.Gamemode.sandbox, Config.Gamemode.siege, Config.Gamemode.survival, Config.Gamemode.tower), SpawnCommand::run);
        CommandsHelper.register(handler, "artv", "commands.artv.description", true, Seq.with(Config.Gamemode.attack, Config.Gamemode.pvp, Config.Gamemode.sandbox, Config.Gamemode.siege, Config.Gamemode.survival, Config.Gamemode.tower), ARTVCommand::run);
        CommandsHelper.register(handler, "core", "<small/medium/big>", "commands.core.description", true, Seq.with(Config.Gamemode.attack, Config.Gamemode.pvp, Config.Gamemode.sandbox, Config.Gamemode.siege, Config.Gamemode.survival, Config.Gamemode.tower), CoreCommand::run);
        CommandsHelper.register(handler, "give", "<item> [count]", "commands.give.description", true, Seq.with(Config.Gamemode.attack, Config.Gamemode.pvp, Config.Gamemode.sandbox, Config.Gamemode.siege, Config.Gamemode.survival, Config.Gamemode.tower), GiveCommand::run);
        CommandsHelper.register(handler, "team", "<team> [player...]", "commands.team.description", true, TeamCommand::run);
        CommandsHelper.register(handler, "spectate", "commands.spectate.description", true, Seq.with(Config.Gamemode.attack, Config.Gamemode.pvp, Config.Gamemode.sandbox, Config.Gamemode.siege, Config.Gamemode.survival, Config.Gamemode.tower), SpectateCommand::run);
        CommandsHelper.register(handler, "fill", "<width> <height> <block_1> [block_2]", "commands.fill.description", true, Seq.with(Config.Gamemode.sandbox), FillCommand::run);
    }
}
