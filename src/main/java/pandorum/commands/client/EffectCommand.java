package pandorum.commands.client;

import arc.util.Strings;
import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Player;
import mindustry.type.StatusEffect;
import pandorum.components.Icons;

import static pandorum.util.PlayerUtils.bundled;
import static pandorum.util.PlayerUtils.isAdmin;
import static pandorum.util.Search.findPlayer;
import static pandorum.util.Search.findEffect;
import static pandorum.util.StringUtils.effectsList;;

public class EffectCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        if (!isAdmin(player)) {
            bundled(player, "commands.permission-denied");
            return;
        }

        StatusEffect effect = findEffect(args[0]);
        if (effect == null) {
            bundled(player, "commands.effect-not-found", effectsList());
            return;
        }

        if (!Strings.canParsePositiveInt(args[1])) {
            bundled(player, "commands.admin.effect.incorrect-number-format");
            return;
        }

        int amount = Strings.parseInt(args[1]);
        if (amount < 0 || amount > 300) {
            bundled(player, "commands.admin.effect.limit", 0, 300);
            return;
        }

        Player target = args.length > 2 ? findPlayer(args[2]) : player;
        if (target == null) {
            bundled(player, "commands.player-not-found", args[2]);
            return;
        }

        if (amount == 0) target.unit().unapply(effect);
        else target.unit().apply(effect, amount * 60f);
        bundled(target, "commands.admin.effect.success", Icons.get(effect.name));
    }
}
