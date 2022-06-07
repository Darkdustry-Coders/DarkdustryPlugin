package pandorum.commands.discord;

import arc.Events;
import arc.util.CommandHandler.CommandRunner;
import mindustry.game.EventType.GameOverEvent;
import pandorum.discord.Context;
import pandorum.util.Utils;

import static mindustry.Vars.state;

public class GameOverCommand implements CommandRunner<Context> {
    public void accept(String[] args, Context context) {
        if (!Utils.isAdmin(context.member)) {
            context.err(":no_entry_sign: Эта команда только для администрации.", "У тебя нет прав на ее использование.");
            return;
        }

        Events.fire(new GameOverEvent(state.rules.waveTeam));
        context.success(":map: Игра успешно завершена.");
    }
}
