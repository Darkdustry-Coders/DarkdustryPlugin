package rewrite;

import arc.struct.OrderedMap;
import arc.struct.Seq;
import arc.util.CommandHandler;
import net.dv8tion.jda.api.entities.Message;
import rewrite.components.Config;
import rewrite.components.Config.Gamemode;

import static rewrite.components.Config.Gamemode.*;

public class PluginVars {

    /** Максимальное количество записей истории на один тайл. */
    public static final int maxHistoryCapacity = 6;

    /** Список uuid игроков, просматривающих историю в данный момент. */
    public static final Seq<String> activeHistory = new Seq<>();

    /** Список uuid игроков, ожидающих авторизацию. */
    public static final OrderedMap<Message, String> loginWaiting = new OrderedMap<>();

    /** Ссылка на наш Discord сервер */
    public static final String discordServerUrl = "discord.gg/uDPPVN6V3E";

    /** Ссылка на API переводчика. */
    public static final String translatorApiUrl = "https://translo.p.rapidapi.com/api/v3/translate", translatorApiHost = "translo.p.rapidapi.com";

    /** Список всех языков переводчика. */
    public static final OrderedMap<String, String> translatorLanguages = new OrderedMap<>();

    /** Словарь для перевода локалей миндастри в локали переводчика */
    public static final OrderedMap<String, String> mindustry2Api = new OrderedMap<>();

    /** Язык по умолчанию. */
    public static final String defaultLanguage = "en";

    /** Название файла, в котором хранится конфигурация сервера. */
    public static final String configFileName = "config.json";

    /** Режимы, в которых будут доступны стандартные команды. */
    public static final Seq<Gamemode> defaultModes = Seq.with(attack, pvp, sandbox, survival, tower);

    /** Конфигурация сервера. */
    public static Config config;

    /** Текстовый список всех предметов, юнитов и команд. */
    public static String items, units, teams;

    /** Кэшированные хэндлеры, которые использовались для регистрации команд. */
    public static CommandHandler clientCommands, serverCommands, discordCommands;
}
