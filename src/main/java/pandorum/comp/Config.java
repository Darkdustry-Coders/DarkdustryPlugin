package pandorum.comp;

import java.util.Set;

import arc.util.Strings;
import pandorum.struct.Tuple2;

public class Config{

    public int alertDistance = 300;

    /** Необходимое количество игроков для успешного завершения голосования. */
    public float voteRatio = 0.6f;

    /** Ёмкость массива хранимого информацию о действиях с тайлом. Может сильно влиять на трату ОЗУ */
    public int historyLimit = 8;

    /** Время через которое запись в истории тайла будет удалена. По умолчанию 30 минут. Записывается в миллисекундах */
    public long expireDelay = 1800000;

    /** Время голосования. В секундах */
    public float voteDuration = 150f;

    public String hubIp = "darkdustry.ml:6567";

    public PluginType type = PluginType.def;

    public Set<String> bannedNames = Set.of(
            "IGGGAMES",
            "CODEX",
            "VALVE",
            "tuttop",
            "IgruhaOrg"
    );

    public Tuple2<String, Integer> parseIp(){
        String ip = hubIp;
        int port = 6567;
        String[] parts = ip.split(":");
        if(ip.contains(":") && Strings.canParsePositiveInt(parts[1])){
            ip = parts[0];
            port = Strings.parseInt(parts[1]);
        }
        return Tuple2.of(ip, port);
    }

    public enum PluginType{

        /** Тип для серверов с режимом выживания или атаки */
        def,

        /** Тип для PvP серверов */
        pvp,

        /** Тип для серверов с режимом песочницы */
        sand,

        /** Тип для сервера анархии */
        anarchy,

        /** Тип для любого другого сервера */
        other
    }
}
