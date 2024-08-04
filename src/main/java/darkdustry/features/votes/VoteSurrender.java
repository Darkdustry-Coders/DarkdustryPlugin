package darkdustry.features.votes;

import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.world.blocks.storage.CoreBlock;
import useful.Bundle;

import java.util.Objects;

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

        var data = team.data();

        var builds = new Seq<Building>();
        if (data.buildingTree != null) {
            data.buildingTree.getObjects(builds);
        }

        data.plans.clear();

        for (Building b : builds) {
            if (b instanceof CoreBlock.CoreBuild) {
                b.kill();
            } else if (b.block != Blocks.worldCell && b.block != Blocks.worldProcessor) {
                Call.setTeam(b, Team.derelict);
                if (Mathf.chance(0.25)) {
                    Time.run(Mathf.random(0.0F, 360.0F), b::kill);
                }
            }
        }

        data.units.each((u) -> {
            Time.run(Mathf.random(0.0F, 300.0F), () -> {
                if (u.team == this.team) u.kill();
            });
        });
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