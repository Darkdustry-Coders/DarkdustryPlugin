package pandorum.commands.client;

import arc.files.Fi;
import arc.util.CommandHandler.CommandRunner;
import arc.util.Timekeeper;
import mindustry.gen.Player;
import mindustry.maps.Map;
import pandorum.util.Utils;
import pandorum.vote.VoteLoadSession;
import pandorum.vote.VoteMapSession;
import pandorum.vote.VoteSaveSession;
import pandorum.vote.VoteSession;

import static mindustry.Vars.saveDirectory;
import static mindustry.Vars.saveExtension;
import static pandorum.PluginVars.*;
import static pandorum.util.PlayerUtils.bundled;
import static pandorum.util.Search.findMap;
import static pandorum.util.Search.findSave;

public class NominateCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        if (currentVote[0] != null) {
            bundled(player, "commands.vote-already-started");
            return;
        }

        Timekeeper cooldown = nominateCooldowns.get(player.uuid(), () -> new Timekeeper(nominateCooldownTime));
        if (!cooldown.get() && !player.admin) {
            bundled(player, "commands.nominate.cooldown", Utils.secondsToMinutes(nominateCooldownTime));
            return;
        }

        switch (args[0].toLowerCase()) {
            case "map" -> {
                Map map = findMap(args[1]);
                if (map == null) {
                    bundled(player, "commands.nominate.map.not-found");
                    return;
                }
                VoteSession session = new VoteMapSession(currentVote, map);
                currentVote[0] = session;
                session.vote(player, 1);
                cooldown.reset();
            }
            case "save" -> {
                Fi save = saveDirectory.child(args[1] + "." + saveExtension);
                VoteSession session = new VoteSaveSession(currentVote, save);
                currentVote[0] = session;
                session.vote(player, 1);
                cooldown.reset();
            }
            case "load" -> {
                Fi save = findSave(args[1]);
                if (save == null) {
                    bundled(player, "commands.nominate.load.not-found");
                    return;
                }
                VoteSession session = new VoteLoadSession(currentVote, save);
                currentVote[0] = session;
                session.vote(player, 1);
                cooldown.reset();
            }
            default -> bundled(player, "commands.nominate.incorrect-mode");
        }
    }
}
