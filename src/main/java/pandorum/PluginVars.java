package pandorum;

import arc.struct.ObjectMap;
import arc.struct.OrderedMap;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Timekeeper;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.game.Team;
import net.dv8tion.jda.api.entities.Message;
import pandorum.components.Gamemode;
import pandorum.components.PluginConfig;
import pandorum.vote.VoteKickSession;
import pandorum.vote.VoteSession;
import redis.clients.jedis.JedisPool;

import static mindustry.Vars.tilesize;

// TODO если переменная используется только в одном классе и не является цифрой, вынести в этот класс?
public class PluginVars {

    /** IP адрес сервера. */
    public static final String serverIp = "darkdustry.tk";

    /** Максимальный размер заполняемого пространства через /fill. */
    public static final int maxFillSize = 512;

    /** Максимальное количество выданного ресурса через /give. */
    public static final int maxGiveAmount = 1000000;
    /** Максимальное количество заспавненных юнитов через /spawn. */
    public static final int maxSpawnAmount = 25;

    // TODO вынести в Cooldowns, переделать механику кулдаунов?
    /** Время кулдауна для команды /nominate. В секундах. */
    public static final int nominateCooldownTime = 150;
    /** Время кулдауна для команды /votekick. В секундах. */
    public static final int voteKickCooldownTime = 300;
    /** Время кулдауна для команды /login. В секундах. */
    public static final int loginCooldownTime = 900;
    /** Время кулдауна для команды /sync. В секундах. */
    public static final int syncCooldownTime = 15;

    // TODO разные ratio ля разных типов голосования?
    /** Необходимое количество игроков для успешного завершения голосования. */
    public static final float voteRatio = 0.6f;

    /** Максимальное количество записей истории на один тайл. */
    public static final int maxTileHistoryCapacity = 6;

    /** Расстояние до ядер, в котором отслеживаются опасные блоки. */
    public static final int alertsDistance = 8 * tilesize;

    /** Интервал оповещений о постройке опасных блоков. В секундах. */
    public static final float alertsTimer = 2.5f;

    /** Время голосования через /nominate. В секундах. */
    public static final float voteDuration = 120f;
    /** Время голосования через /votekick. В секундах. */
    public static final float voteKickDuration = 45f;
    /** Время, на которое игрок будет выгнан голосованием или через команду. В миллисекундах. */
    public static final long kickDuration = 2700000L;

    /** Язык по умолчанию. */
    public static final String defaultLanguage = "en";

    /** Ссылка на наш Discord сервер */
    public static final String discordServerUrl = "discord.gg/uDPPVN6V3E";

    public static final String translatorApiUrl = "https://translo.p.rapidapi.com/api/v3/translate", translatorApiHost = "translo.p.rapidapi.com";

    /** Название файла с конфигурацией. */
    public static final String configFileName = "config.json";

    /** Порт для подключения базы данных Jedis. */
    public static final int jedisPoolPort = 6379;

    /** Команда для наблюдателей. */
    public static final Team spectateTeam = Team.derelict;

    // TODO сделать кастомные эффекты каждому рангу
    /** Эффект при входе на сервер. */
    public static final Effect joinEffect = Fx.greenBomb;
    /** Эффект при выходе с сервера. */
    public static final Effect leaveEffect = Fx.greenLaserCharge;
    /** Эффект при движении игрока. */
    public static final Effect moveEffect = Fx.freezing;

    public static final ObjectMap<Team, Seq<String>> votesSurrender = new ObjectMap<>();

    // TODO вынести в Cooldowns, переделать механику кулдаунов? (сделать что-то похожее на это: https://github.com/TomtheCoder2/mainPlugin/blob/master/src/main/java/mindustry/plugin/utils/Cooldowns.java)
    public static final ObjectMap<String, Timekeeper> nominateCooldowns = new ObjectMap<>(), voteKickCooldowns = new ObjectMap<>(), loginCooldowns = new ObjectMap<>();

    public static final ObjectMap<String, Team> activeSpectatingPlayers = new ObjectMap<>();
    public static final ObjectMap<Message, String> loginWaiting = new ObjectMap<>();

    public static final OrderedMap<String, String> translatorLanguages = new OrderedMap<>();
    public static final OrderedMap<String, String> mindustryLocales2Api = new OrderedMap<>();

    public static final Seq<String> votesRtv = new Seq<>(), votesVnw = new Seq<>(), activeHistoryPlayers = new Seq<>();

    public static final Seq<Gamemode> defaultModes = Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower);

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

    public static VoteSession currentVote;
    public static VoteKickSession currentVoteKick;

    public static CommandHandler clientCommands, serverCommands, discordCommands;

    public static String itemsList, unitsList, teamsList;
}
