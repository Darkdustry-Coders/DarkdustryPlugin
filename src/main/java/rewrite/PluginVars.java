package rewrite;

import arc.struct.Seq;
import rewrite.components.Config;
import rewrite.components.Config.Gamemode;

import static rewrite.components.Config.Gamemode.*;

public class PluginVars {

    /** Язык по умолчанию. */
    public static final String defaultLanguage = "en";

    /** Название файла, в котором хранится конфигурация сервера. */
    public static final String configFileName = "config.json";

    public static final Seq<Gamemode> defaultModes = Seq.with(attack, pvp, sandbox, survival, tower);

    /** Конфигурация сервера. */
    public static Config config;
}
