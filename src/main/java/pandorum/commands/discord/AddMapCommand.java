package pandorum.commands.discord;

import arc.files.Fi;
import mindustry.io.SaveIO;
import net.dv8tion.jda.api.entities.Message.Attachment;
import pandorum.discord.Context;

import static mindustry.Vars.*;

public class AddMapCommand {
    public static void run(final String[] args, final Context context) {
        if (context.attachments.size() != 1 || !context.attachments.get(0).getFileName().endsWith(mapExtension)) {
            context.err(":link: Invalid attachment.", "You need to attach a valid .msav file!");
            return;
        }

        Attachment attachment = context.attachments.get(0);

        attachment.downloadToFile(customMapDirectory.child(attachment.getFileName()).file()).thenAccept(file -> {
            Fi mapFile = new Fi(file);
            if (!SaveIO.isSaveValid(mapFile)) {
                context.err(":x: Attachment is invalid or corrupted!");
                mapFile.delete();
                return;
            }

            maps.reload();
            context.success(":map: Map added to server.");
        }).exceptionally(e -> {
            context.err(":x: Attachment is invalid or corrupted!");
            return null;
        });
    }
}
