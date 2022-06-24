package pandorum.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import pandorum.features.Authme;

import java.awt.*;

import static pandorum.PluginVars.loginWaiting;
import static pandorum.util.PlayerUtils.isAdmin;

public class ButtonListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!loginWaiting.containsKey(event.getMessage())) return;

        if (!isAdmin(event.getMember())) {
            event.replyEmbeds(new EmbedBuilder().setColor(Color.red).setTitle(":no_entry_sign: Взаимодействовать с запросами могут только админы.").build()).setEphemeral(true).queue();
            return;
        }

        Context context = new Context(event);

        switch (event.getComponentId()) {
            case "admin.confirm" -> Authme.confirm(context);
            case "admin.deny" -> Authme.deny(context);
            case "admin.info" -> Authme.info(context, event);
        }
    }
}
