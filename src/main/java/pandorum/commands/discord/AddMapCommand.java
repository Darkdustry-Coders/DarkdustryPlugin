package pandorum.commands.discord;

import arc.files.Fi;
import mindustry.io.SaveIO;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;

import java.io.File;

import static mindustry.Vars.*;
import static pandorum.discord.Bot.err;
import static pandorum.discord.Bot.text;

public class AddMapCommand {
    public static void run(final String[] args, final Message message) {
        if (message.getAttachments().size() != 1 || !message.getAttachments().get(0).getFileName().endsWith(mapExtension)) {
            err(message.getChannel(), ":x: Ошибка.", "Пожалуйста, прикрепи один файл карты к сообщению.");
            return;
        }

        Attachment attachment = message.getAttachments().get(0);

        try {
            File mapFile = customMapDirectory.child(attachment.getFileName()).file();
            attachment.downloadToFile(mapFile).thenAccept(file -> {
                Fi mapFi = new Fi(file);
                if (!SaveIO.isSaveValid(mapFi)) {
                    err(message.getChannel(), ":x: Ошибка.", "Кажется, файл карты поврежден.");
                    mapFi.delete();
                    return;
                }

                maps.reload();
                text(message.getChannel(), "Карта добавлена на сервер.");
            });
        } catch (Exception e) {
            err(message.getChannel(), ":x: Ошибка.", "Добавить карту на сервер не удалось.");
        }
    }
}
