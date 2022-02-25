package pandorum.commands;

import arc.Core;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.CommandHandler.Command;
import arc.util.Strings;
import mindustry.gen.Player;
import pandorum.commands.client.*;
import pandorum.components.Bundle;
import pandorum.components.Config.Gamemode;

import static pandorum.PluginVars.adminOnlyCommands;
import static pandorum.PluginVars.config;
import static pandorum.util.Utils.adminCheck;
import static pandorum.util.Utils.bundled;

public class ClientCommandsLoader {

    public static void registerClientCommands(CommandHandler handler) {
        registerClient(handler, "help", "[page]", false, HelpCommand::run);
        registerClient(handler, "discord", false, DiscordLinkCommand::run);
        registerClient(handler, "a", "<message...>", true, AdminChatCommand::run);
        registerClient(handler, "t", "<message...>", false, TeamChatCommand::run);
        registerClient(handler, "votekick", "<ID/username...>", false, VoteKickCommand::run);
        registerClient(handler, "vote", "<y/n>", false, VoteCommand::run);
        registerClient(handler, "sync", false, SyncCommand::run);
        registerClient(handler, "tr", "<current/list/off/auto/locale>", false, TranslatorCommand::run);
        registerClient(handler, "stats", "[ID/username...]", false, StatsCommand::run);
        registerClient(handler, "rank", "[ID/username...]", false, RankCommand::run);
        registerClient(handler, "players", "[page]", false, PlayersListCommand::run);

        registerClient(handler, "login", false, LoginCommand::run);

        registerClient(handler, "hub", false, Seq.with(Gamemode.attack, Gamemode.castle, Gamemode.crawler, Gamemode.hexed, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), HubCommand::run);

        registerClient(handler, "rtv", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), RtvCommand::run);
        registerClient(handler, "vnw", false, Seq.with(Gamemode.attack, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), VnwCommand::run);
        registerClient(handler, "surrender", false, Seq.with(Gamemode.pvp), SurrenderCommand::run);

        registerClient(handler, "history", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), HistoryCommand::run);
        registerClient(handler, "alert", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), AlertCommand::run);
        registerClient(handler, "map", false, MapCommand::run);
        registerClient(handler, "maps", "[page]", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), MapsListCommand::run);
        registerClient(handler, "saves", "[page]", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), SavesListCommand::run);
        registerClient(handler, "nominate", "<map/save/load> <name...>", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), NominateCommand::run);
        registerClient(handler, "voting", "<y/n>", false, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), VotingCommand::run);

        registerClient(handler, "spawn", "<unit> [amount] [team]", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), SpawnCommand::run);
        registerClient(handler, "core", "[small/medium/big] [team]", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), CoreCommand::run);
        registerClient(handler, "give", "<item> <amount>", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), GiveCommand::run);
        registerClient(handler, "unit", "<unit> [ID/username...]", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), UnitCommand::run);
        registerClient(handler, "team", "<team> [ID/username...]", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), TeamCommand::run);
        registerClient(handler, "spectate", "[ID/username...]", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), SpectateCommand::run);

        registerClient(handler, "artv", true, Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower), ArtvCommand::run);
        registerClient(handler, "despw", true, DespwCommand::run);
        registerClient(handler, "fill", "<width> <height> <block>", true, Seq.with(Gamemode.attack, Gamemode.sandbox, Gamemode.survival), FillCommand::run);
    }

    public static void registerClient(CommandHandler clientHandler, String text, String params, boolean adminOnly, Seq<Gamemode> modes, CommandHandler.CommandRunner<Player> runner) {
        if (!modes.contains(config.mode)) return;
        Command command = clientHandler.<Player>register(text, params, Bundle.get(Strings.format("commands.@.description", text), Bundle.defaultLocale()), (args, player) -> {
            if (adminOnly && !adminCheck(player)) {
                bundled(player, "commands.permission-denied");
                return;
            }
            Core.app.post(() -> runner.accept(args, player));
        });

        if (adminOnly) adminOnlyCommands.add(command);
    }

    public static void registerClient(CommandHandler clientHandler, String text, String params, boolean adminOnly, CommandHandler.CommandRunner<Player> runner) {
        registerClient(clientHandler, text, params, adminOnly, Seq.with(Gamemode.values()), runner);
    }

    public static void registerClient(CommandHandler clientHandler, String text, boolean adminOnly, Seq<Gamemode> modes, CommandHandler.CommandRunner<Player> runner) {
        registerClient(clientHandler, text, "", adminOnly, modes, runner);
    }

    public static void registerClient(CommandHandler clientHandler, String text, boolean adminOnly, CommandHandler.CommandRunner<Player> runner) {
        registerClient(clientHandler, text, "", adminOnly, runner);
    }
}
