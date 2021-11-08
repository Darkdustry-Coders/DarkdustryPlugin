package pandorum.discord;

import arc.util.Log;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.rest.util.Color;
import pandorum.Misc;
import pandorum.PandorumPlugin;
import pandorum.comp.Authme;

import java.util.Objects;

public class BotMain {

    public static DiscordClient bot;
    public static GatewayDiscordClient client;
    public static BotHandler listener;

    public static final Color normalColor = Color.YELLOW;
    public static final Color successColor = Color.GREEN;
    public static final Color errorColor = Color.RED;

    public static void start() {
        bot = DiscordClient.create(PandorumPlugin.config.DiscordBotToken);
        client = bot.login().block();

        client.on(MessageCreateEvent.class).subscribe(event -> {
            Message msg = event.getMessage();
            BotHandler.handler.handleMessage(msg.getContent(), msg);

            if (Objects.equals(msg.getChannel().block(), BotHandler.botChannel) && !msg.getAuthor().get().isBot() && !msg.getContent().startsWith(BotHandler.prefix) && msg.getContent().length() > 0 && msg.getContent().length() < 100) {
                Misc.sendToChat("events.discord-message", msg.getAuthorAsMember().block().getDisplayName(), msg.getContent());
                Log.info("[Discord]@: @", msg.getAuthorAsMember().block().getDisplayName(), msg.getContent());
            }
        });

        client.on(ButtonInteractionEvent.class).subscribe(event -> {
            Message msg = event.getMessage().get();
            String button = event.getCustomId();
            if (BotHandler.waiting.containsKey(msg)) {
                switch (button) {
                    case "confirm" -> {
                        BotHandler.text(msg, "**Запрос был подтвержден** " + event.getInteraction().getMember().get().getDisplayName());
                        msg.delete().block();
                        Authme.confirm(BotHandler.waiting.get(msg));
                    }
                    case "deny" -> {
                        BotHandler.text(msg, "**Запрос был отклонен** " + event.getInteraction().getMember().get().getDisplayName());
                        msg.delete().block();
                        Authme.deny(BotHandler.waiting.get(msg));
                    }
                }
            }
        });

        listener = new BotHandler();
        Log.info("Бот успешно запущен...");
    }
}
