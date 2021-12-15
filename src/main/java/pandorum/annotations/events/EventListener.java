package pandorum.annotations.events;

import mindustry.game.EventType;

public @interface EventListener {
    Class<? super EventType> eventType();
}
