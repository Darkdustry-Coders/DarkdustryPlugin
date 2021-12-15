package pandorum.commands.client;

import arc.files.Fi;
import arc.util.Timekeeper;
import mindustry.gen.Player;
import mindustry.maps.Map;
import pandorum.Misc;
import pandorum.annotations.commands.ClientCommand;
import pandorum.annotations.commands.gamemodes.RequireSimpleGamemode;
import pandorum.vote.VoteLoadSession;
import pandorum.vote.VoteMapSession;
import pandorum.vote.VoteSaveSession;
import pandorum.vote.VoteSession;

import static pandorum.Misc.bundled;
import static pandorum.PandorumPlugin.current;
import static pandorum.PandorumPlugin.nominateCooldowns;

public class NominateCommand {
    private static final float cooldownTime = 300f;

    @RequireSimpleGamemode
    @ClientCommand(name = "nominate", args = "<map/save/load> <name...>", description = "Vote for load a save/map.")
    public static void run(final String[] args, final Player player) {
        if (current[0] != null) {
            bundled(player, "commands.vote-already-started");
            return;
        }

        Timekeeper vtime = nominateCooldowns.get(player.uuid(), () -> new Timekeeper(cooldownTime));
        if (!vtime.get()) {
            bundled(player, "commands.nominate.cooldown", (int) (cooldownTime / 60f));
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
                vtime.reset();
            }
            case "save" -> {
                VoteSession session = new VoteSaveSession(current, args[1]);
                current[0] = session;
                session.vote(player, 1);
                vtime.reset();
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
                vtime.reset();
            }
            default -> bundled(player, "commands.nominate.incorrect-mode");
        }
    }
}
