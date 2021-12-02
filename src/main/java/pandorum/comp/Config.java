package pandorum.comp;

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

    /** Время голосования через /nominate. В секундах */
    public float voteDuration = 150f;

    /** Время голосования через /votekick. В секундах */
    public float votekickDuration = 40f;

    /** Время, на которое игрок будет выгнан голосованием. В миллисекундах */
    public long kickDuration = 2700000L;

    /** IP адрес хаба. */
    public String hubIp = "darkdustry.ml:6567";

    /** Режим игры на этом сервере. Влияет на доступные команды и не только */
    public Gamemode mode = Gamemode.survival;

    /** Токен бота, привязанного к серверу. Если его не указать, сервер не запустится! */
    public String DiscordBotToken = "token";

    /** ID канала в Discord, куда отправляются все сообщения */
    public long DiscordChannelID = 0L;

    /** ID канала в Discord, куда отправляются подтверждения для администраторов */
    public long DiscordAdminChannelID = 0L;

    /** Префикс бота, привязанного к серверу */
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
        attack,
        castle(false, false),
        crawler(false, false),
        hexed(false, false),
        hub(false, false),
        pvp(true),
        sandbox,
        siege(true),
        survival,
        tower;

        public boolean isPvP;
        public boolean isSimple;

        Gamemode(boolean isPvP, boolean isSimple) {
            this.isPvP = isPvP;
            this.isSimple = isSimple;
        }

        Gamemode(boolean isPvP) {
            this(isPvP, true);
        }

        Gamemode() {
            this(false, true);
        }
    }
}
