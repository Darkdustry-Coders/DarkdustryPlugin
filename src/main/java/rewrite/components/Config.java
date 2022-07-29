package rewrite.components;

public class Config {

    /** IP адрес Хаба. */
    public String hubIp = "darkdustry.tk";

    /** Порт Хаба. */
    public int hubPort = 6567;

    /** Режим игры на сервере. */
    public Gamemode mode = Gamemode.survival;

    /** Токен бота, привязанного к серверу. */
    public String discordBotToken = "token";

    /** Префикс бота, привязанного к серверу. */
    public String discordBotPrefix = "prefix";

    /** ID сервера в Discord. */
    public long discordGuildId = 0L;

    /** ID канала в Discord, куда отправляются все сообщения. */
    public long discordBotChannelId = 0L;

    /** ID канала в Discord, куда отправляются подтверждения для администраторов. */
    public long discordAdminChannelId = 0L;

    /** ID роли администраторов в Discord. */
    public long discordAdminRoleId = 0L;

    /** Ключ API для переводчика чата. */
    public String translatorApiKey = "key";

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
