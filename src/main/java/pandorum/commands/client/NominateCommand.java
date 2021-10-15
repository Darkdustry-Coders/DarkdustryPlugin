package pandorum.commands.client;

import arc.files.Fi;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.maps.Map;
import pandorum.Misc;
import pandorum.vote.VoteLoadSession;
import pandorum.vote.VoteMapSession;
import pandorum.vote.VoteSaveSession;
import pandorum.vote.VoteSession;

import static pandorum.Misc.bundled;
import static pandorum.PandorumPlugin.current;

public class NominateCommand {
    public static void run(final String[] args, final Player player) {
        if (Groups.player.size() < 3) {
            bundled(player, "commands.not-enough-players");
            return;
        }

        if (current[0] != null) {
            bundled(player, "commands.vote-already-started");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "map" -> {
                Map map = Misc.findMap(args[1]);
                if (map == null) {
                    bundled(player, "commands.nominate.map.not-found");
                    return;
                }
                VoteSession session = new VoteMapSession(current, map);
                current[0] = session;
                session.vote(player, 1);
            }
            case "save" -> {
                VoteSession session = new VoteSaveSession(current, args[1]);
                current[0] = session;
                session.vote(player, 1);
            }
            case "load" -> {
                Fi save = Misc.findSave(args[1]);
                if (save == null) {
                    bundled(player, "commands.nominate.load.not-found");
                    return;
                }
                VoteSession session = new VoteLoadSession(current, save);
                current[0] = session;
                session.vote(player, 1);
            }
            default -> bundled(player, "commands.nominate.incorrect-mode");
        }
    }
}
