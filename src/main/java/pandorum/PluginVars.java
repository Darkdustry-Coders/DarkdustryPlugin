package pandorum;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.CommandHandler.Command;
import arc.util.Interval;
import arc.util.Timekeeper;
import discord4j.core.object.entity.Message;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.game.Team;
import mindustry.gen.Player;
import pandorum.comp.Config;
import pandorum.entry.HistoryEntry;
import pandorum.struct.CacheSeq;
import pandorum.vote.VoteKickSession;
import pandorum.vote.VoteSession;

public class PluginVars {

    /** Максимальный размер заполняемого пространства через /fill. Максимальное количество заспавненных юнитов через /spawn. */
    public static final int maxFillSize = 25, maxSpawnAmount = 25;

    /** Время кулдауна для различных команд. В секундах. */
    public static final int nominateCooldownTime = 300, votekickCooldownTime = 300, loginCooldownTime = 1200;

    /** Необходимое количество игроков для успешного завершения голосования. */
    public static final float voteRatio = 0.6f;

    /** Ёмкость массива, хранящего информацию о действиях с тайлом. Может сильно влиять на трату ОЗУ. */
    public static final int historyLimit = 6;

    /** Время, через которое запись в истории тайла будет удалена. В миллисекундах. */
    public static final long expireDelay = 1800000L;

    /** Расстояние до ядер, в котором отслеживаются ториевые реакторы. */
    public static final int alertsDistance = 120;

    /** Время голосования через /nominate. В секундах. */
    public static final float voteDuration = 150f;

    /** Время голосования через /votekick. В секундах. */
    public static final float votekickDuration = 40f;

    /** Время, на которое игрок будет выгнан голосованием. В миллисекундах. */
    public static final long kickDuration = 2700000L;

    /** Название файла с конфигурацией. */
    public static final String configFileName = "config.json";
    public static final String connectionStringUrl = "mongodb://manager:QULIoZBckRlLkZXn@127.0.0.1:27017/?authSource=darkdustry", databaseName = "darkdustry", collectionName = "players";

    /** Команда для наблюдателей. */
    public static final Team spectateTeam = Team.derelict;

    /** Различные эффекты. */
    public static final Effect joinEffect = Fx.greenBomb, leaveEffect = Fx.greenLaserCharge, moveEffect = Fx.freezing;

    public static final VoteSession[] currentVote = {null};
    public static final VoteKickSession[] currentVotekick = {null};

    public static final ObjectMap<Team, Seq<String>> votesSurrender = new ObjectMap<>();

    public static final ObjectMap<String, Timekeeper> nominateCooldowns = new ObjectMap<>(), votekickCooldowns = new ObjectMap<>(), loginCooldowns = new ObjectMap<>();
    public static final ObjectMap<String, Team> activeSpectatingPlayers = new ObjectMap<>();

    public static final ObjectMap<String, String> codeLanguages = new ObjectMap<>();
    public static final ObjectMap<Message, Player> loginWaiting = new ObjectMap<>();

    public static final Seq<String> votesRTV = new Seq<>(), votesVNW = new Seq<>(), activeHistoryPlayers = new Seq<>();

    public static final Seq<Command> adminOnlyCommands = new Seq<>();

    public static final Interval interval = new Interval(2);

    public static Config config;
    public static CacheSeq<HistoryEntry>[][] history;
}
