package pandorum.listeners.events;

import arc.func.Cons;
import mindustry.game.EventType.AdminRequestEvent;

import static pandorum.util.PlayerUtils.sendToChat;

public class OnAdminRequest implements Cons<AdminRequestEvent> {

    public void get(AdminRequestEvent event) {
        switch (event.action) {
            case wave -> sendToChat("events.admin.wave", event.player.name);
            case kick -> sendToChat("events.admin.kick", event.player.name, event.other.name);
            case ban -> sendToChat("events.admin.ban", event.player.name, event.other.name);
            default -> {}
        }
    }
}