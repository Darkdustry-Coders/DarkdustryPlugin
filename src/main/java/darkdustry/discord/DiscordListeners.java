package darkdustry.discord;

import darkdustry.DarkdustryPlugin;
import darkdustry.commands.DiscordCommands;
import darkdustry.features.Authme;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import javax.annotation.Nonnull;

import static arc.Core.app;
import static darkdustry.PluginVars.loginWaiting;
import static darkdustry.discord.Bot.*;
import static darkdustry.listeners.NetHandlers.waitingEditBans;
import static darkdustry.utils.Checks.notAdmin;
import static java.util.Objects.requireNonNull;

public class DiscordListeners extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getMessage().getContentDisplay().isEmpty()) return;
        if (event.isFromGuild() && event.getChannel() == botChannel) // можно кнч объединить в один if, но он будет просто огромным
            app.post(() -> sendMessageToGame(requireNonNull(event.getMember()), event.getMessage()));
    }

    @Override
    public void onSelectMenuInteraction(SelectMenuInteractionEvent event) {
        if (!loginWaiting.containsKey(event.getMessage()) || notAdmin(event)) return;

        if (event.getComponentId().equals("authme")) {
            switch (event.getValues().get(0)) {
                case "authme.confirm" -> Authme.confirm(event);
                case "authme.deny" -> Authme.deny(event);
                case "authme.info" -> Authme.information(event);
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        DarkdustryPlugin.discord("@ использует /@", requireNonNull(event.getMember()).getEffectiveName(), event.getName());
        DiscordCommands.commands.get(event.getName()).get(event);
    }
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals("editban")) {

            if (!event.getMember().getRoles().contains(adminRole)) return;

            TextInput reason = TextInput.create("reason", "Причина", TextInputStyle.SHORT)
                    .setPlaceholder("Причина бана")
                    .setRequiredRange(3, 200)
                    .build();

            TextInput date = TextInput.create("date", "Дата разбана", TextInputStyle.SHORT)
                    .setPlaceholder("Дата разбана (Желательно в формате dd/mm/yyyy)")
                    .setRequiredRange(3, 200)
                    .build();

            Modal modal = Modal.create("editban", "Редактировать бан")
                    .addActionRows(ActionRow.of(reason), ActionRow.of(date))
                    .build();

            event.replyModal(modal).queue();
        }
    }
    @Override
    public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
        if (event.getModalId().equals("editban")) {
            String reason = event.getValue("reason").getAsString();
            String date = event.getValue("date").getAsString();

            event.getMessage().editMessageEmbeds(waitingEditBans.get(event.getMessage().getIdLong())
                    .addField("Причина", reason, false)
                    .addField("Дата разбана", date, false)
                    .build()).setActionRow(Button.primary("editban", "Редактировать бан").asDisabled()).queue();

            waitingEditBans.removeKey(event.getMessage().getIdLong());
        }
    }
}
