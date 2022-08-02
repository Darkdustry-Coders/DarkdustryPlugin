package rewrite;

import arc.struct.OrderedMap;
import arc.struct.Seq;
import rewrite.components.Config;
import rewrite.components.Config.Gamemode;

import static rewrite.components.Config.Gamemode.*;

public class PluginVars {

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

    /** Список всех предметов, юнитов и команд. */
    public static String items, units, teams;
}
