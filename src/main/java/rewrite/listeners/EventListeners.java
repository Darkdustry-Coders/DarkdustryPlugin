package rewrite.listeners;

import arc.func.Cons;
import mindustry.game.EventType.*;
import rewrite.DarkdustryPlugin;

import static pandorum.util.PlayerUtils.sendToChat;

// перечисления названы заглавными буквами не просто так!
// по их имени находятся классы событий

@SuppressWarnings("unchecked")
public enum EventListeners implements Cons<Object>, Runnable {
    AdminRequest((AdminRequestEvent event) -> {
        switch (event.action) {
            case wave -> sendToChat("events.admin.wave", event.player.name);
            case kick -> sendToChat("events.admin.kick", event.player.name, event.other.name);
            case ban -> sendToChat("events.admin.ban", event.player.name, event.other.name);
            default -> {} // без этой строки vscode кидает ошибку
        }
    }),
    BlockBuildEnd((BlockBuildEndEvent event) -> {

    }),
    BuildSelect((BuildSelectEvent event) -> {
        
    }),
    Config((ConfigEvent event) -> {

    }),
    Deposit((DepositEvent event) -> {

    }),
    GameOver((GameOverEvent event) -> {

    }),
    PlayerJoin((PlayerJoin event) -> {

    }),
    PlayerLeave((PlayerLeave event) -> {

    }),
    ServerLoad((ServerLoadEvent event) -> {

    }),
    Tap((TapEvent event) -> {

    }),
    Withdraw((WithdrawEvent event) -> {

    }),
    WorldLoad((WorldLoadEvent event) -> {
        
    });

    private final Cons<Object> on;

    <T> EventListeners(Cons<T> on){
        this.on = (Cons<Object>) on;
    }

    @Override
    public void run() {
        get(null);
    }

    @Override
    public void get(Object event) {
        on.get(event);
    }

    public Class<?> event() {
        try {
            return name().startsWith("Player") ? Class.forName(name()) : Class.forName(name() + "Event");
        } catch (ClassNotFoundException e) {
            DarkdustryPlugin.error("Не удалось найти класс: @", e);
        }
        return null; // по идеи, это никогда не должно случится, если у всех руки растут из правильного места
    }
}
