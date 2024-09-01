package darkdustry.database.models;

import arc.util.Time;
import darkdustry.database.Database;
import dev.morphia.annotations.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity(value = "mutes", useDiscriminator = false)
@Indexes({
        @Index(fields = @Field("uuid")),
        @Index(fields = @Field("_id")),
        @Index(
                fields = @Field("unmuteDate"),
                options = @IndexOptions(expireAfterSeconds = 0)
        )
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mute {
    public String uuid;

    public String playerName, adminName;
    public int playerID;

    @Id
    public int id;

    public String reason;
    public Date unmuteDate;

    public void generateID() {
        this.id = Database.generateNextID("mutes");
    }

    public void generatePlayerID() {
        this.playerID = Database.getPlayerData(uuid).id;
    }

    public boolean expired() {
        return unmuteDate.getTime() < Time.millis();
    }

    public long remaining() {
        return unmuteDate.getTime() - Time.millis();
    }
}
