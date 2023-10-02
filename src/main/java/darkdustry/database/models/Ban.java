package darkdustry.database.models;

import arc.util.Time;
import darkdustry.database.Database;
import lombok.*;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.Date;

import static com.mongodb.client.model.Filters.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ban {
    public String uuid, ip;
    public String player, admin;

    @BsonProperty("pid")
    public int id;

    public String reason;
    public Date unbanDate;

    public void generateID() {
        this.id = Database.players.getField(eq("uuid", uuid), "pid", -1);
    }

    public boolean expired() {
        return unbanDate.getTime() < Time.millis();
    }

    public long remaining() {
        return unbanDate.getTime() - Time.millis();
    }
}