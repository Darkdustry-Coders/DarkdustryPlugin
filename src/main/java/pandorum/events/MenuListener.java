package pandorum.events;

import arc.Events;
import arc.util.Strings;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Unitc;
import mindustry.ui.Menus;
import org.bson.Document;
import pandorum.PandorumPlugin;
import pandorum.comp.DiscordWebhookManager;

import static pandorum.Misc.bundled;
import static pandorum.Misc.sendToChat;

public class MenuListener {
    public static void init() {
        // Приветственное сообщение
        Menus.registerMenu(1, (player, option) -> {
            if (option == 1) {
                Document playerInfo = PandorumPlugin.createInfo(player);
                playerInfo.replace("hellomsg", false);
                PandorumPlugin.savePlayerStats(player.uuid());
                bundled(player, "events.hellomsg.disabled");
            }
        });

        // Команда /despw
        Menus.registerMenu(2, (player, option) -> {
            if (option == 1) return;
            int amount = 0;

            switch (option) {
                case 0 -> {
                    amount = Groups.unit.size();
                    Groups.unit.each(Unitc::kill);
                }
                case 2 -> {
                    amount = Groups.unit.count(Unitc::isPlayer);
                    Groups.unit.each(Unitc::isPlayer, Unitc::kill);
                }
                case 3 -> {
                    amount = Groups.unit.count(u -> u.team == Team.sharded);
                    Groups.unit.each(u -> u.team == Team.sharded, Unitc::kill);
                }
                case 4 -> {
                    amount = Groups.unit.count(u -> u.team == Team.crux);
                    Groups.unit.each(u -> u.team == Team.crux, Unitc::kill);
                }
                case 5 -> {
                    player.clearUnit();
                    bundled(player, "commands.admin.despw.suicide");
                    return;
                }
            }

            bundled(player, "commands.admin.despw.success", amount);
            WebhookEmbedBuilder despwEmbedBuilder = new WebhookEmbedBuilder()
                    .setColor(0xFF0000)
                    .setTitle(new WebhookEmbed.EmbedTitle("Убито " + amount + " юнитов!", null))
                    .addField(new WebhookEmbed.EmbedField(true, "Imposter", Strings.stripColors(player.name)));
            DiscordWebhookManager.client.send(despwEmbedBuilder.build());
        });

        // Команда /artv
        Menus.registerMenu(3, (player, option) -> {
            if (option == 0) {
                Events.fire(new EventType.GameOverEvent(Team.crux));
                sendToChat("commands.admin.artv.info");
                WebhookEmbedBuilder artvEmbedBuilder = new WebhookEmbedBuilder()
                        .setColor(0xFF0000)
                        .setTitle(new WebhookEmbed.EmbedTitle("Игра принудительно завершена!", null))
                        .addField(new WebhookEmbed.EmbedField(true, "Imposter", Strings.stripColors(player.name)));
                DiscordWebhookManager.client.send(artvEmbedBuilder.build());
            }
        });
    }
}
