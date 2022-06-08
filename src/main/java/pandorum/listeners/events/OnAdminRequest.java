package pandorum.listeners.events;

import arc.func.Cons;
import mindustry.game.EventType.AdminRequestEvent;

import static pandorum.util.PlayerUtils.sendToChat;

public class OnAdminRequest implements Cons<AdminRequestEvent> {

    public void get(AdminRequestEvent event) {
        switch (event.action) {
            case wave -> sendToChat("events.admin.wave", event.player.coloredName());
            case kick -> sendToChat("events.admin.kick", event.player.coloredName(), event.other.coloredName());
            case ban -> sendToChat("events.admin.ban", event.player.coloredName(), event.other.coloredName());
        }
    }
}
