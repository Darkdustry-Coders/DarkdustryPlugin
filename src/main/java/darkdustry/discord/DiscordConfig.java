package darkdustry.discord;

import arc.struct.ObjectMap;
import arc.util.Log;
import useful.ConfigLoader;

import static darkdustry.PluginVars.*;
import static mindustry.Vars.*;

public class DiscordConfig {

    /** Конфигурация discord-бота. */
    public static DiscordConfig discordConfig;

    public static void load() {
        discordConfig = ConfigLoader.load(DiscordConfig.class, discordConfigFile);
        Log.info("Discord Config loaded. (@)", dataDirectory.child(discordConfigFile).absolutePath());
    }

    /** Токен бота, привязанного к серверу. */
    public String token = "token";

    /** Токен бота, привязанного к серверу. */
    public String prefix = "prefix";

    /** ID сервера в Discord, к которому привязан бот. */
    public long botGuildId = 0L;

    /** ID канала в Discord, куда отправляются баны. */
    public long banChannelId = 0L;

    /** ID канала в Discord, куда отправляются подтверждения для администраторов. */
    public long adminChannelId = 0L;

    /** ID роли администраторов в Discord. */
    public long adminRoleId = 0L;

    /** ID роли картоделов в Discord. */
    public long mapReviewerRoleId = 0L;

    /** ID каналов, привязанных к серверам. */
    public ObjectMap<String, Long> serverToChannel = ObjectMap.of("server", 0L);
}