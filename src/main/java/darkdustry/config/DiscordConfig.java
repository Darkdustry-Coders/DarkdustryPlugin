package darkdustry.config;

import arc.struct.*;
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

    /** ID канала в Discord, куда отправляются баны. */
    public long banChannelID = 0L;

    /** ID канала в Discord, куда отправляются подтверждения для администраторов. */
    public long adminChannelID = 0L;

    /** ID канала в Discord, куда отправляются успешные голосования за кик. */
    public long votekickChannelID = 0L;

    /** ID ролей администраторов в Discord. */
    public Seq<Long> adminRoleIDs = Seq.with(0L);

    /** ID ролей картоделов в Discord. */
    public Seq<Long> mapReviewerRoleIDs = Seq.with(0L);

    /** ID каналов, привязанных к серверам. */
    public ObjectMap<String, Long> serverToChannel = ObjectMap.of("server", 0L);

    /** Verified user role */
    public long verifiedRoleID = 0L;

    public Seq<Long> blacklistedChannelIDs = Seq.with(0L);

    public long botsChannelID = 0L;
}