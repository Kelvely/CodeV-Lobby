package net.codevmc.util;

import org.bukkit.Server;
import sun.reflect.misc.ReflectUtil;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

import java.io.File;

public class PlayerDataUtils {

    private final File playerDataFolder;

    public PlayerDataUtils(Server server) throws NoSuchFieldException, IllegalAccessException {
        Class<?> craftServerClass = server.getClass();
        Field playerListField = getFieldDeeply(craftServerClass, "playerList");
        Object playerList = playerListField.get(server);

        Class<?> playerListClass = playerList.getClass();
        Field playerFileDataField = getFieldDeeply(playerListClass, "playerFileData");
        Object playerFileData = playerFileDataField.get(playerList);

        Class<?> playerFileDataClass = playerFileData.getClass();
        Field playerDirField = getFieldDeeply(playerFileDataClass, "playerDir");
        playerDataFolder = (File) playerDirField.get(playerFileData);
    }

    public File getPlayerFile(UUID uuid) {
        return new File(playerDataFolder, uuid.toString() + ".dat");
    }

    private static Field getFieldDeeply(Class<?> cls, String field) throws NoSuchFieldException {
        try {
            return cls.getDeclaredField(field);
        } catch (NoSuchFieldException ex) {
            Class<?> superClass = cls.getSuperclass();
            if(superClass != null) {
                return getFieldDeeply(superClass, field);
            } else throw ex;
        }
    }

    private static Method getMethodDeeply(Class<?> cls, String method, Class<?> parameterTypes) throws NoSuchMethodException {
        try {
            return cls.getDeclaredMethod(method, parameterTypes);
        } catch (NoSuchMethodException ex) {
            Class<?> superClass = cls.getSuperclass();
            if(superClass != null) {
                return getMethodDeeply(superClass, method, parameterTypes);
            } else throw ex;
        }
    }

}
