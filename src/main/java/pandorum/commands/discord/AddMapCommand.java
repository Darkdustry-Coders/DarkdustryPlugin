package pandorum.commands.discord;

import arc.files.Fi;
import arc.util.io.Streams;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;

import java.io.FileOutputStream;

import static mindustry.Vars.*;
import static pandorum.Misc.download;
import static pandorum.discord.BotHandler.err;
import static pandorum.discord.BotHandler.text;

public class AddMapCommand {
    public static void run(final String[] args, final Message message) {
        if (message.getAttachments().size() != 1 || !message.getAttachments().get(0).getFilename().endsWith(mapExtension)) {
            err(message.getChannel().block(), "Ошибка.", "Пожалуйста, прикрепи файл карты к сообщению.");
            return;
        }

        Attachment a = message.getAttachments().get(0);

        try {
            Fi mapFile = customMapDirectory.child(a.getFilename());
            Streams.copy(download(a.getUrl()), new FileOutputStream(mapFile.file()));
            maps.reload();
            text(message.getChannel().block(), "*Карта добавлена на сервер. (@)*", mapFile.absolutePath());
        } catch (Exception e) {
            err(message.getChannel().block(), "Ошибка.", "Добавить карту не удалось.");
        }
    }
}
