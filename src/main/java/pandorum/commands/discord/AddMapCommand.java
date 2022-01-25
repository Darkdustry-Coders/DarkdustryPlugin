package pandorum.commands.discord;

import arc.files.Fi;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;

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
            Fi mapFile = customMapDirectory.child(attachment.getFileName());
            attachment.downloadToFile(mapFile.file()).thenAccept(file -> maps.reload());
            text(message.getChannel(), ":white_check_mark: Карта добавлена на сервер. ||(@)||", mapFile.absolutePath());
        } catch (Exception e) {
            err(message.getChannel(), ":x: Ошибка.", "Добавить карту на сервер не удалось.");
        }
    }
}
