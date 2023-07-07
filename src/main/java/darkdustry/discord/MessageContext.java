package darkdustry.discord;

import arc.func.Cons;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.*;
import discord4j.core.spec.EmbedCreateSpec.Builder;
import discord4j.rest.util.*;

import static arc.util.Strings.*;

public record MessageContext(Message message, Member member, MessageChannel channel) {

    public MessageCreateMono success(String title, String content, Object... values) {
        return success(embed -> embed.title(title).description(format(content, values)));
    }

    public MessageCreateMono success(Cons<Builder> setter) {
        return reply(embed -> {
            embed.color(Color.MEDIUM_SEA_GREEN);
            setter.get(embed);
        });
    }

    public MessageCreateMono error(String title, String content, Object... values) {
        return error(embed -> embed.title(title).description(format(content, values)));
    }

    public MessageCreateMono error(Cons<Builder> setter) {
        return reply(embed -> {
            embed.color(Color.CINNABAR);
            setter.get(embed);
        });
    }

    public MessageCreateMono info(String title, String content, Object... values) {
        return info(embed -> embed.title(title).description(format(content, values)));
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
}