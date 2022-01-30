package pandorum.commands.discord;

import arc.files.Fi;
import mindustry.io.SaveIO;
import net.dv8tion.jda.api.entities.Message.Attachment;
import pandorum.discord.Context;

import static mindustry.Vars.*;

public class AddMapCommand {
    public static void run(final String[] args, final Context context) {
        if (context.attachments.size() != 1 || !context.attachments.get(0).getFileName().endsWith(mapExtension)) {
            context.err(":x: Ошибка.", "Пожалуйста, прикрепи один файл карты к сообщению.");
            return;
        }

        Attachment attachment = context.attachments.get(0);

        attachment.downloadToFile(customMapDirectory.child(attachment.getFileName()).file()).thenAccept(file -> {
            Fi mapFile = new Fi(file);
            if (!SaveIO.isSaveValid(mapFile)) {
                context.err(":x: Ошибка.", "Кажется, файл карты поврежден.");
                mapFile.delete();
                return;
            }

            maps.reload();
            context.success(":white_check_mark: Успешно.", "Карта добавлена на сервер.");
        }).exceptionally(e -> {
            context.err(":x: Ошибка.", "Добавить карту на сервер не удалось.");
            return null;
        });
    }
}
