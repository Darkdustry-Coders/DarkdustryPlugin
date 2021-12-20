package pandorum.annotations.handlers;

import mindustry.net.Packet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PacketHandler {
    Class<? extends Packet> packetType();
}
