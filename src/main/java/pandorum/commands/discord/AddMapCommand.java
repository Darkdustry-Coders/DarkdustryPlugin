package pandorum.commands.discord;

import arc.files.Fi;
import arc.util.io.Streams;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;

import java.io.FileOutputStream;

import static mindustry.Vars.*;
import static pandorum.Misc.download;
import static pandorum.discord.Bot.*;

public class AddMapCommand {
    public static void run(final String[] args, final Message message) {
        if (message.getAttachments().size() != 1 || !message.getAttachments().get(0).getFilename().endsWith(mapExtension)) {
            err(message, "Ошибка.", "Пожалуйста, прикрепи файл карты к сообщению.");
            return;
        }

        Attachment a = message.getAttachments().get(0);

        try {
            Fi mapFile = customMapDirectory.child(a.getFilename());
            Streams.copy(download(a.getUrl()), new FileOutputStream(mapFile.file()));
            maps.reload();
            text(message, "*Карта добавлена на сервер. (@)*", mapFile.absolutePath());
        } catch (Exception e) {
            err(message, "Ошибка.", "Добавить карту не удалось.");
        }
    }
}
