package net.codevmc.db;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.gridfs.GridFS;
import net.codevmc.lobby.Lobby;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.Closeable;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.UUID;

public class PlayerLoader extends MongoConnector {

    private final Lobby lobby;
    private static final String BUCKET_NAME = "playerData";
    private static final String META_PLAYER_UUID_FIELD_NAME = "playerUUID";
    private static final String COLL_PLAYER_UUID_FIELD_NAME_QUALIFIED = "metadata." + META_PLAYER_UUID_FIELD_NAME;

    public PlayerLoader(Lobby lobby, UUID nodeId) {
        this.lobby = lobby;
    }

    public boolean loadPlayer(UUID playerUUID) throws FileNotFoundException {
        GridFSBucket bucket = GridFSBuckets.create(getDatabase(), BUCKET_NAME);
        GridFSFindIterable iterable = bucket.find(Filters.eq(COLL_PLAYER_UUID_FIELD_NAME_QUALIFIED, playerUUID));
        for (GridFSFile file : iterable) {
            if(file.getFilename().endsWith(".tmp")) throw new IllegalStateException("The player's file encountered disaster and need to be repaired.");
        }
        GridFSFile remoteFile = iterable.first();
        if(remoteFile != null) {
            bucket.downloadToStream(remoteFile.getObjectId(), new FileOutputStream(lobby.getPlayerDataUtils().getPlayerFile(playerUUID)));
            return true;
        } else {
            return false;
        }
    }

    public boolean unloadPlayer(Player player) throws FileNotFoundException {
        GridFSBucket bucket = GridFSBuckets.create(getDatabase(), BUCKET_NAME);
        GridFSFindIterable previousFiles = bucket.find(Filters.eq(COLL_PLAYER_UUID_FIELD_NAME_QUALIFIED, player.getUniqueId()));
        player.saveData();
        File playerFile = lobby.getPlayerDataUtils().getPlayerFile(player.getUniqueId());
        ObjectId current = bucket.uploadFromStream(playerFile.getName() + ".tmp", new FileInputStream(playerFile), new GridFSUploadOptions().metadata(new Document(META_PLAYER_UUID_FIELD_NAME, player.getUniqueId())));
        for(GridFSFile previousFile : previousFiles) {
            bucket.delete(previousFile.getObjectId());
        }
        bucket.rename(current, playerFile.getName());
        return true;
    }

    public void removePlayer(UUID playerUUID) {
        GridFSBucket bucket = GridFSBuckets.create(getDatabase(), BUCKET_NAME);
        GridFSFindIterable iterable = bucket.find(Filters.eq(COLL_PLAYER_UUID_FIELD_NAME_QUALIFIED, playerUUID));
        for(GridFSFile file : iterable) {
            bucket.delete(file.getObjectId());
        }
    }



}
