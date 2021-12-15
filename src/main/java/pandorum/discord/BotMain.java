package pandorum.discord;

import arc.util.CommandHandler.CommandResponse;
import arc.util.CommandHandler.ResponseType;
import arc.util.Log;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.entity.Message;
import discord4j.rest.request.RouteMatcher;
import discord4j.rest.response.ResponseFunction;
import discord4j.rest.route.Routes;
import discord4j.rest.util.AllowedMentions;
import discord4j.rest.util.Color;
import pandorum.PandorumPlugin;
import pandorum.comp.Authme;

import java.util.Objects;

import static pandorum.Misc.sendToChat;

public class BotMain {

    public static final Color normalColor = Color.ORANGE;
    public static final Color successColor = Color.GREEN;
    public static final Color errorColor = Color.RED;

    public static DiscordClient bot;
    public static GatewayDiscordClient client;

    public static void start() {
        try {
            bot = DiscordClientBuilder.create(PandorumPlugin.config.discordBotToken)
                    .onClientResponse(ResponseFunction.emptyIfNotFound())
                    .onClientResponse(ResponseFunction.emptyOnErrorStatus(RouteMatcher.route(Routes.REACTION_CREATE), 400))
                    .setDefaultAllowedMentions(AllowedMentions.suppressAll()).build();

            client = bot.login().block();

            client.on(MessageCreateEvent.class).subscribe(event -> {
                Message msg = event.getMessage();
                CommandResponse response = BotHandler.handler.handleMessage(msg.getContent(), msg);
                if (response.type == ResponseType.fewArguments || response.type == ResponseType.manyArguments) {
                    BotHandler.err(msg.getChannel().block(), "Неверное количество аргументов.", "Использование : **@@** @", BotHandler.handler.getPrefix(), response.command.text, response.command.paramText);
                    return;
                }

                if (Objects.equals(msg.getChannel().block(), BotHandler.botChannel) && !msg.getAuthor().get().isBot() && msg.getContent().length() > 0) {
                    sendToChat("events.discord.chat", Objects.requireNonNull(msg.getAuthorAsMember().block()).getDisplayName(), msg.getContent());
                    Log.info("[Discord] @: @", Objects.requireNonNull(msg.getAuthorAsMember().block()).getDisplayName(), msg.getContent());
                }
            }, e -> {});

            client.on(ButtonInteractionEvent.class).subscribe(event -> {
                Message msg = event.getMessage().get();
                Interaction interaction = event.getInteraction();
                if (Authme.loginWaiting.containsKey(msg)) {
                    switch (event.getCustomId()) {
                        case "confirm" -> {
                            BotHandler.text(Objects.requireNonNull(msg.getChannel().block()), "Запрос игрока **@** был подтвержден **@**", Authme.loginWaiting.get(msg).getInfo().lastName, interaction.getUser().getUsername());
                            Authme.confirm(Authme.loginWaiting.get(msg));
                        }
                        case "deny" -> {
                            BotHandler.text(Objects.requireNonNull(msg.getChannel().block()), "Запрос игрока **@** был отклонен **@**", Authme.loginWaiting.get(msg).getInfo().lastName, interaction.getUser().getUsername());
                            Authme.deny(Authme.loginWaiting.get(msg));
                        }
                    }
                    Authme.loginWaiting.remove(msg);
                    msg.delete().block();
                }
            }, e -> {});

            BotHandler.init();
            Log.info("[Darkdustry] Бот успешно запущен...");
        } catch (Exception e) {
            Log.err("[Darkdustry] Ошибка запуска бота...");
            throw new RuntimeException(e);
        }
    }
}
