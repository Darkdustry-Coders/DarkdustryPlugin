package rewrite.commands;

import arc.func.Cons;

public enum ServerCommands implements Cons<String[]> {
    help("Список всех команд.", (args) -> {

    }),
    version("Информация о версии сервера.", (args) -> {

    }),
    exit("Выключить сервер.", (args) -> {

    }),
    stop("Остановить сервер.", (args) -> {

    }),
    host("Запустить сервер на выбранной карте.", "[карта] [режим]", (args) -> {

    });

    public final String description;
    public final String params;
    private final Cons<String[]> runner;

    ServerCommands(String description, Cons<String[]> runner) {
        this(description, "", runner);
    }

    ServerCommands(String description, String params, Cons<String[]> runner) {
        this.description = description;
        this.params = params;
        this.runner = runner;
    }

    @Override
    public void get(String[] args) {
        runner.get(args);
    }
}