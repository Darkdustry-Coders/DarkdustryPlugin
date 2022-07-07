package pandorum.commands.discord;

import arc.Events;
import arc.util.CommandHandler.CommandRunner;
import mindustry.game.EventType.GameOverEvent;
import pandorum.discord.MessageContext;

import static mindustry.Vars.state;
import static pandorum.util.PlayerUtils.isAdmin;

public class GameOverCommand implements CommandRunner<MessageContext> {
    public void accept(String[] args, MessageContext context) {
        if (!isAdmin(context.member)) {
            context.err(":no_entry_sign: Эта команда только для администрации.", "У тебя нет прав на ее использование.");
            return;
        }

        if (state.isMenu()) {
            context.err(":gear: Сервер не запущен.", ":thinking: Почему?");
            return;
        }

        Events.fire(new GameOverEvent(state.rules.waveTeam));
        context.success(":map: Игра успешно завершена.");
    }
}
