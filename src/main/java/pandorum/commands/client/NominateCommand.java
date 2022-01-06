package pandorum.commands.client;

import arc.files.Fi;
import arc.util.Strings;
import arc.util.Timekeeper;
import mindustry.gen.Player;
import mindustry.maps.Map;
import pandorum.vote.VoteLoadSession;
import pandorum.vote.VoteMapSession;
import pandorum.vote.VoteSaveSession;
import pandorum.vote.VoteSession;

import java.util.concurrent.TimeUnit;

import static mindustry.Vars.saveDirectory;
import static mindustry.Vars.saveExtension;
import static pandorum.Misc.*;
import static pandorum.PluginVars.*;

public class NominateCommand {
    public static void run(final String[] args, final Player player) {
        if (current[0] != null) {
            bundled(player, "commands.vote-already-started");
            return;
        }

        Timekeeper vtime = nominateCooldowns.get(player.uuid(), () -> new Timekeeper(nominateCooldownTime));
        if (!vtime.get() && !player.admin) {
            bundled(player, "commands.nominate.cooldown", TimeUnit.SECONDS.toMinutes(nominateCooldownTime));
            return;
        }

        switch (args[0].toLowerCase()) {
            case "map" -> {
                Map map = findMap(args[1]);
                if (map == null) {
                    bundled(player, "commands.nominate.map.not-found");
                    return;
                }
                VoteSession session = new VoteMapSession(current, map);
                current[0] = session;
                session.vote(player, 1);
                vtime.reset();
            }
            case "save" -> {
                Fi save = saveDirectory.child(Strings.format("@.@", args[1], saveExtension));
                if (save.exists()) {
                    bundled(player, "commands.nominate.save.already-exists");
                    return;
                }
                VoteSession session = new VoteSaveSession(current, save);
                current[0] = session;
                session.vote(player, 1);
                vtime.reset();
            }
            case "load" -> {
                Fi save = findSave(args[1]);
                if (save == null) {
                    bundled(player, "commands.nominate.load.not-found");
                    return;
                }
                VoteSession session = new VoteLoadSession(current, save);
                current[0] = session;
                session.vote(player, 1);
                vtime.reset();
            }
            default -> bundled(player, "commands.nominate.incorrect-mode");
        }
    }
}
