package darkdustry.database.models;

import arc.util.Strings;
import darkdustry.database.Database;
import darkdustry.features.Ranks.Rank;
import darkdustry.features.menus.MenuHandler.*;
import dev.morphia.annotations.*;
import lombok.*;

@Entity(value = "players", useDiscriminator = false)
@Indexes({
        @Index(fields = @Field("uuid")),
        @Index(fields = @Field("_id"))
})
@Data
@NoArgsConstructor
public class PlayerData {
    public String uuid;
    public String name = "<unknown>";

    @Id
    public int id;

    public boolean alerts = true;
    public boolean history = false;
    public boolean welcomeMessage = true;
    public boolean discordLink = true;

    public Language language = Language.off;
    public EffectsPack effects = EffectsPack.none;

    public int playTime = 0;
    public int blocksPlaced = 0;
    public int blocksBroken = 0;
    public int gamesPlayed = 0;
    public int wavesSurvived = 0;

    public int attackWins = 0;
    public int castleWins = 0;
    public int fortsWins = 0;
    public int hexedWins = 0;
    public int msgoWins = 0;
    public int pvpWins = 0;

    public Rank rank = Rank.player;

    public PlayerData(String uuid) {
        this.uuid = uuid;
    }

    public void generateID() {
        this.id = Database.generateNextID("players");
    }

    public String plainName() {
        return Strings.stripColors(name);
    }
}