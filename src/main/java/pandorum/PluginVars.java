package pandorum;

import arc.func.Boolp;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Interval;
import arc.util.Timekeeper;
import arc.util.io.ReusableByteOutStream;
import arc.util.io.Writes;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.game.Team;
import mindustry.gen.Iconc;
import mindustry.type.Item;
import mindustry.world.Block;
import net.dv8tion.jda.api.entities.Message;
import pandorum.components.Gamemode;
import pandorum.components.PluginConfig;
import pandorum.features.Translator.Language;
import pandorum.features.history.HistorySeq;
import pandorum.vote.VoteKickSession;
import pandorum.vote.VoteSession;
import redis.clients.jedis.JedisPool;

import static mindustry.Vars.tilesize;

public class PluginVars {

    /** IP адрес сервера. */
    public static final String serverIp = "darkdustry.ml";

    /** Максимальный размер заполняемого пространства через /fill. */
    public static final int maxFillSize = 512;
    /** Максимальное количество заспавненных юнитов через /spawn. */
    public static final int maxSpawnAmount = 25;

    /** Время кулдауна для команды /nominate. В секундах. */
    public static final int nominateCooldownTime = 150;
    /** Время кулдауна для команды /votekick. В секундах. */
    public static final int voteKickCooldownTime = 300;
    /** Время кулдауна для команды /login. В секундах. */
    public static final int loginCooldownTime = 900;
    /** Время кулдауна для команды /sync. В миллисекундах. */
    public static final int syncCooldownTime = 15000;

    /** Необходимое количество игроков для успешного завершения голосования. */
    public static final float voteRatio = 0.6f;

    /** Максимальное количество записей истории на один тайл. */
    public static final int maxTileHistoryCapacity = 6;

    /** Расстояние до ядер, в котором отслеживаются опасные блоки. */
    public static final int alertsDistance = 8 * tilesize;
    /** Интвервал оповещений о постройке опасных блоков. */
    public static final float alertsInterval = 150f;

    /** Время голосования через /nominate. В секундах. */
    public static final float voteDuration = 120f;
    /** Время голосования через /votekick. В секундах. */
    public static final float voteKickDuration = 45f;
    /** Время, на которое игрок будет выгнан голосованием или через команду. В миллисекундах. */
    public static final long kickDuration = 2700000L;

    /** Локаль по умолчанию. */
    public static final String defaultLocale = "en", defaultTranslatorLocale = "en_GB";

    /** Ключ API для переводчика чата. */
    public static final String translatorApiKey = "Bearer a_25rccaCYcBC9ARqMODx2BV2M0wNZgDCEl3jryYSgYZtF1a702PVi4sxqi2AmZWyCcw4x209VXnCYwesx";

    /** Ссылка на наш Discord сервер */
    public static final String discordServerUrl = "discord.gg/45NNzjGCmY";

    /** Название файла с конфигурацией. */
    public static final String configFileName = "config.json";

    /** Порт для подключения базы данных Jedis. */
    public static final int jedisPoolPort = 6379;

    /** Команда для наблюдателей. */
    public static final Team spectateTeam = Team.derelict;

    /** Эффект при входе на сервер. */
    public static final Effect joinEffect = Fx.greenBomb;
    /** Эффект при выходе с сервера. */
    public static final Effect leaveEffect = Fx.greenLaserCharge;
    /** Эффект при движении игрока. */
    public static final Effect moveEffect = Fx.freezing;

    public static final ObjectMap<Team, Seq<String>> votesSurrender = new ObjectMap<>();
    public static final ObjectMap<String, Timekeeper> nominateCooldowns = new ObjectMap<>(), voteKickCooldowns = new ObjectMap<>(), loginCooldowns = new ObjectMap<>();
    public static final ObjectMap<String, Team> activeSpectatingPlayers = new ObjectMap<>();

    public static final Seq<Language> translatorLanguages = new Seq<>();

    public static final ObjectMap<Message, String> loginWaiting = new ObjectMap<>();

    public static final Seq<String> votesRtv = new Seq<>(), votesVnw = new Seq<>(), activeHistoryPlayers = new Seq<>();

    public static final Seq<Gamemode> defaultModes = Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower);

    public static final Interval interval = new Interval();

    public static final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES).setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();

    /** Блоки, которые опасно строить рядом с ядром. */
    public static final ObjectMap<Block, Boolp> dangerousBuildBlocks = new ObjectMap<>();
    /** Блоки, в которые опасно переносить конкретные ресурсы. */
    public static final ObjectMap<Block, Item> dangerousDepositBlocks = new ObjectMap<>();

    public static final char[] rotateIcons = {Iconc.right, Iconc.up, Iconc.left, Iconc.down};

    /** Время непрерывной работы сервера. */
    public static int serverUpTime = 0;

    /** Время, проведенное на текущей карте. */
    public static int mapPlayTime = 0;

    /** Могут ли игроки голосовать в данный момент. */
    public static boolean canVote = false;

    /** База данных Jedis. */
    public static JedisPool jedisPool;

    /** Конфигурация сервера. */
    public static PluginConfig config;

    public static HistorySeq[][] history;

    public static VoteSession currentVote;
    public static VoteKickSession currentVoteKick;

    public static CommandHandler clientCommands, serverCommands, discordCommands;

    public static ReusableByteOutStream writeBuffer;
    public static Writes outputBuffer;

    public static boolean alertsEnabled() {
        return defaultModes.contains(config.mode);
    }

    public static boolean historyEnabled() {
        return defaultModes.contains(config.mode);
    }
}
