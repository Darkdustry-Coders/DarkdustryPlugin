package pandorum.comp;

import arc.util.Log;
import io.ipinfo.api.errors.ErrorResponseException;
import io.ipinfo.api.errors.RateLimitedException;
import io.ipinfo.api.model.IPResponse;
import pandorum.PandorumPlugin;

public class AntiVpn {

    public static boolean enabled = true;

    /**
     * @param ip - Айпи, который нужно проверить
     * @return - Является ли этот айпи VPN
     */
    public static boolean checkIP(String ip) {
        if (enabled) {
            try {
                IPResponse response = PandorumPlugin.ipInfo.lookupIP(ip);
                return response.getPrivacy().getVpn();
            } catch (RateLimitedException e) {
                Log.info("Лимит запросов ANTI-VPN исчерпан. Выключаю ANTI-VPN...");
                enabled = false;
            } catch (ErrorResponseException ignored) {}
        }

        return false;
    }
}
