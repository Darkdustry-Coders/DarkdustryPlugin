package pandorum.discord;

import arc.util.CommandHandler.CommandResponse;
import arc.util.CommandHandler.ResponseType;
import arc.util.Log;
import arc.util.Strings;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.rest.request.RouteMatcher;
import discord4j.rest.response.ResponseFunction;
import discord4j.rest.route.Routes;
import discord4j.rest.util.AllowedMentions;
import discord4j.rest.util.Color;
import mindustry.net.Administration.PlayerInfo;
import pandorum.PandorumPlugin;
import pandorum.comp.Authme;

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
                Member member = msg.getAuthorAsMember().block();
                CommandResponse response = BotHandler.handler.handleMessage(msg.getContent(), msg);
                if (response.type == ResponseType.fewArguments || response.type == ResponseType.manyArguments) {
                    BotHandler.err(msg.getChannel().block(), "Неверное количество аргументов.", "Использование : **@@** @", BotHandler.handler.getPrefix(), response.command.text, response.command.paramText);
                    return;
                }

                if (msg.getChannel().block() == BotHandler.botChannel && member != null && !member.isBot() && msg.getContent().length() > 0) {
                    sendToChat("events.discord.chat", member.getDisplayName(), msg.getContent());
                    Log.info("[Discord] @: @", member.getDisplayName(), msg.getContent());
                }
            }, e -> {});

            client.on(ButtonInteractionEvent.class).subscribe(event -> {
                Message msg = event.getMessage().get();
                Interaction interaction = event.getInteraction();
                if (Authme.loginWaiting.containsKey(msg)) {
                    switch (event.getCustomId()) {
                        case "confirm" -> {
                            BotHandler.text(msg.getChannel().block(), "Запрос игрока **@** был подтвержден **@**", Strings.stripColors(Authme.loginWaiting.get(msg).getInfo().lastName), interaction.getUser().getUsername());
                            Authme.confirm(Authme.loginWaiting.get(msg));
                            Authme.loginWaiting.remove(msg);
                            msg.delete().block();
                        }
                        case "deny" -> {
                            BotHandler.text(msg.getChannel().block(), "Запрос игрока **@** был отклонен **@**", Strings.stripColors(Authme.loginWaiting.get(msg).getInfo().lastName), interaction.getUser().getUsername());
                            Authme.deny(Authme.loginWaiting.get(msg));
                            Authme.loginWaiting.remove(msg);
                            msg.delete().block();
                        }
                        case "check" -> {
                            PlayerInfo info = Authme.loginWaiting.get(msg).getInfo();
                            event.reply(Strings.format("> Информация об игроке **@**\n\nID: @\nIP: @\n\nВошел на сервер: @ раз.\nБыл выгнан с сервера: @ раз\n\nВсе IP адреса: @\n\nВсе никнеймы: @", info.lastName, info.id, info.lastIP, info.timesJoined, info.timesKicked, info.ips, info.names)).withEphemeral(true).block();
                        }
                    }
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
