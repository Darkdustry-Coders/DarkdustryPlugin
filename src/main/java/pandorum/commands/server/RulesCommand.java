package pandorum.commands.server;

import arc.Core;
import arc.util.Log;
import arc.util.serialization.JsonReader;
import arc.util.serialization.JsonValue;
import arc.util.serialization.JsonValue.ValueType;
import mindustry.gen.Call;
import mindustry.io.JsonIO;

import static mindustry.Vars.state;

public class RulesCommand {
    public static void run(final String[] args) {
        if (args.length == 0) {
            Log.info("Пользовательские правила:\n@", JsonIO.print(Core.settings.getString("globalrules")));
            return;
        }

        if (args.length == 1) {
            Log.err("Неверное использование команды. Необходимо выбрать, какое пользовательское правило добавить или убрать.");
            return;
        }

        JsonValue base = JsonIO.json.fromJson(null, Core.settings.getString("globalrules"));
        switch (args[0].toLowerCase()) {
            case "add" -> {
                if (args.length < 3) {
                    Log.err("Не хватает последнего аргумента. Необходимо выбрать, какое значение присвоить пользовательскому правилу.");
                    return;
                }

                try {
                    JsonValue value = new JsonReader().parse(args[2]);
                    value.name = args[1];

                    JsonValue parent = new JsonValue(ValueType.object);
                    parent.addChild(value);

                    JsonIO.json.readField(state.rules, value.name, parent);
                    if (base.has(value.name)) base.remove(value.name);
                    base.addChild(args[1], value);
                    Log.info("Пользовательское правило изменено: @", value.toString().replace("\n", " "));
                } catch (Exception e) {
                    Log.err("Ошибка при изменении пользовательского правила: @", e.getMessage());
                    return;
                }
            }
            case "remove" -> {
                if (base.has(args[1])) {
                    base.remove(args[1]);
                    Log.info("Пользовательское правило '@' убрано.", args[1]);
                } else {
                    Log.err("Такого пользовательского правила не существует.");
                    return;
                }
            }
            default -> {
                Log.err("Второй параметр должен быть или 'add' или 'remove'.");
                return;
            }
        }

        Core.settings.put("globalrules", base.toString());
        Call.setRules(state.rules);
    }
}
