package darkdustry.components;

import arc.util.Http;
import arc.util.serialization.JsonReader;
import darkdustry.utils.Utils;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Objects;

import static arc.util.Log.err;
import static arc.util.Log.info;

public class Updater {
    public void init() {
        var mod = Utils.getPlugin();

        var json = mod.root.child("plugin.json");
        var repo = mod.getRepo();

        if (json.exists() && repo != null) {
            var api = "https://api.github.com/repos/" + repo + "/releases/latest";
            var version = new JsonReader().parse(json).getString("version");

            Http.get(api, result -> {
                var tag = new JsonReader().parse(result.getResultAsString()).getString("tag_name");

                if (Objects.equals(version, tag)) {
                    info("Main plugin \"@\" has latest version: @", mod.name, version);
                } else {
                    var download = "https://github.com/" + repo + "/releases/download/" + tag + "/DarkdustryPlugin-" + tag + ".jar";
                    Http.get(download, response -> {
                        OutputStream outputStream = new FileOutputStream(mod.file.file());

                        outputStream.write(response.getResultAsStream().readAllBytes());
                        outputStream.close();

                        info("Main plugin \"@\" has been updated", mod.name);
                    });
                }
            });
        } else {
            err("Cannot find plugin.json or github repository in main plugin @", mod.name);
        }
    }
}
