package darkdustry.discord;

import darkdustry.features.Authme;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.modals.Modal;

import static arc.Core.app;
import static darkdustry.PluginVars.loginWaiting;
import static darkdustry.discord.Bot.*;
import static darkdustry.utils.Checks.notAdmin;
import static net.dv8tion.jda.api.interactions.components.ActionRow.of;
import static net.dv8tion.jda.api.interactions.components.text.TextInputStyle.SHORT;

public class DiscordListeners extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || !event.isFromGuild() || event.getMessage().getContentRaw().isEmpty()) return;

        if (handleMessage(new Context(event))) return;

        if (event.isFromGuild() && event.getChannel() == botChannel)
            app.post(() -> sendMessageToGame(event.getMember(), event.getMessage()));
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (!loginWaiting.containsKey(event.getMessage()) || notAdmin(event)) return;

        if (event.getComponentId().equals("authme"))
            switch (event.getValues().get(0)) {
                case "authme.confirm" -> Authme.confirm(event);
                case "authme.deny" -> Authme.deny(event);
                case "authme.info" -> Authme.information(event);
            }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals("editban")) {
            if (notAdmin(event)) return;

            var reason = TextInput.create("reason", "Reason", SHORT)
                    .setPlaceholder("...")
                    .setRequiredRange(4, 128)
                    .build();

            var date = TextInput.create("date", "Unban Date", SHORT)
                    .setPlaceholder("...")
                    .setRequiredRange(4, 128)
                    .build();

            var modal = Modal.create("editban", "Edit Ban")
                    .addActionRows(of(reason), of(date))
                    .build();

            event.replyModal(modal).queue();
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getModalId().equals("editban")) {
            if (event.getMessage().getEmbeds().get(0).getFields().size() == 5) return;

            event.getMessage().editMessageEmbeds(new EmbedBuilder(event.getMessage().getEmbeds().get(0))
                    .setAuthor(event.getUser().getName(), event.getUser().getEffectiveAvatarUrl(), event.getUser().getEffectiveAvatarUrl())
                    .addField("Reason", event.getValue("reason").getAsString(), false)
                    .addField("Unban Date", event.getValue("date").getAsString(), false)
                    .build()).setActionRow(Button.primary("editban", "Edit Ban").asDisabled()).queue();

            event.reply("Successfully edited.").setEphemeral(true).queue();
        }
    }
}