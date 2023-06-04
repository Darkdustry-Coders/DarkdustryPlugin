package darkdustry.commands;

import arc.util.CommandHandler.CommandRunner;
import darkdustry.components.*;
import darkdustry.features.Authme;
import darkdustry.features.menus.MenuHandler;
import darkdustry.features.votes.*;
import darkdustry.utils.*;
import mindustry.gen.*;
import useful.*;

import static arc.util.Strings.*;
import static darkdustry.PluginVars.*;
import static darkdustry.features.menus.MenuHandler.*;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;
import static useful.Bundle.*;

public class ClientCommands {

    public static void load() {
        register("help", PageIterator::commands);

        register("discord", (args, player) -> Call.openURI(player.con, discordServerUrl));

        register("sync", (args, player) -> {
            Call.worldDataBegin(player.con);
            netServer.sendWorldData(player);
        });

        register("t", (args, player) -> Translator.translate(other -> other.team() == player.team(), player, args[0], "commands.t.chat", player.team().color, player.coloredName()));

        register("settings", (args, player) -> MenuHandler.showSettingsMenu(player));

        register("players", PageIterator::players);

        if (!config.hubIp.isEmpty())
            register("hub", (args, player) -> net.pingHost(config.hubIp, config.hubPort, host -> Call.connect(player.con, host.address, host.port), error -> Bundle.send(player, "commands.hub.failed", error.getMessage())));

        register("stats", (args, player) -> {
            var target = args.length > 0 ? Find.player(args[0]) : player;
            if (notFound(player, target)) return;

            MenuHandler.showStatsMenu(player, target, Cache.get(target));
        });

        register("votekick", (args, player) -> {
            if (votekickDisabled(player) || alreadyVoting(player, voteKick)) return;

            var target = Find.player(args[0]);
            if (notFound(player, target) || invalidVotekickTarget(player, target)) return;

            voteKick = new VoteKick(player, target, args[1]);
            voteKick.vote(player, 1);
        });

        register("vote", (args, player) -> {
            if (notVoting(player, voteKick) || alreadyVoted(player, voteKick)) return;

            int sign = voteChoice(args[0]);
            if (invalidVoteSign(player, sign)) return;

            if (invalidVoteTarget(player, voteKick.target)) return;
            voteKick.vote(player, sign);
        });

        register("login", (args, player) -> {
            if (alreadyAdmin(player)) return;

            showConfirmMenu(player, "commands.login.confirm", () -> {
                Authme.sendAdminRequest(player);
                Bundle.send(player, "commands.login.sent");
            });
        });

        if (config.mode.useRtv()) {
            register("rtv", (args, player) -> {
                if (alreadyVoting(player, vote)) return;

                var map = args.length > 0 ? Find.map(args[0]) : maps.getNextMap(state.rules.mode(), state.map);
                if (notFound(player, map)) return;

                vote = new VoteRtv(map);
                vote.vote(player, 1);
            });

            register("maps", PageIterator::maps);
        }

        if (config.mode.useVnw()) {
            register("vnw", (args, player) -> {
                if (alreadyVoting(player, vote)) return;

                int amount = args.length > 0 ? parseInt(args[0]) : 1;
                if (invalidAmount(player, amount, 1, maxVnwAmount)) return;

                vote = new VoteVnw(amount);
                vote.vote(player, 1);
            });
        }

        if (config.mode.isDefault()) {
            register("savemap", (args, player) -> {
                if (alreadyVoting(player, vote)) return;

                vote = new VoteSave(saveDirectory.child(args[0] + "." + saveExtension));
                vote.vote(player, 1);
            });

            register("loadsave", (args, player) -> {
                if (alreadyVoting(player, vote)) return;

                var save = Find.save(args[0]);
                if (notFound(player, save)) return;

                vote = new VoteLoad(save);
                vote.vote(player, 1);
            });

            register("saves", PageIterator::saves);
        }
    }

    public static void register(String name, CommandRunner<Player> runner) {
        clientCommands.<Player>register(name, Bundle.get("commands." + name + ".params", "", defaultLocale), Bundle.get("commands." + name + ".description", defaultLocale), (args, player) -> {
            if (onCooldown(player, name)) return;
            runner.accept(args, player);
            Cooldowns.run(player, name);
        });
    }
}