package pandorum.annotations.commands.gamemodes;

import pandorum.annotations.commands.gamemodes.containers.DisabledGamemodes;
import pandorum.comp.Config;

import java.lang.annotation.*;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(DisabledGamemodes.class)
public @interface DisableGamemode {
    Config.Gamemode Gamemode();
}
