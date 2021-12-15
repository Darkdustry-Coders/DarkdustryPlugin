package pandorum.annotations.commands.gamemodes.containers;

import pandorum.annotations.commands.gamemodes.RequireGamemode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequiredGamemodes {
    RequireGamemode[] value();
}
