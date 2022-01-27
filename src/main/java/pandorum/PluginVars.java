package pandorum;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.CommandHandler.Command;
import arc.util.Interval;
import arc.util.Timekeeper;
import arc.util.Timer.Task;
import arc.util.io.ReusableByteOutStream;
import arc.util.io.Writes;
import darkdustry.mindustry.effects.utils.Engine;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import discord4j.core.object.entity.Message;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.game.Team;
import mindustry.gen.Player;
import okhttp3.OkHttpClient;
import pandorum.comp.Config;
import pandorum.comp.TilesHistory;
import pandorum.entry.CacheEntry;
import pandorum.vote.VoteKickSession;
import pandorum.vote.VoteSession;

public class PluginVars {

    /**
     * Максимальный размер заполняемого пространства через /fill. Максимальное количество заспавненных юнитов через /spawn.
     */
    public static final int maxFillSize = 25, maxSpawnAmount = 25;

    /**
     * Время кулдауна для различных команд. В секундах.
     */
    public static final int nominateCooldownTime = 300, votekickCooldownTime = 300, loginCooldownTime = 1200, syncCooldownTime = 15;

    /**
     * Необходимое количество игроков для успешного завершения голосования.
     */
    public static final float voteRatio = 0.6f;

    /**
     * Ёмкость кэша, хранящего историю.
     */
    public static final int historySize = 40000;

    /**
     * Максимальное количество записей на один тайл
     */
    public static final byte maxTileHistory = 8;

    /**
     * Время, через которое запись в истории тайла будет удалена. В минутах.
     */
    public static final int expireDelay = 30;

    /**
     * Расстояние до ядер, в котором отслеживаются ториевые реакторы.
     */
    public static final int alertsDistance = 120;

    /**
     * Время голосования через /nominate. В секундах.
     */
    public static final float voteDuration = 150f;

    /**
     * Время голосования через /votekick. В секундах.
     */
    public static final float votekickDuration = 40f;

    /**
     * Время, на которое игрок будет выгнан голосованием или через команду. В миллисекундах.
     */
    public static final long kickDuration = 2700000L;

    /**
     * Локаль по умолчанию.
     */
    public static final String defaultLocale = "en";

    /**
     * Ссылка на наш Discord сервер
     */
    public static final String discordServerUrl = "discord.gg/uDPPVN6V3E";

    /**
     * Название файла с конфигурацией.
     */
    public static final String configFileName = "config.json";

    /**
     * Url для подключения к базе данных. Название базы данных. Название коллекции со статистикой игроков в базе данных.
     */
    public static final String connectionStringUrl = "mongodb://manager:QULIoZBckRlLkZXn@127.0.0.1:27017/?authSource=darkdustry", databaseName = "darkdustry", collectionName = "players";

    /**
     * Команда для наблюдателей.
     */
    public static final Team spectateTeam = Team.derelict;

    /**
     * Различные эффекты.
     */
    public static final Effect joinEffect = Fx.greenBomb, leaveEffect = Fx.greenLaserCharge, moveEffect = Fx.freezing;

    public static final VoteSession[] currentVote = {null};
    public static final VoteKickSession[] currentVotekick = {null};

    public static final ObjectMap<Team, Seq<String>> votesSurrender = new ObjectMap<>();

    public static final ObjectMap<String, Timekeeper> nominateCooldowns = new ObjectMap<>(), votekickCooldowns = new ObjectMap<>(), loginCooldowns = new ObjectMap<>();
    public static final ObjectMap<String, Team> activeSpectatingPlayers = new ObjectMap<>();

    public static final ObjectMap<String, Task> updateTimers = new ObjectMap<>();

    public static final ObjectMap<String, String> codeLanguages = new ObjectMap<>();
    public static final ObjectMap<Message, Player> loginWaiting = new ObjectMap<>();

    public static final ObjectMap<String, Boolean> antiVpnCache = new ObjectMap<>();

    public static final Seq<String> votesRtv = new Seq<>(), votesVnw = new Seq<>(), activeHistoryPlayers = new Seq<>();

    public static final Seq<Command> adminOnlyCommands = new Seq<>();

    public static final Interval interval = new Interval();

    public static final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES).setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();

    public static final OkHttpClient client = new OkHttpClient();

    public static Config config;
    public static TilesHistory<CacheEntry> history = new TilesHistory<>(maxTileHistory, expireDelay, historySize);

    public static ReusableByteOutStream writeBuffer;
    public static Writes outputBuffer;

    public static Engine staticAnimationEngine = new Engine();
    public static Engine dynamicAnimationEngine = new Engine();
}
