package pandorum.comp;

import arc.util.*;
import arc.util.Timer.Task;
import mindustry.game.Gamemode;
import mindustry.gen.*;
import mindustry.maps.*;
import mindustry.net.WorldReloader;

import static mindustry.Vars.*;
import pandorum.comp.*;
import static pandorum.PandorumPlugin.*;

import java.util.Locale;

public class VoteMapSession extends VoteSession{
    private final Map target;

    public VoteMapSession(VoteSession[] map, Map target){
        super(map);

        this.target = target;
    }

    @Override
    protected Task start(){
        return Timer.schedule(() -> {
            if(!checkPass()){
                sendToChat("commands.nominate.map.failed", target.name());
                voted.clear();
                map[0] = null;
                task.cancel();
            }
        }, config.voteDuration);
    }

    @Override
    public void vote(Player player, int d){
        votes += d;
        voted.addAll(player.uuid(), netServer.admins.getInfo(player.uuid()).lastIP);
        sendToChat("commands.nominate.map.vote", player.name, target.name(), votes, votesRequired());
        checkPass();
    }

    @Override
    protected boolean checkPass(){
        if(votes >= votesRequired()){
            sendToChat("commands.nominate.map.passed", target.name());
            map[0] = null;
            task.cancel();

            Runnable r = () -> {
                WorldReloader reloader = new WorldReloader();

                reloader.begin();

                world.loadMap(target, target.applyRules(Gamemode.survival));

                state.rules = state.map.applyRules(Gamemode.survival);
                logic.play();

                reloader.end();
            };

            Timer.schedule(new Task(){
                @Override
                public void run(){
                    try{
                        r.run();
                    }catch(MapException e){
                        Log.err(e);
                        net.closeServer();
                    }
                }
            }, 10);
            return true;
        }
        return false;
    }
    public static void sendToChat(String key, Object... values) {
        Groups.player.each(p -> p.sendMessage(bundle.format(key, findLocale(p.locale), values)));
    }
    private static Locale findLocale(String code) {
        Locale locale = Structs.find(bundle.supportedLocales, l -> l.toString().equals(code) ||
                code.startsWith(l.toString()));
        return locale != null ? locale : bundle.defaultLocale();
    }
}
