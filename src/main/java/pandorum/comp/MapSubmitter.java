package pandorum.comp;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.Button;

public class MapSubmitter {

    // TODO

    public static final Button confirm = Button.success("map.confirm", "Добавить на сервер");
    public static final Button deny = Button.danger("map.deny", "Отклонить");

    public static void sendMap(Message message, Member member) {

    }

    public static void confirm(Message message, Member member) {

    }

    public static void deny(Message message, Member member) {

    }
}
