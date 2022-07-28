package pandorum.commands.discord;

import arc.files.Fi;
import arc.util.CommandHandler.CommandRunner;
import mindustry.io.SaveIO;
import net.dv8tion.jda.api.entities.Message.Attachment;
import pandorum.discord.MessageContext;

import static mindustry.Vars.*;
import static pandorum.discord.Bot.isAdmin;

public class AddMapCommand implements CommandRunner<MessageContext> {
    public void accept(String[] args, MessageContext context) {
        if (!isAdmin(context.member)) {
            context.err(":no_entry_sign: Эта команда только для администрации.", "У тебя нет прав на ее использование.");
            return;
        }

        if (context.message.getAttachments().size() != 1 || !context.message.getAttachments().get(0).getFileName().endsWith(mapExtension)) {
            context.err(":link: Неверное вложение.", "Тебе нужно прикрепить один файл с расширением **.msav!**");
            return;
        }

        Attachment attachment = context.message.getAttachments().get(0);

        attachment.getProxy().downloadToFile(customMapDirectory.child(attachment.getFileName()).file()).thenAccept(file -> {
            Fi mapFile = new Fi(file);
            if (!SaveIO.isSaveValid(mapFile)) {
                context.err(":no_entry_sign: Файл поврежден или не является картой!");
                mapFile.delete();
                return;
            }

            maps.reload();
            context.success(":map: Карта добавлена на сервер.");
        }).exceptionally(e -> {
            context.err(":no_entry_sign: Файл поврежден или не является картой!");
            return null;
        });
    }
}
