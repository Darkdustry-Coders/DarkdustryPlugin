package darkdustry.features.net;

import arc.util.Log;
import darkdustry.database.Database;
import darkdustry.database.models.Payment;
import net.darkness.Payok;

import java.util.Date;

import static darkdustry.config.Config.*;

public class PayokServer {

    public static void load() {
        Payok.setShopID(config.payokShopID);
        Payok.setSecretKey(config.payokSecretKey);

        if (config.mode.isMainServer)
            Payok.handlePaymentResponse("/payment/", Log::info);
    }

    public static String generatePaymentLink(int amount, int playerID, String currency, String desc) {
        var payment = Payment.builder()
                .amount(amount)
                .playerID(playerID)
                .currency(currency)
                .desc(desc)
                .dateCreated(new Date())
                .build();

        payment.generateID();
        payment.generateSign();

        return Payok.generatePaymentLink(Database.savePayment(payment));
    }
}