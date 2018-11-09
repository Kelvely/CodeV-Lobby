package net.codevmc.db;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import net.codevmc.lobby.Lobby;

import java.io.Closeable;

public abstract class MongoConnector implements Closeable {

    private MongoClient mongoClient;
    private MongoDatabase database;
    private boolean connects;


    public void connect(MongoClientURI uri, String dbName) {
        if(mongoClient != null) throw new IllegalStateException("Cannot connect after connected.");
        setConnection(new MongoClient(uri), dbName);
    }

    public void setConnection(MongoClient client, String dbName) {
        if(mongoClient != null && connects) {
            mongoClient.close();
        }
        mongoClient = client;
        database = client.getDatabase(dbName);
    }

    @Override
    public void close() {
        if(!connects) throw new IllegalStateException("Don't close a player loader if it didn't create the connection.");
        mongoClient.close();
        mongoClient = null;
    }

    protected MongoClient getMongo() {
        return mongoClient;
    }

    protected MongoDatabase getDatabase() {
        return database;
    }

    public boolean connects() {
        return connects;
    }
}
