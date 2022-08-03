package pandorum.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pandorum.features.Authme;

import java.awt.*;

import static pandorum.PluginVars.loginWaiting;
import static pandorum.discord.Bot.isAdmin;

public class ButtonListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!loginWaiting.containsKey(event.getMessage())) return;

        if (!isAdmin(event.getMember())) {
            event.replyEmbeds(new EmbedBuilder().setColor(Color.red).setTitle(":no_entry_sign: Взаимодействовать с запросами могут только админы.").build()).setEphemeral(true).queue();
            return;
        }

        switch (event.getComponentId()) {
            case "authme.confirm" -> Authme.confirm(event);
            case "authme.deny" -> Authme.deny(event);
            case "authme.info" -> Authme.info(event);
        }
    }
}
