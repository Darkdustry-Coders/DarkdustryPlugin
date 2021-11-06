package pandorum.discord;

import arc.util.Log;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.message.Message;
import org.javacord.api.interaction.MessageComponentInteraction;
import pandorum.Misc;
import pandorum.PandorumPlugin;
import pandorum.comp.Authme;

import java.awt.*;

public class BotMain {

    public static DiscordApi bot;
    public static BotHandler listener;

    public static final Color normalColor = Color.decode("#FAB462");
    public static final Color successColor = Color.decode("#00FF00");
    public static final Color errorColor = Color.decode("#ff3838");

    public static void run() {
        bot = new DiscordApiBuilder()
                .setToken(PandorumPlugin.config.DiscordBotToken)
                .login().join();

        bot.setAutomaticMessageCacheCleanupEnabled(true);

        bot.addMessageCreateListener(event -> {
            Message msg = event.getMessage();
            BotHandler.handler.handleMessage(msg.getContent(), msg);

            if (msg.getChannel().equals(BotHandler.botChannel) && !msg.getAuthor().isBotUser() && !msg.getContent().startsWith(BotHandler.prefix) && msg.getContent().length() > 0 && msg.getContent().length() < 100) {
                Misc.sendToChat("events.discord-message", msg.getAuthor().getDisplayName(), msg.getContent());
                Log.info("[Discord]@: @", msg.getAuthor().getDisplayName(), msg.getContent());
            }
        });

        bot.addMessageComponentCreateListener(event -> {
            MessageComponentInteraction interaction = event.getMessageComponentInteraction();
            String button = interaction.getCustomId();
            Message msg = interaction.getMessage().get();
            if (BotHandler.waiting.containsKey(msg)) {
                switch (button) {
                    case "confirm" -> {
                        BotHandler.text(msg, "**Запрос был подтвержден** " + interaction.getUser().getDisplayName(BotHandler.server));
                        msg.delete().join();
                        Authme.confirm(BotHandler.waiting.get(msg));
                    }
                    case "deny" -> {
                        BotHandler.text(msg, "**Запрос был отклонен** " + interaction.getUser().getDisplayName(BotHandler.server));
                        msg.delete().join();
                        Authme.deny(BotHandler.waiting.get(msg));
                    }
                }
            }
        });

        listener = new BotHandler();
        Log.info("Бот успешно запущен...");
    }
}
