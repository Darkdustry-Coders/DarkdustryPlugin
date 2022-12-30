package darkdustry.features.menus;

import arc.func.*;
import darkdustry.features.Ranks;
import mindustry.gen.*;
import useful.Bundle;
import useful.menu.view.*;
import useful.menu.view.Menu.*;
import useful.menu.view.State.StateKey;

import static darkdustry.PluginVars.*;
import static darkdustry.components.Database.*;
import static darkdustry.features.menus.MenuUtils.showMenuClose;
import static darkdustry.utils.Utils.formatDuration;
import static mindustry.net.Administration.Config.serverName;
import static useful.Bundle.bundled;

public class MenuHandler {

    // region menus

    public static Menu
            listMenu = new Menu(),
            statsMenu = new Menu(),
            welcomeMenu = new Menu(),
            despawnMenu = new Menu(),
            settingsMenu = new Menu(),
            languageMenu = new Menu();

    // endregion
    // region keys

    public static StateKey<Integer>
            PAGE = new StateKey<>("page", Integer.class),
            PAGES = new StateKey<>("pages", Integer.class);

    public static StateKey<PlayerData> DATA = new StateKey<>("data", PlayerData.class);

    // endregion
    // region transforms

    public static void load() {
        listMenu.transform(menu -> {
            menu.addOption("ui.button.left", Action.showGet(PAGE, page -> Math.max(1, page - 1)));
            menu.addOption("ui.button.page", Action.show(), menu.state.get(PAGE), menu.state.get(PAGES));
            menu.addOption("ui.button.right", Action.showGet(PAGE, page -> Math.min(page + 1, menu.state.get(PAGES)))).row();

            menu.addOptionNone("ui.button.close");
        });

        statsMenu.transform(menu -> {
            menu.title("stats.title");

            menu.addOptionPlayer("requirements.button", player -> {
                var builder = new StringBuilder();
                Ranks.all.each(rank -> rank.requirements != null, rank -> builder.append(rank.localisedReq(player)).append("\n"));

                showMenuClose(player, "requirements.title", builder.toString());
            }).row();

            menu.addOptionNone("ui.button.close");
        });

        welcomeMenu.transform(menu -> {
            var builder = new StringBuilder();
            welcomeMessageCommands.each(command -> builder.append("\n[cyan]").append(clientCommands.prefix).append(command).append("[gray] - [lightgray]").append(Bundle.get("commands." + command + ".description", menu.player)));

            menu.title("welcome.title");
            menu.content("welcome.content", serverName.string(), builder.toString());

            menu.addOptionNone("ui.button.close").row();
            menu.addOptionRow("ui.button.discord", Action.uri(discordServerUrl));
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

            menu.addOptionNone("ui.button.close");
        });

        settingsMenu.transform(menu -> {
            menu.title("settings.title");
            menu.content("settings.content");

            menu.addOptionsRow(1, Settings.values()).row();
            menu.addOptionRow("settings.translator", view -> showLanguageMenu(view.player, view.state.get(DATA)), menu.state.get(DATA).language);

            menu.addOptionNone("ui.button.close");
        });

        languageMenu.transform(menu -> {
            menu.title("language.title");
            menu.content("language.content", menu.state.get(DATA).language);

            menu.addOptionsRow(3, Language.values()).row();

            menu.addOption("ui.button.back", view -> showSettingsMenu(view.player, view.state.get(DATA)));
            menu.addOptionNone("ui.button.close");
        });
    }

    // endregion
    // region show

    public static void showListMenu(Player player, String title, Func<Integer, String> content, int page, int pages) {
        listMenu.showWith(player, PAGE, page, PAGES, pages, menu -> {
            menu.title(title);
            menu.content(content.get(menu.state.get(PAGE)));
        });
    }

    public static void showStatsMenu(Player player, Player target, PlayerData data) {
        statsMenu.show(player, menu -> menu.content("stats.content", target.coloredName(), data.rank.localisedName(player), data.rank.localisedDesc(player), data.blocksPlaced, data.blocksBroken, data.gamesPlayed, data.wavesSurvived, formatDuration(data.playTime * 60 * 1000L, player.locale)));
    }

    public static void showWelcomeMenu(Player player) {
        welcomeMenu.show(player);
    }

    public static void showDespawnMenu(Player player) {
        despawnMenu.show(player);
    }

    public static void showSettingsMenu(Player player, PlayerData data) {
        settingsMenu.showWith(player, DATA, data);
    }

    public static void showLanguageMenu(Player player, PlayerData data) {
        languageMenu.showWith(player, DATA, data);
    }

    // endregion
    // region enums

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

    public enum Settings implements OptionData {
        alerts(data -> data.alerts = !data.alerts, data -> data.alerts),
        effects(data -> data.effects = !data.effects, data -> data.effects),
        history(data -> data.history = !data.history, data -> data.history),
        welcomeMessage(data -> data.welcomeMessage = !data.welcomeMessage, data -> data.welcomeMessage);

        public final Cons<PlayerData> cons;
        public final Func<PlayerData, Boolean> func;

        Settings(Cons<PlayerData> cons, Func<PlayerData, Boolean> func) {
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