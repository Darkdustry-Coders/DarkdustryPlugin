package darkdustry.features.net;

import net.darkness.Payok;
import net.darkness.request.SimplePaymentRequest;

import static darkdustry.config.Config.*;

public class PayokServer {

    public static void load() {
        Payok.setShopID(config.payokShopID);
        Payok.setSecretKey(config.payokSecretKey);

        if (config.mode.isMainServer)
            Payok.handlePaymentResponse("/payment/", response -> {

            });
    }

    public static String generatePaymentLink() {
        return Payok.generatePaymentLink(SimplePaymentRequest.builder().build());
    }
}