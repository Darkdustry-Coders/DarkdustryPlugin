package darkdustry.database.models;

import darkdustry.database.Database;
import dev.morphia.annotations.*;
import lombok.*;
import net.darkness.Payok;
import net.darkness.request.PaymentRequest;

import java.util.Date;

@Entity(value = "payments", useDiscriminator = false)
@Indexes({
        @Index(fields = @Field("playerID")),
        @Index(fields = @Field("_id")),
        @Index(
                fields = @Field("dateCreated"),
                options = @IndexOptions(expireAfterSeconds = 86400)
        )
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment implements PaymentRequest {
    public int amount;
    public int playerID;

    @Id
    public int paymentID;

    public String currency;
    public String desc;

    public Date dateCreated;
    public String sign;

    public void generateID() {
        this.paymentID = Database.generateNextID("payments");
    }

    public void generateSign() {
        this.sign = Payok.generateSign(amount, paymentID, currency, desc);
    }
}