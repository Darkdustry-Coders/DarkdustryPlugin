package pandorum.discord;

import arc.util.ArcRuntimeException;
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

    public static final Color normalColor = Color.ORANGE;
    public static final Color successColor = Color.GREEN;
    public static final Color errorColor = Color.RED;

    public static void start() {
        bot = DiscordClient.create(PandorumPlugin.config.DiscordBotToken);
        client = bot.login().block();

        if (client == null) throw new ArcRuntimeException("Не удалось запустить бота. Выключаю сервер...");

        client.on(MessageCreateEvent.class).subscribe(event -> {
            Message msg = event.getMessage();
            BotHandler.handler.handleMessage(msg.getContent(), msg);

            if (Objects.equals(msg.getChannel().block(), BotHandler.botChannel) && !msg.getAuthor().get().isBot() && !msg.getContent().startsWith(BotHandler.prefix) && msg.getContent().length() > 0 && msg.getContent().length() < 100) {
                Misc.sendToChat("events.discord-message", Objects.requireNonNull(msg.getAuthorAsMember().block()).getDisplayName(), msg.getContent());
                Log.info("[Discord]@: @", Objects.requireNonNull(msg.getAuthorAsMember().block()).getDisplayName(), msg.getContent());
            }
        });

        client.on(ButtonInteractionEvent.class).subscribe(event -> {
            Message msg = event.getMessage().get();
            String button = event.getCustomId();
            if (Authme.loginWaiting.containsKey(msg)) {
                switch (button) {
                    case "confirm" -> {
                        BotHandler.text(msg, "**Запрос был подтвержден** " + event.getInteraction().getMember().get().getDisplayName());
                        msg.delete().block();
                        Authme.confirm(Authme.loginWaiting.get(msg));
                        Authme.loginWaiting.remove(msg);
                    }
                    case "deny" -> {
                        BotHandler.text(msg, "**Запрос был отклонен** " + event.getInteraction().getMember().get().getDisplayName());
                        msg.delete().block();
                        Authme.deny(Authme.loginWaiting.get(msg));
                        Authme.loginWaiting.remove(msg);
                    }
                }
            }
        });

        BotHandler.init();
        Log.info("[Darkdustry] Бот успешно запущен...");
    }
}
