package pandorum.comp;

import arc.struct.Seq;
import arc.util.Strings;
import mindustry.Vars;
import pandorum.struct.Tuple2;

public class Config {
    /** Необходимое количество игроков для успешного завершения голосования */
    public float voteRatio = 0.6f;

    /** Ёмкость массива, хранящего информацию о действиях с тайлом. Может сильно влиять на трату ОЗУ */
    public int historyLimit = 6;

    /** Время, через которое запись в истории тайла будет удалена. По умолчанию 30 минут. Записывается в миллисекундах */
    public long expireDelay = 1800000L;

    /** Расстояние до ядер, в котором отслеживаются ториевые реакторы. */
    public int alertsDistance = 120;

    /** Время голосования через /nominate. В секундах */
    public float voteDuration = 150f;

    /** Время голосования через /votekick. В секундах */
    public float votekickDuration = 40f;

    /** Время, на которое игрок будет выгнан голосованием. В миллисекундах */
    public long kickDuration = 2700000L;

    /** IP адрес хаба. */
    public String hubIp = "darkdustry.ml";

    /** Режим игры на сервере. */
    public Gamemode mode = Gamemode.survival;

    /** Токен бота, привязанного к серверу. Если его не указать, сервер не запустится! */
    public String discordBotToken = "token";

    /** ID канала в Discord, куда отправляются все сообщения */
    public long discordChannelID = 0L;

    /** ID канала в Discord, куда отправляются подтверждения для администраторов */
    public long discordAdminChannelID = 0L;

    /** Префикс бота, привязанного к серверу */
    public String prefix = "prefix";

    /** Ключ Anti-VPN API. */
    public String antiVPNAPIToken = "w7j425-826177-597253-3134u9";

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

    public boolean historyEnabled() {
        return Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.siege, Gamemode.survival, Gamemode.tower).contains(mode);
    }

    public boolean alertsEnabled() {
        return alertsDistance > 0 && Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.siege, Gamemode.survival, Gamemode.tower).contains(mode);
    }

    public enum Gamemode {
        attack,
        castle,
        crawler,
        hexed,
        hub,
        pvp,
        sandbox,
        siege,
        survival,
        tower
    }
}
