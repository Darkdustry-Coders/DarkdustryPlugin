package pandorum.database.models;

import arc.func.Cons;
import com.mongodb.BasicDBObject;
import com.mongodb.reactivestreams.client.MongoCollection;
import mindustry.maps.Map;
import org.bson.Document;
import pandorum.database.MongoDataBridge;

public class MapModel extends MongoDataBridge<MapModel> {

    public MapModel(MongoCollection<Document> collection) {
        super(collection);
    }

    public MapModel() {}

    public String name;

    public int upVotes = 0;
    public int downVotes = 0;

    public int playTime = 0;
    public int gamesPlayed = 0;
    public int bestWave = 0;

    public void find(Map map, Cons<MapModel> cons) {
        if (map != null && map.hasTag("name")) find(map.name(), cons);
    }

    public void find(String name, Cons<MapModel> cons) {
        findAndApplySchema(new BasicDBObject("name", name), cons);
    }
}
