package darkdustry.features.votes;

import arc.math.Mathf;
import mindustry.game.Team;
import mindustry.gen.*;
import useful.Bundle;

import static darkdustry.PluginVars.*;
import static darkdustry.utils.Checks.*;

public class VoteSurrender extends VoteSession{

    public final Team team;

    public VoteSurrender(Team team) {
        this.team = team;
    }

    public void vote(Player player, int sign) {
        if (otherSurrenderTeam(player, team)) return;

        Bundle.send(sign == 1 ? "commands.surrender.yes" : "commands.surrender.no", player.coloredName(), team.coloredName(), votes() + sign, votesRequired());
        super.vote(player, sign);
    }

    @Override
    public void left(Player player) {
        if (votes.remove(player) != 0)
            Bundle.send("commands.surrender.left", player.coloredName(), votes(), votesRequired());
    }

    @Override
    public void success() {
        stop();
        Bundle.send("commands.surrender.success", team.coloredName());

        team.data().destroyToDerelict();
    }

    @Override
    public void fail() {
        stop();
        Bundle.send("commands.surrender.fail", team.coloredName());
    }

    @Override
    public int votesRequired() {
        return Math.max(Math.min(2, team.data().players.size), Mathf.ceil(team.data().players.size * voteRatio));
    }
}