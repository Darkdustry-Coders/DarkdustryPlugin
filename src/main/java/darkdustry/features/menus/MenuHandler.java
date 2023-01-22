package darkdustry.features.menus;

import arc.func.*;
import arc.struct.Seq;
import darkdustry.features.Ranks;
import mindustry.gen.*;
import useful.Bundle;
import useful.menu.view.*;
import useful.menu.view.Menu.*;
import useful.menu.view.State.StateKey;

import static darkdustry.PluginVars.*;
import static darkdustry.components.Database.*;
import static darkdustry.utils.Administration.ban;
import static darkdustry.utils.Utils.*;
import static mindustry.net.Administration.Config.serverName;
import static useful.Bundle.bundled;

public class MenuHandler {

    // region menus

    public static final Menu
            listMenu = new Menu(),
            statsMenu = new Menu(),
            welcomeMenu = new Menu(),
            despawnMenu = new Menu(),
            tempbanMenu = new Menu(),
            settingsMenu = new Menu();

    // endregion
    // region keys

    public static final StateKey<Integer>
            PAGE = new StateKey<>("page", Integer.class),
            PAGES = new StateKey<>("pages", Integer.class);

    public static final StateKey<Boolean>
            REQUIREMENTS = new StateKey<>("requirements", Boolean.class),
            LANGUAGES = new StateKey<>("languages", Boolean.class);

    public static final StateKey<Player> TARGET = new StateKey<>("target", Player.class);
    public static final StateKey<PlayerData> DATA = new StateKey<>("data", PlayerData.class);

    // endregion
    // region transforms

    public static void load() {
        listMenu.transform(menu -> {
            menu.addOption("ui.button.left", Action.showGet(PAGE, page -> Math.max(1, page - 1)));
            menu.addOption("ui.button.page", Action.show(), menu.state.get(PAGE), menu.state.get(PAGES));
            menu.addOption("ui.button.right", Action.showGet(PAGE, page -> Math.min(page + 1, menu.state.get(PAGES)))).row();

            menu.addOption("ui.button.close");
        });

        statsMenu.transformIf(REQUIREMENTS, menu -> {
            var builder = new StringBuilder();
            Ranks.all.each(rank -> rank.requirements != null, rank -> builder.append(rank.localisedRequirements(menu.player)).append("\n"));

            menu.title("stats.requirements.title");
            menu.content(builder.toString());

            menu.addOption("ui.button.back", Action.showWith(REQUIREMENTS, false));
            menu.addOption("ui.button.close");
        }, menu -> {
            menu.addOption("stats.requirements.show", Action.showWith(REQUIREMENTS, true)).row();
            menu.addOption("ui.button.close");
        });

        welcomeMenu.transform(menu -> {
            var builder = new StringBuilder();
            welcomeMessageCommands.each(command -> builder.append("\n[cyan]/").append(command).append("[gray] - [lightgray]").append(Bundle.get("commands." + command + ".description", menu.player)));

            menu.title("welcome.title");
            menu.content("welcome.content", serverName.string(), builder.toString());

            menu.addOption("ui.button.close").row();
            menu.addOption("ui.button.discord", Action.uri(discordServerUrl)).row();
            menu.addOptionPlayer("ui.button.disable", player -> {
                updatePlayerData(player, data -> data.welcomeMessage = false);
                bundled(player, "welcome.disabled");
            });
        });

        despawnMenu.transform(menu -> {
            menu.title("despawn.title");
            menu.content("despawn.content");

            menu.addOptionsRow(1, DespawnType.values()).row();
            menu.addOptionPlayer("despawn.suicide", player -> {
                Call.unitEnvDeath(player.unit());
                bundled(player, "despawn.success.suicide");
            }).row();

            menu.addOption("ui.button.close");
        });

        tempbanMenu.transform(menu -> {
            menu.title("tempban.title");
            menu.content("tempban.content", menu.state.get(TARGET).coloredName());

            menu.addOptionsRow(3, BanDuration.values()).row();

            menu.addOption("ui.button.close");
        });

        settingsMenu.transformIf(LANGUAGES, menu -> {
            menu.title("language.title");
            menu.content("language.content", menu.state.get(DATA).language);

            menu.addOptionsRow(3, Language.values()).row();

            menu.addOption("ui.button.back", Action.showWith(LANGUAGES, false));
            menu.addOption("ui.button.close");
        }, menu -> {
            menu.title("settings.title");
            menu.content("settings.content");

            menu.addOptionsRow(1, Setting.values()).row();
            menu.addOption("settings.translator", Action.showWith(LANGUAGES, true), menu.state.get(DATA).language).row();

            menu.addOption("ui.button.close");
        });
    }

    // endregion
    // region show

    public static <T> void showListMenu(Player player, String title, Seq<T> content, int page, int pages, Cons3<StringBuilder, Integer, T> cons) {
        listMenu.showWith(player, PAGE, page, PAGES, pages, menu -> {
            menu.title(title);
            menu.content(formatList(content, menu.state.get(PAGE), cons));
        });
    }

    public static void showStatsMenu(Player player, Player target, PlayerData data) {
        statsMenu.showWithIfNot(player, REQUIREMENTS, false, menu -> {
            menu.title("stats.title");
            menu.content("stats.content", target.coloredName(), data.rank.localisedName(player), data.rank.localisedDesc(player), data.blocksPlaced, data.blocksBroken, data.gamesPlayed, data.wavesSurvived, data.pvpWins, data.pvpLosses, formatDuration(data.playTime * 60 * 1000L, player));
        });
    }

    public static void showPromotionMenu(Player player, PlayerData data) {
        statsMenu.showWithIfNot(player, REQUIREMENTS, false, menu -> {
            menu.title("stats.promotion.title");
            menu.content("stats.promotion.content", data.rank.localisedName(player), data.rank.localisedDesc(player));
        });
    }

    public static void showWelcomeMenu(Player player) {
        welcomeMenu.show(player);
    }

    public static void showDespawnMenu(Player player) {
        despawnMenu.show(player);
    }

    public static void showTempbanMenu(Player player, Player target) {
        tempbanMenu.showWith(player, TARGET, target);
    }

    public static void showSettingsMenu(Player player, PlayerData data) {
        settingsMenu.showWith(player, LANGUAGES, false, DATA, data);
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
        public MenuOption option(MenuView menu) {
            return menu.option("tempban." + name(), view -> ban(view.player, view.state.get(TARGET), duration));
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
        public MenuOption option(MenuView menu) {
            return menu.optionPlayer("despawn." + name(), player -> {
                Groups.unit.each(unit -> filter.get(player, unit), Call::unitEnvDeath);
                bundled(player, "despawn.success");
            }, Groups.unit.count(unit -> filter.get(menu.player, unit)));
        }
    }

    public enum Setting implements OptionData {
        alerts(data -> data.alerts = !data.alerts, data -> data.alerts),
        effects(data -> data.effects = !data.effects, data -> data.effects),
        history(data -> data.history = !data.history, data -> data.history),
        welcomeMessage(data -> data.welcomeMessage = !data.welcomeMessage, data -> data.welcomeMessage);

        public final Cons<PlayerData> cons;
        public final Func<PlayerData, Boolean> func;

        Setting(Cons<PlayerData> cons, Func<PlayerData, Boolean> func) {
            this.cons = cons;
            this.func = func;
        }

        @Override
        public MenuOption option(MenuView menu) {
            return menu.option("settings." + name(), Action.player(player -> updatePlayerData(player, cons)).then(Action.showConsume(DATA, cons)), Bundle.get(func.get(menu.state.get(DATA)) ? "settings.on" : "settings.off", menu.player));
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

        off("off", "language.off");

        public final String code, name;

        Language(String code, String name) {
            this.code = code;
            this.name = name;
        }

        @Override
        public MenuOption option(MenuView menu) {
            return menu.option(name, Action.player(player -> updatePlayerData(player, data -> data.language = code)).then(Action.showConsume(DATA, data -> data.language = code)));
        }
    }

    // endregion
}