package pandorum;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.CommandHandler.Command;
import arc.util.Interval;
import arc.util.Timekeeper;
import arc.util.io.ReusableByteOutStream;
import arc.util.io.Writes;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.reactivestreams.client.MongoCollection;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.game.Team;
import mindustry.gen.Player;
import net.dv8tion.jda.api.entities.Message;
import okhttp3.OkHttpClient;
import org.bson.Document;
import pandorum.comp.Config;
import pandorum.entry.HistoryEntry;
import pandorum.struct.CacheSeq;
import pandorum.vote.VoteKickSession;
import pandorum.vote.VoteSession;

public class PluginVars {

    /** Время непрерывной работы сервера. */
    public static int serverUpTime = 0,
            /** Время, проведенное на текущей карте. */
            mapPlayTime = 0;

    /** Максимальный размер заполняемого пространства через /fill. */
    public static final int maxFillSize = 25,
            /** Максимальное количество заспавненных юнитов через /spawn. */
            maxSpawnAmount = 25;

    /** Время кулдауна для команды nominate. В секундах. */
    public static final int nominateCooldownTime = 300,
            /** Время кулдауна для команды votekick. В секундах */
            votekickCooldownTime = 300,
            /** Время кулдауна для команды login. В секундах */
            loginCooldownTime = 1200,
            /** Время кулдауна для команды sync. В секундах */
            syncCooldownTime = 15;

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

    /** Время, на которое игрок будет выгнан голосованием или через команду. В миллисекундах. */
    public static final long kickDuration = 2700000L;

    /** Локаль по умолчанию. */
    public static final String defaultLocale = "en";

    /** Ссылка на наш Discord сервер */
    public static final String discordServerUrl = "discord.gg/uDPPVN6V3E";

    /** Название файла с конфигурацией. */
    public static final String configFileName = "config.json";

    /** Url для подключения к базе данных. */
    public static final String connectionStringUrl = "mongodb://manager:QULIoZBckRlLkZXn@127.0.0.1:27017/?authSource=darkdustry",
            /** Название базы данных. */
            databaseName = "darkdustry",
            /** Название коллекции со статистикой игроков в базе данных. */
            playersCollectionName = "players",
            /** Название коллекции со статистикой карт в базе данных. */
            mapsCollectionName = "maps";

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

    public static final Seq<String> votesRtv = new Seq<>(), votesVnw = new Seq<>(), activeHistoryPlayers = new Seq<>();

    public static final Seq<Command> adminOnlyCommands = new Seq<>();

    public static final Interval interval = new Interval();

    public static final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES).setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();

    public static final OkHttpClient client = new OkHttpClient();

    public static MongoCollection<Document> playersInfoCollection, mapsInfoCollection;

    public static Config config;
    public static CacheSeq<HistoryEntry>[][] history;

    public static ReusableByteOutStream writeBuffer;
    public static Writes outputBuffer;
}
