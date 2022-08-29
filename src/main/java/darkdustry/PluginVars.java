package darkdustry;

import arc.struct.*;
import arc.util.CommandHandler;
import com.google.gson.*;
import darkdustry.components.Config;
import darkdustry.components.Config.Gamemode;
import darkdustry.features.votes.*;
import mindustry.core.Version;
import net.dv8tion.jda.api.entities.Message;

import static darkdustry.components.Config.Gamemode.*;
import static mindustry.Vars.tilesize;

public class PluginVars {

    /** Необходимое количество игроков для успешного завершения голосования. */
    public static final float voteRatio = 0.5f;

    /** Время, после которого голосование автоматически завершится. */
    public static final float voteDuration = 50f;

    /** Время, на которое игрок будет выгнан голосованием или через команду. */
    public static final long kickDuration = 2700000L;

    /** Время, после которого будет загружена карта. */
    public static final int mapLoadDelay = 10;

    /** Расстояние до ядра, в котором отслеживаются опасные блоки. Интервал оповещений об опасных блоках. */
    public static final int alertsDistance = 16 * tilesize, alertsTimer = 3;

    /** Максимальное количество выдаваемых ресурсов. */
    public static final int maxGiveAmount = 100000;

    /** Максимальное количество создаваемых юнитов. */
    public static final int maxSpawnAmount = 25;

    /** Максимальное количество пропущенных волн. */
    public static final int maxVnwAmount = 10;

    /** Максимальная площадь для заливки. */
    public static final int maxFillAmount = 512;

    /** Количество команд/игроков/карт/сохранений на одной странице списка. */
    public static final int maxPerPage = 8;

    /** Максимальное количество записей истории на один тайл. */
    public static final int maxHistoryCapacity = 6;

    /** Максимально допустимое количество игроков с одинаковыми IP адресами. */
    public static final int maxIdenticalIPs = 3;

    /** Версия Mindustry, запущенная на сервере. */
    public static final int mindustryVersion = Version.build;

    /** Список uuid игроков, просматривающих историю в данный момент. */
    public static final Seq<String> activeHistory = new Seq<>();

    /** Список uuid игроков, ожидающих авторизацию. */
    public static final OrderedMap<Message, String> loginWaiting = new OrderedMap<>();

    /** Ссылка на наш Discord сервер */
    public static final String discordServerUrl = "https://discord.gg/uDPPVN6V3E";

    /** Ссылка на API переводчика. */
    public static final String translatorApiUrl = "https://translo.p.rapidapi.com/api/v3/translate", translatorApiHost = "translo.p.rapidapi.com";

    /** Список всех языков переводчика. */
    public static final OrderedMap<String, String> translatorLanguages = new OrderedMap<>();

    /** Словарь для перевода локалей миндастри в локали переводчика. */
    public static final OrderedMap<String, String> mindustry2Api = new OrderedMap<>();

    /** Язык по умолчанию. Язык Discord сервера. */
    public static final String defaultLanguage = "en", discordLanguage = "ru";

    /** Название файла, в котором хранится конфигурация сервера. */
    public static final String configFileName = "config.json";

    /** Используется для считывания и записи Json объектов. */
    public static final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES).setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();

    /** Режимы, в которых будут доступны стандартные команды. */
    public static final Seq<Gamemode> defaultModes = Seq.with(attack, pvp, sandbox, survival, tower);

    /** Текущее голосование. */
    public static VoteSession vote;

    /** Текущее голосование за кик игрока. */
    public static VoteKick voteKick;

    /** Конфигурация сервера. */
    public static Config config;

    /** Текстовый список всех предметов, юнитов и команд. */
    public static String items, units, teams;

    /** Точное время запуска сервера и загрузки последней карты. */
    public static long serverLoadTime, mapLoadTime;

    /** Кэшированные хэндлеры, которые использовались для регистрации команд. */
    public static CommandHandler clientCommands, serverCommands;
}
