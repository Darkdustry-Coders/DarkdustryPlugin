package darkdustry.discord;

import arc.func.Cons;
import arc.util.Strings;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.EmbedCreateSpec.Builder;
import discord4j.core.spec.MessageCreateMono;
import discord4j.rest.util.Color;

public record MessageContext(Message message, Member member, MessageChannel channel) {

    public MessageCreateMono success(String title, String content, Object... values) {
        return success(embed -> embed.title(title).description(Strings.format(content, values)));
    }

    public MessageCreateMono success(Cons<Builder> cons) {
        var embed = EmbedCreateSpec.builder().color(Color.MEDIUM_SEA_GREEN);
        cons.get(embed);

        return reply(embed.build());
    }

    public MessageCreateMono error(String title, String content, Object... values) {
        return error(embed -> embed.title(title).description(Strings.format(content, values)));
    }

    public MessageCreateMono error(Cons<Builder> cons) {
        var embed = EmbedCreateSpec.builder().color(Color.CINNABAR);
        cons.get(embed);

        return reply(embed.build());
    }

    public MessageCreateMono info(String title, String content, Object... values) {
        return info(embed -> embed.title(title).description(Strings.format(content, values)));
    }

    public MessageCreateMono info(Cons<Builder> cons) {
        var embed = EmbedCreateSpec.builder().color(Color.SUMMER_SKY);
        cons.get(embed);

        return reply(embed.build());
    }

    public MessageCreateMono reply(String text) {
        return channel.createMessage(text).withMessageReference(message.getId());
    }

    public MessageCreateMono reply(EmbedCreateSpec embed) {
        return channel.createMessage(embed).withMessageReference(message.getId());
    }
}