package pandorum.comp;

import org.json.JSONArray;
import org.json.JSONObject;
import okhttp3.*;

import java.io.IOException;
import java.util.*;

import pandorum.PandorumPlugin;

public class Translator {

    public static JSONObject translate(String text, String dest_lang) throws IOException, InterruptedException {
        String destAlphaLang = PandorumPlugin.codeLanguages.get("ru");

        if (PandorumPlugin.codeLanguages.containsKey(dest_lang)) destAlphaLang = PandorumPlugin.codeLanguages.get(dest_lang);

        RequestBody formBody = new FormBody.Builder()
                .add("to", destAlphaLang)
                .add("text", text)
                .add("platform", "dp")
                .build();
        Request request = new Request.Builder()
                .url("https://api-b2b.backenster.com/b1/api/v3/translate/")
                .post(formBody)
                .addHeader("accept", "application/json, text/javascript, */*; q=0.01")
                .addHeader("accept-language", "ru,en;q=0.9")
                .addHeader("authorization", "Bearer a_25rccaCYcBC9ARqMODx2BV2M0wNZgDCEl3jryYSgYZtF1a702PVi4sxqi2AmZWyCcw4x209VXnCYwesx")
                .addHeader("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
                .addHeader("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"90\", \"Yandex\";v=\"90\"")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-site", "cross-site")
                .build();
        Response response = PandorumPlugin.client.newCall(request).execute();
        ResponseBody responseBody = response.body();

        if (Objects.isNull(responseBody)) return new JSONObject("{}");
        return new JSONObject(responseBody.string());
    }

    public static JSONArray getAllLanguages() throws IOException {
        Request request = new Request.Builder()
                .url("https://api-b2b.backenster.com/b1/api/v3/getLanguages?platform=dp")
                .get()
                .addHeader("accept", "application/json, text/javascript, */*; q=0.01")
                .addHeader("accept-language", "ru,en;q=0.9")
                .addHeader("authorization", "Bearer a_25rccaCYcBC9ARqMODx2BV2M0wNZgDCEl3jryYSgYZtF1a702PVi4sxqi2AmZWyCcw4x209VXnCYwesx")
                .addHeader("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
                .addHeader("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"90\", \"Yandex\";v=\"90\"")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-site", "cross-site")
                .addHeader("if-none-match", "W/\"aec6-7FjvQqCRl/1E+dvnCAlbAedDteg\"")
                .build();
        Response response = PandorumPlugin.client.newCall(request).execute();
        ResponseBody responseBody = response.body();

        if (Objects.isNull(responseBody)) {
            return new JSONArray("[]");
        }

        String bodyString = responseBody.string();
        return new JSONObject(bodyString).getJSONArray("result");
    }
}
