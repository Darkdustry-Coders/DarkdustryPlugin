package pandorum.features;

import arc.func.Cons;
import arc.util.Http;
import arc.util.Log;
import com.google.gson.JsonObject;

import static pandorum.PluginVars.*;

public class Translator {

    public static void translate(String text, String to, Cons<String> cons) {
        JsonObject json = new JsonObject();
        json.addProperty("to", to);
        json.addProperty("text", text);
        json.addProperty("platform", "api");
        json.addProperty("enableTransliteration", true);

        Http.post("https://api-b2b.backenster.com/b1/api/v3/translate")
                .header("authorization", translatorApiKey)
                .header("content-type", "application/json")
                .content(json.toString())
                .error(e -> cons.get(""))
                .submit(response -> cons.get(gson.fromJson(response.getResultAsString(), JsonObject.class).get("result").getAsString()));
    }

    public static void loadLanguages() {
        translatorLanguages.addAll(
                new Language("in", "id_ID", "Bahasa Indonesia"),
                new Language("da", "da_DK", "Dansk"),
                new Language("de", "de_DE", "Deutsch"),
                new Language("et", "et_EE", "Eesti"),
                new Language("en", "en_GB", "English"),
                new Language("es", "es_ES", "Español"),
                new Language("eu", "eu_ES", "Euskara"),
                new Language("it", "it_IT", "Italiano"),
                new Language("lt", "lt_LT", "Lietuvių"),
                new Language("hu", "hu_HU", "Magyar"),
                new Language("nl", "nl_NL", "Nederlands"),
                new Language("pl", "pl_PL", "Polski"),
                new Language("pt", "pt_PT", "Português"),
                new Language("ro", "ro_RO", "Română"),
                new Language("fi", "fi_FI", "Suomi"),
                new Language("sv", "sv_SE", "Svenska"),
                new Language("vi", "vi_VN", "Tiếng Việt"),
                new Language("tk", "tk_TK", "Türkmen dili"),
                new Language("tr", "tr_TR", "Türkçe"),
                new Language("cs", "cs_CZ", "Čeština"),
                new Language("be", "be_BY", "Беларуская"),
                new Language("bg", "bg_BG", "Български"),
                new Language("ru", "ru_RU", "Русский"),
                new Language("uk", "uk_UA", "Українська"),
                new Language("th", "th_TH", "ไทย"),
                new Language("zh_CN", "zh-Hans_CN", "简体中文"),
                new Language("ja", "ja_JP", "日本語"),
                new Language("ko", "ko_KR", "한국어")
        );

        Log.info("[Darkdustry] Загружено @ языков для перевода.", translatorLanguages.size);
    }

    public static record Language(String code, String fullCode, String name) {}

    public static Language getLanguageByCode(String code) {
        return translatorLanguages.find(language -> language.code().equals(code));
    }
}
