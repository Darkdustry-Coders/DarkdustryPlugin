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

    public String desc;
    public String params;
    private Cons<String[]> runner;

    private ServerCommands(String desc, Cons<String[]> runner) {
        this(desc, "", runner);
    }

    private ServerCommands(String desc, String params, Cons<String[]> runner) {
        this.desc = desc;
        this.params = params;
        this.runner = runner;
    }

    @Override
    public void get(String[] args) {
        runner.get(args);
    }
}