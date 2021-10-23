package pandorum;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.apache.log4j.PropertyConfigurator;
import org.bson.Document;
import pandorum.models.PlayerModel;

import java.util.Properties;

public class Main {
    public static void main(String[] args) throws IllegalArgumentException {
        Properties log4j = new Properties();

        log4j.setProperty("log4j.rootLogger", "OFF, stdout");
        log4j.setProperty("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
        log4j.setProperty("log4j.appender.stdout.Target", "System.out");
        log4j.setProperty("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
        log4j.setProperty("log4j.appender.stdout.layout.ConversionPattern", "%d{yy/MM/dd HH:mm:ss} %p %c{2}: %m%n");

        PropertyConfigurator.configure(log4j);

        ConnectionString connString = new ConnectionString("mongodb+srv://host:BmnP4NEpht8wQFqv@darkdustry.aztzv.mongodb.net");
        
        MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(connString)
            .retryWrites(true)
            .build();

        MongoClient mongoClient = MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase("darkdustry");
        MongoCollection<Document> collection = database.getCollection("statistics");

        PlayerModel.setSourceCollection(collection);
    }
}
