package pandorum.database.events;

import com.mongodb.client.result.InsertOneResult;

public class OnNext {
    public InsertOneResult insertResult;
    
    public OnNext(InsertOneResult insertOneResult) {
        this.insertResult = insertOneResult;
    }
}