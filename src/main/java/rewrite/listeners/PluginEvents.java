package rewrite.listeners;

import arc.func.Cons;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.game.EventType.*;
import rewrite.features.Ranks;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static rewrite.components.Bundle.*;

@SuppressWarnings("unchecked")
public class PluginEvents {

    public static Seq<Event<?>> events = new Seq<>();
    public static Event<GameOverEvent> gameover;

    public static void load() {
        events.addAll(
                (AdminRequestEvent event) -> {
                    switch (event.action) {
                        case wave -> sendToChat("events.admin.wave", event.player.name);
                        case kick -> sendToChat("events.admin.kick", event.player.name, event.other.name);
                        case ban -> sendToChat("events.admin.ban", event.player.name, event.other.name);
                        default -> {} // без этой строки vscode кидает ошибку
                    }
                },
                (BlockBuildEndEvent event) -> {

                },
                (BuildSelectEvent event) -> {

                },
                (ConfigEvent event) -> {

                },
                (DepositEvent event) -> {

                },
                gameover = (GameOverEvent event) -> {

                },
                (PlayerJoin event) -> {
                    Ranks.setRank(event.player.uuid(), Ranks.veteran);
                },
                (PlayerLeave event) -> {

                },
                (ServerLoadEvent event) -> {

                },
                (TapEvent event) -> {

                },
                (WithdrawEvent event) -> {

                },
                (WorldLoadEvent event) -> {

                });
    }

    public interface Event<T> { // дарк крутой

        void get(T event);

        default void run() {
            get(null);
        }

        default Class<T> type() { // немного шизофрении
            return (Class<T>) ((ParameterizedType) this.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
        }

        default <B> Cons<B> listener() {
            return event -> get((T) event);
        }
    }
}
