package pandorum.discord;

import arc.util.CommandHandler;
import arc.util.Log;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.MessageComponentInteraction;
import org.javacord.api.listener.message.MessageCreateListener;
import pandorum.Misc;
import pandorum.PandorumPlugin;
import pandorum.comp.Authme;

import java.awt.*;

public class BotMain implements MessageCreateListener {

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

        bot.addMessageCreateListener(new BotMain());

        bot.addMessageComponentCreateListener(event -> {
            MessageComponentInteraction interaction = event.getMessageComponentInteraction();
            String button = interaction.getCustomId();
            Message msg = interaction.getMessage().get();
            if (BotHandler.waiting.containsKey(msg)) {
                switch (button) {
                    case "confirm" -> {
                        BotHandler.text(msg, "**Запрос был подтвержден** " + interaction.getUser().getDisplayName(BotHandler.server));
                        msg.delete();
                        Authme.confirm(BotHandler.waiting.get(msg));
                    }
                    case "deny" -> {
                        BotHandler.text(msg, "**Запрос был отклонен** " + interaction.getUser().getDisplayName(BotHandler.server));
                        msg.delete();
                        Authme.deny(BotHandler.waiting.get(msg));
                    }
                }
            }
        });

        listener = new BotHandler();
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        Message msg = event.getMessage();
        BotHandler.handler.handleMessage(msg.getContent(), msg);

        if (msg.getChannel().equals(BotHandler.botChannel) && !msg.getAuthor().isBotUser() && !msg.getContent().startsWith(BotHandler.prefix)) {
            if (msg.getContent().length() > 100) {
                BotHandler.err(msg, "Ошибка", "Длина сообщения не может быть больше 100 символов!");
                return;
            }
            Misc.sendToChat("events.discord-message", msg.getAuthor().getDisplayName(), msg.getContent());
            Log.info("[Discord]@: @", msg.getAuthor().getDisplayName(), msg.getContent());
        }
    }
}
