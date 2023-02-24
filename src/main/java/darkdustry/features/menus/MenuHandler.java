package darkdustry.features.menus;

import arc.func.*;
import arc.graphics.Color;
import arc.struct.Seq;
import mindustry.content.Fx;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import useful.*;
import useful.Menu.MenuView;
import useful.Menu.MenuView.OptionData;
import useful.State.StateKey;
import useful.menu.ConfirmMenu;
import useful.menu.ListMenu;

import static darkdustry.PluginVars.*;
import static darkdustry.components.Database.*;
import static darkdustry.components.EffectsCache.updateEffects;
import static darkdustry.features.Ranks.ranks;
import static darkdustry.utils.Administration.ban;
import static darkdustry.utils.Utils.*;
import static mindustry.net.Administration.Config.serverName;
import static useful.Bundle.bundled;

public class MenuHandler {

    // region menus

    public static final ListMenu listMenu = new ListMenu();
    public static final ConfirmMenu confirmMenu = new ConfirmMenu();

    public static final Menu
            statsMenu = new Menu(),
            promotionMenu = new Menu(),
            requirementsMenu = new Menu(),
            welcomeMenu = new Menu(),
            despawnMenu = new Menu(),
            tempbanMenu = new Menu(),
            settingsMenu = new Menu(),
            languagesMenu = new Menu(),
            effectsMenu = new Menu();

    // endregion
    // region keys

    public static final StateKey<Player> TARGET = new StateKey<>("target", Player.class);
    public static final StateKey<PlayerData> DATA = new StateKey<>("data", PlayerData.class);

    // endregion
    // region transforms

    public static void load() {
        listMenu.left("ui.button.left");
        listMenu.right("ui.button.right");
        listMenu.page("ui.button.page");
        listMenu.close("ui.button.close");

        confirmMenu.confirm("ui.button.yes");
        confirmMenu.deny("ui.button.no");

        statsMenu.transform(TARGET, DATA, (menu, target, data) -> {
            menu.title("stats.title");
            menu.content("stats.content", target.coloredName(), data.rank.name(menu.player), data.rank.description(menu.player), data.blocksPlaced, data.blocksBroken, data.gamesPlayed, data.wavesSurvived, data.pvpWins, data.pvpLosses, formatDuration(data.playTime * 60 * 1000L, menu.player));

            menu.option("stats.requirements.show", Action.open(requirementsMenu)).row();
            menu.option("ui.button.close");
        });

        promotionMenu.transform(DATA, (menu, data) -> {
            menu.title("stats.promotion.title");
            menu.content("stats.promotion.content", data.rank.name(menu.player), data.rank.description(menu.player));

            menu.option("stats.requirements.show", Action.open(requirementsMenu)).row();
            menu.option("ui.button.close");
        });

        requirementsMenu.transform(menu -> {
            var builder = new StringBuilder();
            ranks.each(rank -> rank.requirements != null, rank -> builder.append(rank.requirements(menu.player)).append("\n"));

            menu.title("stats.requirements.title");
            menu.content(builder.toString());

            menu.option("ui.button.back", Action.back());
            menu.option("ui.button.close");
        });

        welcomeMenu.transform(menu -> {
            var builder = new StringBuilder();
            welcomeMessageCommands.each(command -> builder.append("\n[cyan]/").append(command).append("[gray] - [lightgray]").append(Bundle.get("commands." + command + ".description", menu.player)));

            menu.title("welcome.title");
            menu.content("welcome.content", serverName.string(), builder.toString());

            menu.option("ui.button.close").row();
            menu.option("welcome.discord", Action.uri(discordServerUrl)).row();
            menu.option("welcome.disable", view -> {
                updatePlayerData(view.player, data -> data.welcomeMessage = false);
                bundled(view.player, "welcome.disabled");
            });
        });

        despawnMenu.transform(menu -> {
            menu.title("despawn.title");
            menu.content("despawn.content");

            menu.options(1, DespawnType.values()).row();
            menu.option("despawn.suicide", view -> {
                Call.unitEnvDeath(view.player.unit());
                bundled(view.player, "despawn.success.suicide");
            }).row();

            menu.option("ui.button.close");
        });

        tempbanMenu.transform(TARGET, (menu, target) -> {
            menu.title("tempban.title");
            menu.content("tempban.content", target.coloredName());

            menu.options(3, BanDuration.values()).row();

            menu.option("ui.button.close");
        });

        settingsMenu.transform(DATA, (menu, data) -> {
            menu.title("settings.title");
            menu.content("settings.content");

            menu.options(1, Setting.values()).row();
            menu.option("setting.translator", Action.open(languagesMenu), data.language.name(menu)).row();
            menu.option("setting.effects", Action.open(effectsMenu), data.effects.name(menu)).row();

            menu.option("ui.button.close");
        });

        languagesMenu.transform(DATA, (menu, data) -> {
            menu.title("language.title");
            menu.content("language.content", data.language.name(menu));

            menu.options(3, Language.values()).row();

            menu.option("ui.button.back", Action.back());
            menu.option("ui.button.close");
        });

        effectsMenu.transform(DATA, (menu, data) -> {
            menu.title("effects.title");
            menu.content("effects.content", data.effects.name(menu));

            menu.options(2, EffectsPack.values()).row();

            menu.option("ui.button.back", Action.back());
            menu.option("ui.button.close");
        });
    }

    // endregion
    // region show

    public static <T> void showListMenu(Player player, String title, Seq<T> content, int page, int pages, Cons3<StringBuilder, Integer, T> cons) {
        listMenu.show(player, page, pages, title, newPage -> formatList(content, newPage, cons));
    }

    public static void showConfirmMenu(Player player, String content, Runnable confirmed, Object... values) {
        confirmMenu.show(player, "ui.title.confirm", content, confirmed, values);
    }

    public static void showStatsMenu(Player player, Player target, PlayerData data) {
        statsMenu.show(player, TARGET, target, DATA, data);
    }

    public static void showPromotionMenu(Player player, PlayerData data) {
        promotionMenu.show(player, DATA, data);
    }

    public static void showWelcomeMenu(Player player) {
        welcomeMenu.show(player);
    }

    public static void showDespawnMenu(Player player) {
        despawnMenu.show(player);
    }

    public static void showTempbanMenu(Player player, Player target) {
        tempbanMenu.show(player, TARGET, target);
    }

    public static void showSettingsMenu(Player player, PlayerData data) {
        settingsMenu.show(player, DATA, data);
    }

    // endregion
    // region enums

    public enum BanDuration implements OptionData {
        one_day(1),
        three_days(3),
        five_days(5),
        one_week(7),
        two_weeks(14),
        one_month(30),

        permanent(0); // Special case: permanent ban is 0 days

        public final long duration;

        BanDuration(int days) {
            this.duration = days * 24 * 60 * 60 * 1000L;
        }

        @Override
        public void option(MenuView menu) {
            menu.option("tempban." + name(), view -> ban(view.player, view.state.get(TARGET), duration));
        }
    }

    public enum DespawnType implements OptionData {
        all(unit -> true),
        players(Unit::isPlayer),
        ai(Unit::isAI),
        ally((player, unit) -> player.team() == unit.team),
        enemy((player, unit) -> player.team() != unit.team);

        public final Func2<Player, Unit, Boolean> filter;

        DespawnType(Boolf<Unit> filter) {
            this((player, unit) -> filter.get(unit));
        }

        DespawnType(Func2<Player, Unit, Boolean> filter) {
            this.filter = filter;
        }

        @Override
        public void option(MenuView menu) {
            menu.option("despawn." + name(), view -> {
                Groups.unit.each(unit -> filter.get(view.player, unit), Call::unitEnvDeath);
                bundled(view.player, "despawn.success");
            });
        }
    }

    public enum Setting implements OptionData {
        alerts(data -> data.alerts = !data.alerts, data -> data.alerts),
        history(data -> data.history = !data.history, data -> data.history),
        welcomeMessage(data -> data.welcomeMessage = !data.welcomeMessage, data -> data.welcomeMessage);

        public final Cons<PlayerData> cons;
        public final Func<PlayerData, Boolean> func;

        Setting(Cons<PlayerData> cons, Func<PlayerData, Boolean> func) {
            this.cons = cons;
            this.func = func;
        }

        @Override
        public void option(MenuView menu) {
            menu.option("setting." + name(), Action.then(view -> updatePlayerData(view.player, cons), Action.showUse(DATA, cons)), Bundle.get(func.get(menu.state.get(DATA)) ? "setting.on" : "setting.off", menu.player));
        }
    }

    public enum Language implements OptionData {
        english("en", "English"),
        french("fr", "Français"),
        german("de", "Deutsch"),

        italian("it", "Italiano"),
        spanish("es", "Español"),
        portuguese("pt", "Portuga"),

        russian("ru", "Русский"),
        polish("pl", "Polski"),
        turkish("tr", "Türkçe"),

        chinese("zh", "简体中文"),
        korean("ko", "한국어"),
        japanese("ja", "日本語"),

        off("off", "language.disabled", "language.disable");

        public final String code, name, button;

        Language(String code, String name) {
            this(code, name, name);
        }

        Language(String code, String name, String button) {
            this.code = code;
            this.name = name;
            this.button = button;
        }

        public String name(MenuView menu) {
            return Bundle.get(name, menu.player);
        }

        @Override
        public void option(MenuView menu) {
            menu.option(button, Action.then(view -> updatePlayerData(view.player, data -> data.language = this), Action.showUse(DATA, data -> data.language = this)));
        }
    }

    public enum EffectsPack implements OptionData {
        scathe("Scathe",
                player -> Effects.stack(player, Fx.scatheExplosion, Fx.scatheLight),
                player -> Effects.stack(player, Fx.scatheExplosion, Fx.scatheLight),
                player -> Effects.at(Fx.neoplasiaSmoke, player)
        ),

        titan("Titan",
                player -> Effects.stack(player, Fx.titanExplosion, Fx.titanSmoke),
                player -> Effects.stack(player, Fx.titanExplosion, Fx.titanSmoke),
                player -> Effects.at(Fx.incendTrail, player, 3f)
        ),

        lancer("Lancer",
                player -> Effects.stack(player, Fx.railShoot, Fx.railShoot, Fx.railShoot),
                player -> Effects.stack(player, Fx.railShoot, Fx.railShoot, Fx.railShoot),
                player -> Effects.at(Fx.lightningCharge, player)
        ),

        foreshadow("Foreshadow",
                player -> Effects.stack(player, Fx.instHit, Fx.instHit, Fx.instHit),
                player -> Effects.stack(player, Fx.instHit, Fx.instHit, Fx.instHit),
                player -> Effects.at(Fx.mineWallSmall, player, Color.yellow)
        ),

        neoplasm("Neoplasm",
                player -> Effects.stack(player, Pal.neoplasm1, Fx.neoplasmSplat, Fx.titanSmoke),
                player -> Effects.stack(player, Pal.neoplasm1, Fx.neoplasmSplat, Fx.titanSmoke),
                player -> Effects.at(Fx.neoplasmHeal, player)
        ),

        teleport("Teleport",
                player -> Effects.stack(player, Fx.teleport, Fx.teleportActivate, Fx.teleportOut),
                player -> Effects.stack(player, Fx.teleport, Fx.teleportActivate, Fx.teleportOut),
                player -> Effects.at(Fx.smeltsmoke, player, Color.purple)
        ),

        impactDrill("Impact Drill",
                player -> Effects.stack(player, 120f, Fx.mineImpactWave, Fx.mineImpactWave, Fx.mineImpactWave, Fx.mineImpact),
                player -> Effects.stack(player, 120f, Fx.mineImpactWave, Fx.mineImpactWave, Fx.mineImpactWave, Fx.mineImpact),
                player -> Effects.at(Fx.mineSmall, player, Color.cyan)
        ),

        greenLaser("Green Laser",
                player -> Effects.at(Fx.greenBomb, player),
                player -> Effects.at(Fx.greenLaserCharge, player),
                player -> Effects.at(Fx.electrified, player)
        ),

        none("effects.disabled", "effects.disable");

        public final String name, button;
        public final Cons<Player> join, leave, move;

        EffectsPack(String name, Cons<Player> join, Cons<Player> leave, Cons<Player> move) {
            this(name, name, join, leave, move);
        }

        EffectsPack(String name, String button) {
            this(name, button, player -> {}, player -> {}, player -> {});
        }

        EffectsPack(String name, String button, Cons<Player> join, Cons<Player> leave, Cons<Player> move) {
            this.name = name;
            this.button = button;

            this.join = join;
            this.leave = leave;
            this.move = move;
        }

        public String name(MenuView menu) {
            return Bundle.get(name, menu.player);
        }

        @Override
        public void option(MenuView menu) {
            menu.option(button, Action.then(view -> updatePlayerData(view.player, data -> data.effects = this), view -> updateEffects(view.player, this), Action.showUse(DATA, data -> data.effects = this)));
        }
    }

    // endregion
}