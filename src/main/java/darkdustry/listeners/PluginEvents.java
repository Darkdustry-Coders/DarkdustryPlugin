package darkdustry.listeners;

import arc.Events;
import arc.struct.IntIntMap;
import arc.struct.IntMap;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.*;
import arc.util.Timer.Task;
import darkdustry.database.*;
import darkdustry.features.*;
import darkdustry.features.history.*;
import darkdustry.features.menus.MenuHandler;
import darkdustry.features.net.Socket;
import darkdustry.listeners.SocketEvents.ServerMessageEmbedEvent;
import darkdustry.utils.Admins;
import darkdustry.utils.Utils;
import discord4j.rest.util.Color;
import mindustry.content.*;
import mindustry.entities.Units;
import mindustry.game.EventType.*;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.io.SaveIO;
import mindustry.world.blocks.payloads.BuildPayload;
import mindustry.world.blocks.storage.CoreBlock;
import useful.Bundle;

import java.io.IOException;
import java.time.Duration;

import static arc.Core.*;
import static darkdustry.PluginVars.*;
import static darkdustry.config.Config.*;
import static mindustry.Vars.*;
import static mindustry.net.Administration.Config.*;
import static mindustry.server.ServerControl.*;

public class PluginEvents {
    public static ObjectMap<Player, Task> updateNameTasks = new ObjectMap<>();

    private static Task noPlayersRestartTask = null;

    public static void load() {
        if (config.allowSpecialSettings) SpecialSettings.load();

        Events.on(ServerLoadEvent.class, event -> Socket.send(new ServerMessageEmbedEvent(config.mode.name(), "Server Launched", Color.SUMMER_SKY)));

        Events.on(PlayEvent.class, event -> {
            if (noPlayersRestartTask != null) {
                noPlayersRestartTask.cancel();
                noPlayersRestartTask = null;
            }

            if (config.allowSpecialSettings) SpecialSettings.update();

            state.rules.showSpawns = true;
            state.rules.unitPayloadUpdate = true;

            state.rules.modeName = config.mode.displayName;
            if (config.mode != Gamemode.hub)
                state.rules.revealedBlocks.addAll(Blocks.slagCentrifuge, Blocks.heatReactor, Blocks.scrapWall, Blocks.scrapWallLarge, Blocks.scrapWallHuge, Blocks.scrapWallGigantic, Blocks.thruster);

            if (state.rules.infiniteResources)
                state.rules.revealedBlocks.addAll(Blocks.shieldProjector, Blocks.largeShieldProjector, Blocks.beamLink);

            if (config.mode.enable1va)
                OnevAll.nextMap();

            if (OnevAll.enabled()) {
                assert OnevAll.single != null;
                Log.info("Started a 1va round, gladiator: " + OnevAll.single.plainName());
                for (Player player : Groups.player)
                    player.team(OnevAll.selectTeam(player));
            }

            if (config.mode.postSetup != null)
                config.mode.postSetup.get();
        });

        Events.on(WaveEvent.class, event -> Groups.player.each(player -> Cache.get(player).wavesSurvived++));

        Events.on(WorldLoadEvent.class, event -> History.reset());

        Events.on(DepositEvent.class, Alerts::depositAlert);

        Events.on(ConfigEvent.class, event -> {
            if (History.enabled() && event.player != null)
                History.put(event.tile.tile, new ConfigEntry(event));
        });

        Events.on(TapEvent.class, event -> {
            if (!History.enabled() || !Cache.get(event.player).history) return;

            var queue = History.get(event.tile.array());
            if (queue == null) return;

            var builder = new StringBuilder();
            queue.each(entry -> builder.append("\n").append(entry.getMessage(event.player)));

            if (queue.isEmpty())
                builder.append(Bundle.get("history.empty", event.player));

            Bundle.send(event.player, "history.title", event.tile.x, event.tile.y, builder.toString());
        });

        // Extremely bugged!

        //Events.on(PickupEvent.class, event -> {
        //    if (event.build != null && History.enabled()) {
        //        History.put(event.build.tile, new PayloadEntry(event));
        //    }
        //});

        //Events.on(PayloadDropEvent.class, event -> {
        //    if (event.build != null && History.enabled()) {
        //        History.put(event.build.tile, new PayloadEntry(event));
        //    }
        //});

        Events.on(BlockBuildEndEvent.class, event -> {
            if (event.unit == null || !event.unit.isPlayer()) return;

            if (History.enabled() && event.tile.build != null)
                History.put(event.tile, new BlockEntry(event));

            var data = Cache.get(event.unit.getPlayer());
            if (event.breaking)
                data.blocksBroken++;
            else
                data.blocksPlaced++;
        });

        Events.on(BlockBuildBeginEvent.class, event -> {
            if (event.unit == null || !event.unit.isPlayer()) return;

            if (History.enabled() && event.tile.build != null)
                History.put(event.tile, new PreBlockEntry(event));
        });

        Events.on(BuildRotateEvent.class, event -> {
            if (event.unit == null || !event.unit.isPlayer()) return;

            if (History.enabled())
                History.put(event.build.tile, new RotateEntry(event));
        });

        Events.on(BuildSelectEvent.class, event -> {
            if (event.breaking || event.builder == null || event.builder.buildPlan() == null || !event.builder.isPlayer())
                return;

            Alerts.buildAlert(event);
        });

        Events.on(BlockDestroyEvent.class, event -> {
            if (!(event.tile.build instanceof CoreBlock.CoreBuild)) return;
        });

        Events.on(GeneratorPressureExplodeEvent.class, event -> app.post(() -> {
            if (!Units.canCreate(event.build.team, UnitTypes.latum)) return;

            Groups.unit.each(unit -> {
                if (unit instanceof Payloadc payloadc && payloadc.payloads().contains(payload -> payload instanceof BuildPayload buildPayload && buildPayload.build.id == event.build.id)) {
                    payloadc.payloads().clear();
                    payloadc.kill();
                }
            });

            Call.spawnEffect(event.build.x, event.build.y, 0f, UnitTypes.latum);
            UnitTypes.latum.spawn(event.build.team, event.build);
        }));

        Events.on(PlayerJoin.class, event -> {
            if (noPlayersRestartTask != null) {
                Log.info("Cancelled map reload.");
                noPlayersRestartTask.cancel();
                noPlayersRestartTask = null;
            }

            var data = Database.getPlayerDataOrCreate(event.player.uuid());

            Cache.put(event.player, data);
            Ranks.name(event.player, data);

            // Вызываем с задержкой, чтобы игрок успел появиться
            app.post(() -> data.effects.join.get(event.player));

            Log.info("@ has connected. [@ / @]", event.player.plainName(), event.player.uuid(), data.id);
            Bundle.send("events.join", event.player.coloredName(), data.id);

            Socket.send(new ServerMessageEmbedEvent(config.mode.name(), event.player.plainName() + " [" + data.id + "] joined", Color.MEDIUM_SEA_GREEN));

            if (data.welcomeMessage)
                MenuHandler.showWelcomeMenu(event.player);
            else if (data.discordLink)
                Call.openURI(event.player.con, discordServerUrl);

            // На мобильных устройствах приветственное сообщение отображается по-другому
            Bundle.send(event.player, event.player.con.mobile ?
                    "welcome.message.mobile" :
                    "welcome.message", serverName.string(), discordServerUrl);

            var mute = Database.getMute(event.player.uuid());
            if (mute != null) {
                updateNameTasks.put(event.player, Timer.schedule(() -> Ranks.name(event.player, data), (float) (mute.remaining()) / 1000 + 10));
            }
        });

        Events.on(PlayerLeave.class, event -> {
            Cache.mutes.remove(event.player.uuid());
            var data = Cache.remove(event.player);
            Database.savePlayerData(data);

            data.effects.leave.get(event.player);

            Log.info("@ has disconnected. [@ / @]", event.player.plainName(), event.player.uuid(), data.id);
            Bundle.send("events.leave", event.player.coloredName(), data.id);

            if (vote != null) vote.left(event.player);
            if (voteKick != null) voteKick.left(event.player);

            if (OnevAll.enabled() && event.player == OnevAll.single) {
                OnevAll.dipped();
            }

            Socket.send(new ServerMessageEmbedEvent(config.mode.name(), event.player.plainName() + " [" + data.id + "] left", Color.CINNABAR));

            if (Groups.player.size() <= 1 && Restart.restart) {
                if (Restart.copyPlugin) {
                    try {
                        Restart.copyPlugin();
                    } catch (IOException e) {
                        Log.err("Failed to copy plugin", e);
                    }
                }
                System.exit(0);
            }

            if (Groups.player.size() <= 1 && config.mode.restartOnNoPlayers) {
                Log.info("Scheduling rtv in 60s...");
                noPlayersRestartTask = Timer.schedule(() -> instance.play(() -> world.loadMap(maps.getNextMap(instance.lastMode, state.map))), 60);
            }
        });

        {
            var _nativeAssigner = netServer.assigner;
            netServer.assigner = (player, players) -> {
                if (OnevAll.enabled()) {
                    return OnevAll.selectTeam(player);
                }

                Team team;
                try {
                    team = _nativeAssigner.assign(player, players);
                } catch (Exception ignored) {
                    team = Team.derelict;
                }

                if (!Utils.isSpecialTeam(team)) return team;

                for (Team x : Team.all) {
                    if (!x.cores().isEmpty() && !Utils.isSpecialTeam(x)) {
                        return x;
                    }
                }

                return Team.derelict;
            };
        }
        if (config.mode.rememberTeams) {
            var teams = new IntIntMap();
            var _nativeAssigner = netServer.assigner;

            Events.on(WorldLoadEvent.class, event -> teams.clear());
            Events.on(PlayerLeave.class, event -> teams.put(Database.getPlayerData(event.player).id, event.player.team().id));
            Timer.schedule(() -> Groups.player.each(p -> teams.put(Database.getPlayerData(p).id, p.team().id)), 2, 2);

            netServer.assigner = (player, players) -> {
                var id = Database.getPlayerData(player).id;
                int activeTeams = Seq.with(Team.all).count(x -> !x.cores().isEmpty() && !x.data().players.isEmpty());
                int team;
                if ((team = teams.get(id, -1)) != -1 && !Team.all[team].cores().isEmpty() && activeTeams != 1)
                    return Team.all[team];
                team = _nativeAssigner.assign(player, players).id;
                teams.put(id, team);
                return Team.all[team];
            };
        }

        instance.gameOverListener = event -> {
            if (OnevAll.enabled()) {
                OnevAll.gameOverFlag = true;
                assert OnevAll.single != null;
                if (event.winner == OnevAll.single.team()) {
                    OnevAll.victory();
                }
                else {
                    OnevAll.defeat();
                }
            }

            Groups.player.each(player -> {
                var data = Cache.get(player);
                data.gamesPlayed++;

                if (player.team() == event.winner)
                    switch (config.mode) {
                        case attack -> data.attackWins++;
                        case castle -> data.castleWins++;
                        case forts -> data.fortsWins++;
                        case hexed -> data.hexedWins++;
                        case msgo -> data.msgoWins++;
                        case pvp -> data.pvpWins++;
                        case spvp -> data.spvpWins++;
                    }
            });

            // На этих режимах игра сменяется автоматически
            if (config.mode == Gamemode.hexed || config.mode == Gamemode.msgo) return;

            if (state.rules.waves)
                Log.info("Game over! Reached wave @ with @ players online on map @.", state.wave, Groups.player.size(), state.map.plainName());
            else
                Log.info("Game over! Team @ is victorious with @ players online on map @.", event.winner.name, Groups.player.size(), state.map.plainName());

            var map = maps.getNextMap(instance.lastMode, state.map);
            Log.info("Selected next map to be @.", map.plainName());

            if (state.rules.pvp)
                Bundle.infoMessage("events.gameover.pvp", event.winner.coloredName(), map.name(), map.author(), roundExtraTime.num());
            else
                Bundle.infoMessage("events.gameover", map.name(), map.author(), roundExtraTime.num());

            // Оповещаем все клиенты игроков о завершении игры
            Call.updateGameOver(event.winner);

            instance.play(() -> world.loadMap(map, map.applyRules(instance.lastMode)));

            if (Restart.restart) {
                Timer.schedule(() -> {
                    if (Restart.copyPlugin) {
                        try {
                            Restart.copyPlugin();
                        } catch (IOException e) {
                            Log.err("Failed to copy plugin", e);
                        }
                    }
                    System.exit(0);
                }, 5f);
            }
        };

        Timer.schedule(() -> {
            if (config.maxUnitsTotal < 0) return;
            if (Groups.unit.size() < config.maxUnitsTotal) return;
            int overflow = Groups.unit.count(u -> !u.spawnedByCore) - config.maxUnitsTotal;
            if (overflow < 1) return;

            int i = Groups.unit.size();
            while (overflow-- > 0) Groups.unit.index(--i).kill();

        }, 10f, 10f);

        // Таймер сборки мусора
        Timer.schedule(() -> mainExecutor.submit(System::gc), 60f, 60f);

        // Таймер эффектов движения
        Timer.schedule(() -> Groups.player.each(player -> {
            if (player.unit().moving())
                Cache.get(player).effects.move.get(player);
        }), 0f, 0.1f);

        // Таймер обновления времени игры и рангов
        Timer.schedule(() -> Groups.player.each(player -> {
            var data = Cache.get(player);
            data.playTime++;

            while (data.rank.checkNext(data.playTime, data.blocksPlaced, data.gamesPlayed, data.wavesSurvived, data.fortsOvas)) {
                data.rank = data.rank.next;

                Ranks.name(player, data);
                MenuHandler.showPromotionMenu(player, data);
            }

            Database.savePlayerData(data);
        }), 60f, 60f);

        // Rollback backup creation timer
        if (config.maxBackupCount > 0)
            Timer.schedule(() -> {
                Log.info("Creating a backup...");
                var time = Time.millis();
                if (config.maxBackupCount > 1) for (int i = config.maxBackupCount; i > 1; i--) {
                    var to = saveDirectory.child("backup-" + i + ".msav");
                    var from = saveDirectory.child("backup-" + (i - 1) + ".msav");
                    if (from.exists()) from.moveTo(to);
                }
                var newSave = saveDirectory.child("backup-1.msav");
                if (newSave.exists()) newSave.delete(); // We do not want infinite copies of backup saves.
                SaveIO.save(newSave);
                var passed = Bundle.formatDuration(Duration.ofMillis(Time.timeSinceMillis(time)));
                Log.info("Finished in " + passed);
            }, config.backupDelaySec, config.backupDelaySec);
    }
}
