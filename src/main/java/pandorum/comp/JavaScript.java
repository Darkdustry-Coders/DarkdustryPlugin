package pandorum.comp;

import arc.struct.Seq;

public class JavaScript {
    private static final Seq<String> blacklist = Seq.with(".net.", "java.net", "files", "reflect", "javax", "rhino", "file", "channels", "jdk", "exit", "reset",
            "runtime", "util.os", "rmi", "security", "org.", "sun.", "beans", "sql", "http", "exec", "compiler", "process", "system", "app", "exception", "error",
            ".awt", "socket", "classloader", "oracle", "invoke", "java.util.function", "java.util.stream", "org.", "mod.classmap", "config", "dispose", "delete",
            "ban", "kick", "uuid", "packet", "while", "connect", "file", "settings", "throw"
    );

    public static boolean allowScript(String text) {
        return !blacklist.contains(t -> text.toLowerCase().contains(t));
    }
}
