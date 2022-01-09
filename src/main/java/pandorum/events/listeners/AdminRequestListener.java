package pandorum.events.listeners;

import mindustry.game.EventType.AdminRequestEvent;

import static pandorum.Misc.sendToChat;

public class AdminRequestListener {

    public static void call(final AdminRequestEvent event) {
        switch (event.action) {
            case wave -> sendToChat("events.admin.wave", event.player.coloredName());
            case kick -> sendToChat("events.admin.kick", event.player.coloredName(), event.other.coloredName());
            case ban -> sendToChat("events.admin.ban", event.player.coloredName(), event.other.coloredName());
        }
    }
}
