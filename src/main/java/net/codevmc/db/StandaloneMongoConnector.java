package net.codevmc.db;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public class StandaloneMongoConnector extends MongoConnector {

    @Override
    public MongoClient getMongo() {
        return super.getMongo();
    }

    @Override
    public MongoDatabase getDatabase() {
        return super.getDatabase();
    }

}
