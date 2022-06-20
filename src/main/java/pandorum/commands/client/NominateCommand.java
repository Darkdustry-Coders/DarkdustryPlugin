package pandorum.commands.client;

import arc.files.Fi;
import arc.util.CommandHandler.CommandRunner;
import arc.util.Timekeeper;
import mindustry.gen.Player;
import mindustry.maps.Map;
import pandorum.vote.VoteLoadSession;
import pandorum.vote.VoteMapSession;
import pandorum.vote.VoteSaveSession;

import static mindustry.Vars.saveDirectory;
import static mindustry.Vars.saveExtension;
import static pandorum.PluginVars.*;
import static pandorum.util.PlayerUtils.bundled;
import static pandorum.util.Search.findMap;
import static pandorum.util.Search.findSave;

public class NominateCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        if (currentVote != null) {
            bundled(player, "commands.vote-already-started");
            return;
        }

        Timekeeper cooldown = nominateCooldowns.get(player.uuid(), () -> new Timekeeper(nominateCooldownTime));
        if (!cooldown.get() && !player.admin) {
            bundled(player, "commands.cooldown", nominateCooldownTime / 60);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "map" -> {
                Map map = findMap(args[1]);
                if (map == null) {
                    bundled(player, "commands.nominate.map.not-found");
                    return;
                }
                currentVote = new VoteMapSession(map);
                currentVote.vote(player, 1);
                cooldown.reset();
            }
            case "save" -> {
                Fi save = saveDirectory.child(args[1] + "." + saveExtension);
                currentVote = new VoteSaveSession(save);
                currentVote.vote(player, 1);
                cooldown.reset();
            }
            case "load" -> {
                Fi save = findSave(args[1]);
                if (save == null) {
                    bundled(player, "commands.nominate.load.not-found");
                    return;
                }
                currentVote = new VoteLoadSession(save);
                currentVote.vote(player, 1);
                cooldown.reset();
            }
            default -> bundled(player, "commands.nominate.incorrect-mode");
        }
    }
}
