package darkdustry.components;

import arc.util.Http;
import arc.util.serialization.*;
import darkdustry.DarkdustryPlugin;
import darkdustry.utils.Utils;

import java.io.*;
import java.util.Objects;

import static arc.util.Log.*;

public class Updater {
    public static void init() {
        info("Checking updates...");
        var mod = Utils.getPlugin();

        var json = mod.root.child("plugin.json");
        var repo = mod.getRepo();

        if (json.exists() && repo != null) {
            var api = "https://api.github.com/repos/" + repo + "/releases/latest";
            var version = Jval.read(json.reader()).getString("version");

            Http.get(api, result -> {
                var text = result.getResultAsString();
                var tag = new JsonReader().parse(text).getString("tag_name");

                if (Objects.equals(version, tag)) {
                    info("Main plugin \"@\" has latest version: @.", mod.name, version);
                } else {
                    info("Main plugin will be updated and disabled");
                    var download = "https://github.com/" + repo + "/releases/download/" + tag + "/DarkdustryPlugin-" + tag + ".jar";
                    // var download = Jval.read(text).get("assets").get("0").getString("browser_download_url");

                    Http.get(download, response -> {
                        OutputStream outputStream = new FileOutputStream(mod.file.file());

                        outputStream.write(response.getResultAsStream().readAllBytes());
                        outputStream.close();

                        info("Main plugin \"@\" has been updated", mod.name);

                        DarkdustryPlugin.exit();
                    });
                }
            });
        } else {
            err("Cannot find plugin.json or github repository in main plugin @", mod.name);
        }
    }
}
