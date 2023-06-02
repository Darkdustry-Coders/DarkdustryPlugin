package darkdustry.discord;

import arc.func.Cons;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.*;
import discord4j.core.spec.EmbedCreateSpec.Builder;
import discord4j.rest.util.Color;

import static arc.util.Strings.*;

public record MessageContext(Message message, Member member, MessageChannel channel) {

    public MessageCreateMono success(String title, String content, Object... values) {
        return success(embed -> embed.title(title).description(format(content, values)));
    }

    public MessageCreateMono success(Cons<Builder> cons) {
        return reply(embed -> {
            embed.color(Color.MEDIUM_SEA_GREEN);
            cons.get(embed);
        });
    }

    public MessageCreateMono error(String title, String content, Object... values) {
        return error(embed -> embed.title(title).description(format(content, values)));
    }

    public MessageCreateMono error(Cons<Builder> cons) {
        return reply(embed -> {
            embed.color(Color.CINNABAR);
            cons.get(embed);
        });
    }

    public MessageCreateMono info(String title, String content, Object... values) {
        return info(embed -> embed.title(title).description(format(content, values)));
    }

    public MessageCreateMono info(Cons<Builder> cons) {
        return reply(embed -> {
            embed.color(Color.SUMMER_SKY);
            cons.get(embed);
        });
    }

    public MessageCreateMono reply(Cons<Builder> cons) {
        var embed = EmbedCreateSpec.builder();
        cons.get(embed);

        return reply(embed.build());
    }

    public MessageCreateMono reply(EmbedCreateSpec embed) {
        return channel.createMessage(embed).withMessageReference(message.getId());
    }
}