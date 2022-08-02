package rewrite.listeners;

import arc.func.Cons;
import arc.struct.ObjectMap;
import mindustry.game.EventType.*;
import mindustry.gen.Call;
import mindustry.net.Administration.Config;
import rewrite.DarkdustryPlugin;
import rewrite.components.Database.PlayerData;
import rewrite.features.Effects;
import rewrite.features.Ranks;
import rewrite.utils.Find;

import static pandorum.listeners.handlers.MenuHandler.*; // TODO: заменить импорт из пандорума
import static rewrite.PluginVars.*;
import static rewrite.components.Bundle.*;
import static rewrite.components.Database.*;

import java.util.Locale;

@SuppressWarnings("unchecked")
public class PluginEvents {

    public static ObjectMap<Class<?>, Event<?>> events = new ObjectMap<>();
    public static Event<GameOverEvent> gameover;

    public static void load() {
        register(AdminRequestEvent.class, event -> {
            switch (event.action) {
                case wave -> sendToChat("events.admin.wave", event.player.name);
                case kick -> sendToChat("events.admin.kick", event.player.name, event.other.name);
                case ban -> sendToChat("events.admin.ban", event.player.name, event.other.name);
                default -> {} // без этой строки vscode кидает ошибку
            }
        });
        register(BlockBuildEndEvent.class, event -> {});
        register(BuildSelectEvent.class, event -> {});
        register(ConfigEvent.class, event -> {});
        register(DepositEvent.class, event -> {});
        register(GameOverEvent.class, event -> {});
        register(PlayerJoin.class, event -> {
        });
        register(PlayerLeave.class, event -> {
        register(ServerLoadEvent.class, event -> {});
        register(TapEvent.class, event -> {});
        register(WithdrawEvent.class, event -> {});
        register(WorldLoadEvent.class, event -> {});
    }

    private static <T> void register(Class<T> type, Event<T> event){
        events.put(type, event);
    }

    public interface Event<T> extends Runnable {

        void get(T event);

        @Override
        default void run() {
            get(null);
        }

        default <B> Cons<B> listener() {
            return event -> get((T) event);
        }
    }
}
