package pandorum.events.listeners;

import arc.math.geom.Vec2;
import arc.util.Log;
import discord4j.core.spec.EmbedCreateSpec;
import mindustry.game.Team;
import mindustry.game.EventType.ServerLoadEvent;
import mindustry.world.blocks.storage.CoreBlock.CoreBuild;
import pandorum.PluginVars;
import pandorum.animations.Penta;
import pandorum.comp.Config.Gamemode;

import static pandorum.discord.Bot.normalColor;
import static pandorum.discord.Bot.sendEmbed;

public class ServerLoadListener {

    public static void call(final ServerLoadEvent event) {
        if (PluginVars.config.mode == Gamemode.hub)
            PluginVars.staticAnimationEngine.pushAnimationTask(Penta.class, () -> {
                CoreBuild mainCore = Team.sharded.core();
                
                return new Vec2(
                    mainCore.x,
                    mainCore.y
                );
            }).renderTask.run();

        Log.info("[Darkdustry]: Сервер готов к работе...");
        sendEmbed(EmbedCreateSpec.builder().color(normalColor).title("Сервер запущен!").build());
    }
}
