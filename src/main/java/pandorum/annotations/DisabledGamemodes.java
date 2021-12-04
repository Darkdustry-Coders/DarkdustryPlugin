package pandorum.annotations;

import mindustry.mod.Plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DisabledGamemodes{
    DisableGamemode[] value();
}
