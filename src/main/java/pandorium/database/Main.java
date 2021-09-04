package pandorium.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;

import org.bson.Document;

import pandorium.models.PlayerInfo;

public class Main {
    public static void main(String[] args) {
//        ConnectionString connString = new ConnectionString("mongodb+srv://host:BmnP4NEpht8wQFqv@darkdustry.aztzv.mongodb.net");
//
//        MongoClientSettings settings = MongoClientSettings.builder()
//            .applyConnectionString(connString)
//            .retryWrites(true)
//            .build();
//        MongoClient mongoClient = MongoClients.create(settings);
//        MongoDatabase database = mongoClient.getDatabase("darkdustry");
//        MongoCollection<Document> collection = database.getCollection("statistics");
//
//        PlayerInfo statistics = new PlayerInfo(collection);

//        collection.countDocuments().subscribe(new ArrowSubscriber<Long>(
//                subscribe -> {
//                    subscribe.request(1000);
//                },
//                next -> {
//
//                },
//                null, null
//        ));
//        collection.find().subscribe(new ArrowSubscriber<>(
//            subscribe -> subscribe.request(Long.MAX_VALUE),
//            next -> {
//                String json = next.toJson();
//                json.replace('s', 's');
//            },
//                (s) -> {
//                System.out.print(1111);
//                },
//            null
//        ));
//
//        System.out.println(1);
    }
}
