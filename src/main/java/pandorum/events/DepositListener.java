package pandorum.events;

import arc.struct.Seq;
import arc.util.Strings;
import com.mongodb.BasicDBObject;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.world.Tile;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import pandorum.PandorumPlugin;
import pandorum.comp.Config.Gamemode;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;
import pandorum.entry.DepositEntry;
import pandorum.entry.HistoryEntry;
import pandorum.models.PlayerModel;

import static pandorum.Misc.bundled;

public class DepositListener {
    public static void call(final EventType.DepositEvent event) {
        if (PandorumPlugin.config.mode == Gamemode.hexed || PandorumPlugin.config.mode == Gamemode.hub || PandorumPlugin.config.mode == Gamemode.castle) return;

        if (event.tile.block() == Blocks.thoriumReactor && event.item == Items.thorium && event.player.team().cores().contains(c -> event.tile.dst(c.x, c.y) < PandorumPlugin.config.alertDistance)) {

            Groups.player.each(p -> PlayerModel.find(new BasicDBObject("UUID", p.uuid()), playerInfo -> {
                if (playerInfo.alerts) bundled(p, "events.withdraw-thorium", event.player.coloredName(), event.tile.tileX(), event.tile.tileY());
            }));

            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(BotMain.errorColor)
                    .setAuthor("Anti-grief")
                    .setTitle("Игрок положил торий в реактор рядом с ядром!")
                    .addField("Никнейм: ", Strings.stripColors(event.player.name), false);

            BotHandler.botChannel.sendMessage(embed).join();
        }

        HistoryEntry entry = new DepositEntry(event);
        Seq<Tile> linkedTiles = event.tile.tile.getLinkedTiles(new Seq<>());
        for (Tile tile : linkedTiles) {
            PandorumPlugin.history[tile.x][tile.y].add(entry);
        }
    }
}
