package pandorum;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.CommandHandler.Command;
import arc.util.Interval;
import arc.util.Timekeeper;
import arc.util.Timer.Task;
import arc.util.io.ReusableByteOutStream;
import arc.util.io.Writes;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.reactivestreams.client.MongoCollection;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.entities.Effect;
import mindustry.game.Team;
import mindustry.gen.Player;
import mindustry.type.Item;
import mindustry.world.Block;
import net.dv8tion.jda.api.entities.Message;
import okhttp3.OkHttpClient;
import org.bson.Document;
import pandorum.components.Config;
import pandorum.history.TilesHistory;
import pandorum.history.entry.HistoryEntry;
import pandorum.vote.VoteKickSession;
import pandorum.vote.VoteSession;

import static mindustry.Vars.tilesize;

public class PluginVars {

    /** Максимальный размер заполняемого пространства через /fill. */
    public static final int maxFillSize = 25;
    /** Максимальное количество заспавненных юнитов через /spawn. */
    public static final int maxSpawnAmount = 25;

    /** Время кулдауна для команды nominate. В секундах. */
    public static final int nominateCooldownTime = 300;
    /** Время кулдауна для команды votekick. В секундах */
    public static final int votekickCooldownTime = 300;
    /** Время кулдауна для команды login. В секундах */
    public static final int loginCooldownTime = 1200;
    /** Время кулдауна для команды sync. В секундах */
    public static final int syncCooldownTime = 15;

    /** Необходимое количество игроков для успешного завершения голосования. */
    public static final float voteRatio = 0.6f;

    /** Максимальное количество записей на один тайл. */
    public static final int maxTileHistoryCapacity = 6;
    /** Ёмкость кэша, хранящего историю. */
    public static final int allHistorySize = 100000;

    /** Расстояние до ядер, в котором отслеживаются опасные блоки. */
    public static final int alertsDistance = 8 * tilesize;
    /** Таймер для оповещения об опасных блоков */
    public static final float alertsTimer = 600f;

    /** Время голосования через /nominate. В секундах. */
    public static final float voteDuration = 150f;
    /** Время голосования через /votekick. В секундах. */
    public static final float votekickDuration = 40f;
    /** Время, на которое игрок будет выгнан голосованием или через команду. В миллисекундах. */
    public static final long kickDuration = 2700000L;

    /** Время, на которое игрок будет выгнан за абьюз команды /login. В миллисекундах. */
    public static final long loginAbuseKickDuration = 2700000L;

    /** Локаль по умолчанию. */
    public static final String defaultLocale = "en", defaultTranslatorLocale = "en_GB";

    /** Ссылка на наш Discord сервер */
    public static final String discordServerUrl = "discord.gg/uDPPVN6V3E";

    /** Название файла с конфигурацией. */
    public static final String configFileName = "config.json";

    /** Url для подключения к базе данных. */
    public static final String connectionStringUrl = "mongodb://manager:QULIoZBckRlLkZXn@127.0.0.1:27017/?authSource=darkdustry";
    /** Название базы данных. */
    public static final String databaseName = "darkdustry";
    /** Название коллекции со статистикой игроков в базе данных. */
    public static final String playersCollectionName = "players";
    /** Название коллекции со статистикой карт в базе данных. */
    public static final String mapsCollectionName = "maps";

    /** Команда для наблюдателей. */
    public static final Team spectateTeam = Team.derelict;

    /** Эффект при входе на сервер. */
    public static final Effect joinEffect = Fx.greenBomb;
    /** Эффект при выходе с сервера. */
    public static final Effect leaveEffect = Fx.greenLaserCharge;
    /** Эффект при движении игрока. */
    public static final Effect moveEffect = Fx.freezing;

    public static final VoteSession[] currentVote = {null};
    public static final VoteKickSession[] currentVotekick = {null};

    public static final ObjectMap<Team, Seq<String>> votesSurrender = new ObjectMap<>();
    public static final ObjectMap<String, Timekeeper> nominateCooldowns = new ObjectMap<>(), votekickCooldowns = new ObjectMap<>(), loginCooldowns = new ObjectMap<>();
    public static final ObjectMap<String, Team> activeSpectatingPlayers = new ObjectMap<>();
    public static final ObjectMap<String, String> codeLanguages = new ObjectMap<>();
    public static final ObjectMap<Message, Player> loginWaiting = new ObjectMap<>();
    public static final ObjectMap<Block, Item> dangerousDepositBlocks = new ObjectMap<>();

    public static final Seq<String> votesRtv = new Seq<>(), votesVnw = new Seq<>(), mapRateVotes = new Seq<>(), activeHistoryPlayers = new Seq<>();
    public static final Seq<Command> adminOnlyCommands = new Seq<>();
    public static final Seq<Block> dangerousBlocks = new Seq<>();

    public static final Interval interval = new Interval();

    public static final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES).setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();

    public static final OkHttpClient client = new OkHttpClient();
    public static final TilesHistory<HistoryEntry> history = new TilesHistory<>(maxTileHistoryCapacity, allHistorySize);

    /** Время непрерывной работы сервера. */
    public static int serverUpTime = 0;

    /** Время, проведенное на текущей карте. */
    public static int mapPlayTime = 0;

    /** Могут ли игроки голосовать в данный момент. */
    public static boolean canVote = false;

    public static MongoCollection<Document> playersInfoCollection, mapsInfoCollection;
    public static Config config;
    public static Task worldLoadTask = null;

    public static ReusableByteOutStream writeBuffer;
    public static Writes outputBuffer;
}
