package pandorum.components;

import arc.struct.Seq;

public class Config {

    /**
     * IP адрес хаба. Порт - 6567 (по умолчанию).
     */
    public String hubIp = "darkdustry.ml";

    /**
     * Режим игры на сервере.
     */
    public Gamemode mode = Gamemode.survival;

    /**
     * Токен бота, привязанного к серверу.
     */
    public String discordBotToken = "token";

    /**
     * Префикс бота, привязанного к серверу.
     */
    public String discordBotPrefix = "prefix";

    /**
     * ID сервера в Discord.
     */
    public long discordGuildID = 0L;

    /**
     * ID канала в Discord, куда отправляются все сообщения.
     */
    public long discordBotChannelID = 0L;

    /**
     * ID канала в Discord, куда отправляются подтверждения для администраторов.
     */
    public long discordAdminChannelID = 0L;

    /**
     * ID роли администраторов в Discord.
     */
    public long discordAdminRoleID = 0L;

    public boolean historyEnabled() {
        return Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower).contains(mode);
    }

    public boolean alertsEnabled() {
        return Seq.with(Gamemode.attack, Gamemode.pvp, Gamemode.sandbox, Gamemode.survival, Gamemode.tower).contains(mode);
    }

    public enum Gamemode {
        attack,
        castle,
        crawler,
        hexed,
        hub,
        pvp,
        sandbox,
        survival,
        tower
    }
}
