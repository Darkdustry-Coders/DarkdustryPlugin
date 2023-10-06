package darkdustry.database.models;

import arc.util.Time;
import darkdustry.database.Database;
import dev.morphia.annotations.*;
import lombok.*;

import java.util.Date;

@Entity("bans")
@Indexes({
        @Index(fields = @Field("uuid")),
        @Index(fields = @Field("ip")),
        @Index(fields = @Field("_id")),
        @Index(
                fields = @Field("unbanDate"),
                options = @IndexOptions(expireAfterSeconds = 0)
        )
})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ban {
    public String uuid, ip;
    public String player, admin;

    @Id
    public int id;

    public String reason;
    public Date unbanDate;

    public void generateID() {
        this.id = Database.generateNextID(Ban.class);
    }

    public boolean expired() {
        return unbanDate.getTime() < Time.millis();
    }

    public long remaining() {
        return unbanDate.getTime() - Time.millis();
    }
}