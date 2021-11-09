package pandorum.comp;

import arc.util.Strings;
import mindustry.Vars;
import pandorum.struct.Tuple2;

public class Config {

    public int alertDistance = 150;

    /** Необходимое количество игроков для успешного завершения голосования. */
    public float voteRatio = 0.6f;

    /** Ёмкость массива, хранящего информацию о действиях с тайлом. Может сильно влиять на трату ОЗУ */
    public int historyLimit = 6;

    /** Время, через которое запись в истории тайла будет удалена. По умолчанию 30 минут. Записывается в миллисекундах */
    public long expireDelay = 1800000;

    /** Время голосования через /nominate. В секундах */
    public float voteDuration = 150f;

    /** Время голосования через /votekick. В секундах */
    public float votekickDuration = 40f;

    public String hubIp = "darkdustry.ml:6567";

    public Gamemode mode = Gamemode.survival;

    public String DiscordBotToken = "token";

    public Long DiscordChannelID = 0L;

    public String prefix = "prefix";

    public Tuple2<String, Integer> hubIp() {
        String ip = hubIp;
        int port = Vars.port;
        if (ip.contains(":") && Strings.canParsePositiveInt(ip.split(":")[1])) {
            String[] parts = ip.split(":");
            ip = parts[0];
            port = Strings.parseInt(parts[1]);
        }
        return Tuple2.of(ip, port);
    }

    public enum Gamemode {
        survival,
        attack,
        sandbox,
        hexed,
        pvp,
        castle,
        tower,
        hub,
        siege,
    }
}
