package darkdustry.config;

import arc.files.Fi;
import arc.struct.IntSeq;
import arc.util.Log;
import arc.util.Timer;
import arc.util.serialization.Json;
import darkdustry.database.Cache;
import darkdustry.database.Database;
import darkdustry.matchmaking.Matchmaking;
import mindustry.gen.Call;
import mindustry.gen.Player;

import java.io.IOException;

public class ArenaTask {
    private final static Json json = new Json() {{
        setUsePrototypes(false);
    }};

    public final int serverId;
    private final int port;
    private final Player[] players;
    private Process process = null;

    public ArenaTask(Player[] players, int port, int serverId) {
        this.players = players;
        this.port = port;
        this.serverId = serverId;
    }

    public boolean isRunning() {
        return process.isAlive();
    }

    public void online() {
        if (!isRunning()) return;
        for (var player : players) if (!player.con.isConnected()) return;
        for (var player : players) Call.connect(player.con, MatchmakingConfig.config.serversHost, port);
    }

    public void setup() {
        final var template = Fi.get(MatchmakingConfig.config.templatePath);
        final var target = Fi.get(MatchmakingConfig.config.serversPath).child("" + serverId);

        target.deleteDirectory();
        target.mkdirs();
        template.copyFilesTo(target);

        var coreConfig = json.fromJson(Config.class, target.child("config.json"));
        coreConfig.serverId = serverId;
        coreConfig.overridePort = port;
        coreConfig.whitelist = IntSeq.with();
        for (var player : players) {
            if (!player.con.isConnected()) {
                target.deleteDirectory();
                return;
            }
            coreConfig.whitelist.add(Database.getPlayerData(player).id);
        }
        json.toJson(coreConfig, target.child("config.json"));

        var builder = new ProcessBuilder();
        builder.directory(target.file());
        builder.command(target.child("start.sh").absolutePath());
        builder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        builder.redirectError(ProcessBuilder.Redirect.DISCARD);
        builder.redirectInput(ProcessBuilder.Redirect.DISCARD);

        try {
            process = builder.start();
        } catch (IOException e) {
            Log.err("Failed to start server", e);
            target.deleteDirectory();
            return;
        }

        var tasks = new Timer.Task[] { null };
        tasks[0] = Timer.schedule(() -> {
            if (isRunning()) return;

            target.deleteDirectory();
            tasks[0].cancel();
        }, 10f, 10f);

        Matchmaking.wait(this);
    }
}
