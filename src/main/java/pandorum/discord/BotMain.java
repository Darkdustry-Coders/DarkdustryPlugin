package pandorum.discord;

import arc.util.Log;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import pandorum.Misc;
import pandorum.PandorumPlugin;
import pandorum.comp.Authme;

import java.awt.*;

public class BotMain extends ListenerAdapter {

    public static JDA jda;
    public static BotHandler listener;

    public static final Color normalColor = Color.decode("#FAB462");
    public static final Color successColor = Color.decode("#00FF00");
    public static final Color errorColor = Color.decode("#ff3838");

    public static final long messageDeleteTime = 10000;

    public static void run() {
        try {
            jda = JDABuilder.createDefault(PandorumPlugin.config.DiscordBotToken).addEventListeners(new BotMain()).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        listener = new BotHandler();
        Log.info("Бот успешно запущен!");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        BotHandler.handler.handleMessage(msg.getContentRaw(), msg);

        if (msg.getChannel().equals(BotHandler.botChannel) && !msg.getAuthor().isBot() && !msg.getContentRaw().startsWith(BotHandler.prefix)) {
            if (msg.getContentRaw().length() > 100) {
                BotHandler.errDelete(msg, "Ошибка", "Длина сообщения не может быть больше 100 символов!");
                return;
            }
            Misc.sendToChat("events.discord-message", msg.getAuthor().getAsTag(), msg.getContentRaw());
            Log.info("[Discord]@: @", msg.getAuthor().getAsTag(), msg.getContentRaw());
        }
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (event.getChannel() == BotHandler.adminChannel && BotHandler.waiting.containsKey(event.getMessageIdLong())) {
            switch (event.getReactionEmote().getName()) {
                case "white_check_mark" -> {
                    Authme.addAdmin(BotHandler.waiting.get(event.getMessageIdLong()));
                    BotHandler.text(event.getChannel(), event.getUser().getName() + " подтвердил запрос");
                }
                case "x" -> {
                    Authme.ignoreRequest(BotHandler.waiting.get(event.getMessageIdLong()));
                    BotHandler.text(event.getChannel(), event.getUser().getName() + " отклонил запрос");
                }
            }
        }
    }
}
