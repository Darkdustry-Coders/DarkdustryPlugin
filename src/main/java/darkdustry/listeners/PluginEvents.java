package darkdustry.listeners;

import arc.Events;
import arc.util.*;
import darkdustry.database.*;
import darkdustry.features.*;
import darkdustry.features.history.*;
import darkdustry.features.menus.MenuHandler;
import darkdustry.features.net.Socket;
import darkdustry.listeners.SocketEvents.ServerMessageEmbedEvent;
import discord4j.rest.util.Color;
import mindustry.content.*;
import mindustry.entities.Units;
import mindustry.game.EventType.*;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.logic.LExecutor;
import mindustry.world.blocks.payloads.BuildPayload;
import mindustry.world.blocks.storage.CoreBlock;
import useful.Bundle;

import java.io.IOException;

import static arc.Core.*;
import static darkdustry.PluginVars.*;
import static darkdustry.config.Config.*;
import static mindustry.Vars.*;
import static mindustry.net.Administration.Config.*;
import static mindustry.server.ServerControl.*;

public class PluginEvents {

    public static void load() {
        //boolean[] unitCapDynamic = new boolean[] {false};

        Events.on(ServerLoadEvent.class, event -> Socket.send(new ServerMessageEmbedEvent(config.mode.name(), "Server Launched", Color.SUMMER_SKY)));

        Events.on(PlayEvent.class, event -> {
            //unitCapDynamic[0] = state.rules.unitCapVariable;

            state.rules.showSpawns = true;
            state.rules.unitPayloadUpdate = true;

            state.rules.modeName = config.mode.displayName;
            state.rules.revealedBlocks.addAll(Blocks.slagCentrifuge, Blocks.heatReactor, Blocks.scrapWall, Blocks.scrapWallLarge, Blocks.scrapWallHuge, Blocks.scrapWallGigantic, Blocks.thruster);

            //if (config.straightforwardUnitCap) {
            //    state.rules.unitCapVariable = false;

            //    final int unitCap = state.rules.unitCap;
            //}

            if (state.rules.infiniteResources)
                state.rules.revealedBlocks.addAll(Blocks.shieldProjector, Blocks.largeShieldProjector, Blocks.beamLink);

            if (config.mode.disableAttackMode)
                state.rules.attackMode = false;

            if (config.mode.disableCrashDamage)
                state.rules.unitCrashDamageMultiplier = 0.0f;

            if (config.mode.enable1va)
                OnevAll.nextMap();

            if (OnevAll.enabled()) {
                assert OnevAll.single != null;
                Log.info("Started a 1va round, gladiator: " + OnevAll.single.plainName());
                for (Player player : Groups.player)
                    player.team(OnevAll.selectTeam(player));
            }
        });

        //if (config.straightforwardUnitCap) {
        //    Events.on();
        //}

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
        });

        Events.on(PlayerLeave.class, event -> {
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
        });

        var _nativeAssigner = netServer.assigner;
        netServer.assigner = (player, players) -> {
            if (OnevAll.enabled()) {
                return OnevAll.selectTeam(player);
            }

            return _nativeAssigner.assign(player, players);
        };

        Events.on(UnitSpawnEvent.class, event -> {
            if (config.straightforwardUnitCap) {
                int cap = state.rules.unitCap;
                if (state.rules.unitCapVariable)
                    cap *= Groups.build.count(x -> x instanceof CoreBlock.CoreBuild && x.team() == event.unit.team());
                if (Groups.unit.count(u -> !u.spawnedByCore && u.team() == event.unit.team() &&
                        u.type == event.unit.type) > cap) {
                    event.unit.kill();
                    return;
                }
            }

            if (config.maxUnitsTotal < 0) return;
            if (event.unit.spawnedByCore()) return;
            if (Groups.unit.size() < config.maxUnitsTotal) return;
            if (Groups.unit.count(u -> !u.spawnedByCore) < config.maxUnitsTotal) return;

            event.unit.kill();
        });

        instance.gameOverListener = event -> {
            if (OnevAll.enabled()) {
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

            while (data.rank.checkNext(data.playTime, data.blocksPlaced, data.gamesPlayed, data.wavesSurvived)) {
                data.rank = data.rank.next;

                Ranks.name(player, data);
                MenuHandler.showPromotionMenu(player, data);
            }

            Database.savePlayerData(data);
        }), 60f, 60f);
    }
}