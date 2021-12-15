package pandorum.commands.client;

import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.graphics.Pal;
import pandorum.Misc;
import pandorum.annotations.commands.ClientCommand;
import pandorum.annotations.commands.OverrideCommand;
import pandorum.annotations.commands.admin.RequireAdmin;
import pandorum.comp.Bundle;

import static pandorum.Misc.findLocale;

public class AdminChatCommand {
    @OverrideCommand
    @RequireAdmin
    @ClientCommand(name = "a", args = "<message...>", description = "Send message to admins.")
    public static void run(final String[] args, final Player player) {
        Groups.player.each(Player::admin, admin -> admin.sendMessage(Bundle.format("commands.admin.a.chat", findLocale(admin.locale), Pal.adminChat, player.coloredName(), args[0]), player, args[0]));
    }
}
