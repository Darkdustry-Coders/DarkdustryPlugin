package darkdustry.discord;

import arc.files.Fi;
import arc.func.Cons;
import arc.util.Strings;
import darkdustry.listeners.SocketEvents.EmbedResponse;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateFields.Field;
import discord4j.core.spec.*;
import discord4j.core.spec.EmbedCreateSpec.Builder;
import discord4j.core.spec.MessageCreateFields.File;
import discord4j.rest.util.Color;


public record MessageContext(Message message, Member member, MessageChannel channel) {

    public MessageCreateMono success(String title, String content, Object... values) {
        return success(embed -> embed.title(title).description(Strings.format(content, values)));
    }

    public MessageCreateMono success(Cons<Builder> setter) {
        return reply(embed -> {
            embed.color(Color.MEDIUM_SEA_GREEN);
            setter.get(embed);
        });
    }

    public MessageCreateMono error(String title, String content, Object... values) {
        return error(embed -> embed.title(title).description(Strings.format(content, values)));
    }

    public MessageCreateMono error(Cons<Builder> setter) {
        return reply(embed -> {
            embed.color(Color.CINNABAR);
            setter.get(embed);
        });
    }

    public MessageCreateMono info(String title, String content, Object... values) {
        return info(embed -> embed.title(title).description(Strings.format(content, values)));
    }

    public MessageCreateMono info(Cons<Builder> setter) {
        return reply(embed -> {
            embed.color(Color.SUMMER_SKY);
            setter.get(embed);
        });
    }

    public MessageCreateMono reply(Cons<Builder> setter) {
        var embed = EmbedCreateSpec.builder();
        setter.get(embed);

        return reply(embed.build());
    }

    public MessageCreateMono reply(EmbedCreateSpec embed) {
        return channel.createMessage(embed).withMessageReference(message.getId());
    }

    // region special methods

    public void timeout() {
        error("Internal Error", "The server did not respond. Perhaps the server is down or an error has occurred.").subscribe();
    }

    public void reply(EmbedResponse response) {
        reply(embed -> {
            embed.color(response.color);
            embed.title(response.title);
            embed.fields(response.fields.map(field -> Field.of(field.name(), field.value(), false)));

            if (response.content != null) embed.description(response.content);
            if (response.footer != null) embed.footer(response.footer, null);
        }).withFiles(response.files.map(Fi::get).map(file -> File.of(file.name(), file.read()))).subscribe();
    }

    // endregion
}