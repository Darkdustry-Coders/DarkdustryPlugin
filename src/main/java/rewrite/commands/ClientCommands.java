package rewrite.commands;

import arc.files.Fi;
import arc.util.CommandHandler;
import mindustry.gen.Player;
import mindustry.maps.Map;
import rewrite.discord.Bot;
import rewrite.features.votes.*;
import rewrite.utils.Find;

import java.util.Locale;

import static mindustry.Vars.*;
import static rewrite.PluginVars.*;
import static rewrite.components.Bundle.*;
import static rewrite.utils.Checks.*;
import static rewrite.utils.Utils.*;

public class ClientCommands extends Commands<Player> {

    public ClientCommands(CommandHandler handler, Locale locale) {
        super(handler, locale);

        register("help", (args, player) -> {

        });
        register("discord", (args, player) -> {

        });
        register("t", (args, player) -> {

        });
        register("sync", (args, player) -> {

        });
        register("tr", (args, player) -> {

        });
        register("stats", (args, player) -> {

        });
        register("rank", (args, player) -> {

        });
        register("players", (args, player) -> {

        });
        register("hub", (args, player) -> {

        });

        register("vote", (args, player) -> {
            if (notVoting(player) || isVoted(player)) return;
            if (vote instanceof VoteKick kick) {
                if (kick.target == player) {
                    bundled(player, "commands.vote.player-is-you");
                    return;
                } else if (kick.target.team() != player.team()) {
                    bundled(player, "commands.vote.player-is-enemy");
                    return;
                }
            }

            int sign = voteChoice(args[0]);
            if (isInvalide(player, sign)) return;
            vote.vote(player, sign);
        });

        register("votekick", (args, player) -> {
            if (isVoting(player) || isCooldowned(player, "votekick") || isDisabled(player)) return;
            Player target = Find.player(args[0]);
            if (notFound(player, target, args[0]) || isInvalide(player, target)) return;

            vote = new VoteKick(player, target);
            vote.vote(player, 1);
        });

        if (!config.mode.isDefault()) return;

        register("rtv", (args, player) -> {
            if (isVoting(player)) return;
            vote = new VoteRtv();
            vote.vote(player, 1);
        });

        register("vnw", (args, player) -> {
            if (isVoting(player)) return;
            vote = new VoteVnw();
            vote.vote(player, 1);
        });

        register("nominate", (args, player) -> {
            if (isVoting(player) || isCooldowned(player, "nominate")) return;

            switch (args[0].toLowerCase()) {
                case "map" -> {
                    Map map = Find.map(args[1]);
                    if (notFound(player, map)) return;
                    vote = new VoteMap(map);
                    vote.vote(player, 1);
                }
                case "save" -> {
                    vote = new VoteSave(saveDirectory.child(args[1] + "." + saveExtension));
                    vote.vote(player, 1);
                }
                case "load" -> {
                    Fi save = Find.save(args[1]);
                    if (notFound(player, save)) return;
                    vote = new VoteLoad(save);
                    vote.vote(player, 1);
                }
                default -> bundled(player, "commands.nominate.incorrect-mode");
            }
        });

        register("maps", (args, player) -> {

        });
        register("saves", (args, player) -> {

        });
        register("history", (args, player) -> {

        });
        register("alerts", (args, player) -> {

        });
        register("login", (args, player) -> {
            if (isAdmin(player) || isCooldowned(player, "login")) return;

            Bot.sendMessageToAdmin(player);
            bundled(player, "commands.login.sent");
        });
    }
}
