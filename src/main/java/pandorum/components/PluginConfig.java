package pandorum.components;

public class PluginConfig {

    // TODO IP:port, а не просто IP. Плюс нужен парсинг адреса
    /** IP адрес Хаба. Порт - 6567 (по умолчанию). */
    public String hubIp = "darkdustry.tk"; // TODO: "darkdustry.tk:6567"

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
}
