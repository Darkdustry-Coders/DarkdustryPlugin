package darkdustry.discord;

import arc.func.Cons;
import darkdustry.listeners.SocketEvents.EmbedResponse;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.*;
import discord4j.core.spec.EmbedCreateSpec.Builder;
import discord4j.core.spec.MessageCreateFields.File;

import static arc.util.Strings.*;
import static discord4j.rest.util.Color.*;

public record MessageContext(Message message, Member member, MessageChannel channel) {

    public MessageCreateMono success(String title, String content, Object... values) {
        return success(embed -> embed.title(title).description(format(content, values)));
    }

    public MessageCreateMono success(Cons<Builder> setter) {
        return reply(embed -> {
            embed.color(MEDIUM_SEA_GREEN);
            setter.get(embed);
        });
    }

    public MessageCreateMono error(String title, String content, Object... values) {
        return error(embed -> embed.title(title).description(format(content, values)));
    }

    public MessageCreateMono error(Cons<Builder> setter) {
        return reply(embed -> {
            embed.color(CINNABAR);
            setter.get(embed);
        });
    }

    public MessageCreateMono info(String title, String content, Object... values) {
        return info(embed -> embed.title(title).description(format(content, values)));
    }

    public MessageCreateMono info(Cons<Builder> setter) {
        return reply(embed -> {
            embed.color(SUMMER_SKY);
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
        var mono = reply(embed -> {
            embed.color(response.color);
            embed.title(response.title);
            embed.fields(response.fields);

            if (response.content != null)
                embed.description(response.content);

            if (response.footer != null)
                embed.footer(response.footer, null);
        });

        if (response.file != null)
            mono = mono.withFiles(File.of(response.file.name(), response.file.read()));

        mono.subscribe();
    }

    // endregion
}