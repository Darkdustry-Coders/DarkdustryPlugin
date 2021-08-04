package pandorum.comp;

import java.util.Set;

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

    public String hubIp = "darkdustry.ml";

    public int hubPort = 6567;

    public PluginType type = PluginType.def;

    public Set<String> bannedNames = Set.of(
            "IGGGAMES",
            "CODEX",
            "VALVE",
            "tuttop",
            "IgruhaOrg"
    );

    public enum PluginType{

        /** Тип для серверов с режимом выживания или атаки */
        def,

        /** Тип для PvP серверов */
        pvp,

        /** Тип для серверов с режимом песочницы */
        sand
    }
}
