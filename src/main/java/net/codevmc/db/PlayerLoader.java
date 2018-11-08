package net.codevmc.db;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import net.codevmc.lobby.Lobby;
import org.bson.Document;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.Closeable;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.UnknownHostException;
import java.util.UUID;

public class PlayerLoader implements Closeable {

    private final Lobby lobby;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private static final String BUCKET_NAME = "playerData";
    private static final String META_PLAYER_UUID_FIELD_NAME = "playerUUID";
    private static final String COLL_PLAYER_UUID_FIELD_NAME_QUALIFIED = "metadata." + META_PLAYER_UUID_FIELD_NAME;

    public PlayerLoader(Lobby lobby, UUID nodeId) {
        this.lobby = lobby;
    }

    public void connect(MongoClientURI uri, String dbName) {
        if(mongoClient != null) throw new IllegalStateException("Cannot connect after connected.");
        mongoClient = new MongoClient(uri);
        database = mongoClient.getDatabase(dbName);
    }

    public boolean loadPlayer(UUID playerUUID) throws FileNotFoundException {
        GridFSBucket bucket = GridFSBuckets.create(database, BUCKET_NAME);
        GridFSFindIterable iterable = bucket.find(Filters.eq(COLL_PLAYER_UUID_FIELD_NAME_QUALIFIED, playerUUID));
        GridFSFile remoteFile = iterable.first();
        if(remoteFile != null) {
            bucket.downloadToStream(remoteFile.getObjectId(), new FileOutputStream(lobby.getPlayerDataUtils().getPlayerFile(playerUUID)));
            bucket.delete(remoteFile.getObjectId());
            return true;
        } else {
            return false;
        }
    }

    public boolean unloadPlayer(Player player) throws FileNotFoundException {
        GridFSBucket bucket = GridFSBuckets.create(database, BUCKET_NAME);
        GridFSFindIterable iterable = bucket.find(Filters.eq(COLL_PLAYER_UUID_FIELD_NAME_QUALIFIED, player.getUniqueId()));
        if(iterable.first() != null) return false;
        player.saveData();
        File playerFile = lobby.getPlayerDataUtils().getPlayerFile(player.getUniqueId());
        bucket.uploadFromStream(playerFile.getName(), new FileInputStream(playerFile), new GridFSUploadOptions().metadata(new Document(META_PLAYER_UUID_FIELD_NAME, player.getUniqueId())));
        return true;
    }

    public void removePlayer(UUID playerUUID) {
        GridFSBucket bucket = GridFSBuckets.create(database, BUCKET_NAME);
        GridFSFindIterable iterable = bucket.find(Filters.eq(COLL_PLAYER_UUID_FIELD_NAME_QUALIFIED, playerUUID));
        for(GridFSFile file : iterable) {
            bucket.delete(file.getObjectId());
        }
    }

    @Override
    public void close() {
        mongoClient.close();
        mongoClient = null;
    }

}
