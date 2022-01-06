package pandorum;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Timekeeper;
import mindustry.game.Team;
import pandorum.comp.Config;
import pandorum.entry.HistoryEntry;
import pandorum.struct.CacheSeq;
import pandorum.vote.VoteKickSession;
import pandorum.vote.VoteSession;

public class PluginVars {

    public static final int maxFillSize = 25, maxSpawnAmount = 25;
    public static final int nominateCooldownTime = 300, votekickCooldownTime = 300, loginCooldownTime = 1200;

    public static final Team spectateTeam = Team.derelict;

    public static final VoteSession[] current = {null};
    public static final VoteKickSession[] currentlyKicking = {null};

    public static final ObjectMap<Team, Seq<String>> votesSurrender = new ObjectMap<>();

    public static final ObjectMap<String, Timekeeper> nominateCooldowns = new ObjectMap<>(), votekickCooldowns = new ObjectMap<>(), loginCooldowns = new ObjectMap<>();
    public static final ObjectMap<String, Team> activeSpectatingPlayers = new ObjectMap<>();

    public static final Seq<String> votesRTV = new Seq<>(), votesVNW = new Seq<>(), activeHistoryPlayers = new Seq<>();

    public static final ObjectMap<String, String> codeLanguages = new ObjectMap<>();

    public static final Interval interval = new Interval(2);

    public static Config config;
    public static CacheSeq<HistoryEntry>[][] history;
}
