package pandorum.annotations.gamemodes;

import pandorum.annotations.containers.DisabledGamemodes;
import pandorum.comp.Config;

import java.lang.annotation.*;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(DisabledGamemodes.class)
public @interface DisableGamemode {
    Config.Gamemode Gamemode();
}
