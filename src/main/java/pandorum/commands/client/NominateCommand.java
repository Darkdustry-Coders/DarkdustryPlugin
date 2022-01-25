package pandorum.commands.client;

import arc.files.Fi;
import arc.util.Strings;
import arc.util.Timekeeper;
import mindustry.gen.Player;
import mindustry.io.SaveIO;
import mindustry.maps.Map;
import pandorum.utils.Utils;
import pandorum.vote.VoteLoadSession;
import pandorum.vote.VoteMapSession;
import pandorum.vote.VoteSaveSession;
import pandorum.vote.VoteSession;

import static mindustry.Vars.saveDirectory;
import static mindustry.Vars.saveExtension;
import static pandorum.utils.Search.*;
import static pandorum.PluginVars.*;

public class NominateCommand {
    public static void run(final String[] args, final Player player) {
        if (currentVote[0] != null) {
            Utils.bundled(player, "commands.vote-already-started");
            return;
        }

        Timekeeper vtime = nominateCooldowns.get(player.uuid(), () -> new Timekeeper(nominateCooldownTime));
        if (!vtime.get() && !player.admin) {
            Utils.bundled(player, "commands.nominate.cooldown", Utils.secondsToMinutes(nominateCooldownTime));
            return;
        }

        switch (args[0].toLowerCase()) {
            case "map" -> {
                Map map = findMap(args[1]);
                if (map == null) {
                    Utils.bundled(player, "commands.nominate.map.not-found");
                    return;
                }
                VoteSession session = new VoteMapSession(currentVote, map);
                currentVote[0] = session;
                session.vote(player, 1);
                vtime.reset();
            }
            case "save" -> {
                Fi save = saveDirectory.child(Strings.format("@.@", args[1], saveExtension));
                if (SaveIO.isSaveValid(save)) {
                    Utils.bundled(player, "commands.nominate.save.already-exists");
                    return;
                }
                VoteSession session = new VoteSaveSession(currentVote, save);
                currentVote[0] = session;
                session.vote(player, 1);
                vtime.reset();
            }
            case "load" -> {
                Fi save = findSave(args[1]);
                if (save == null) {
                    Utils.bundled(player, "commands.nominate.load.not-found");
                    return;
                }
                VoteSession session = new VoteLoadSession(currentVote, save);
                currentVote[0] = session;
                session.vote(player, 1);
                vtime.reset();
            }
            default -> Utils.bundled(player, "commands.nominate.incorrect-mode");
        }
    }
}
