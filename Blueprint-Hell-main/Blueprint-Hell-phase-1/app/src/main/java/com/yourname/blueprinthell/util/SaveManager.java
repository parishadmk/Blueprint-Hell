package com.yourname.blueprinthell.util;

import com.yourname.blueprinthell.model.GameState;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Utility class responsible for saving and loading the {@link GameState}.
 * The game state is serialized and a SHA-256 checksum is stored alongside
 * the data to validate integrity during load operations. Concurrent save
 * and load requests are synchronized using a read/write lock.
 */
public class SaveManager {

    private static final ReentrantReadWriteLock LOCK = new ReentrantReadWriteLock();

    private SaveManager() {
        // Utility class
    }

    /**
     * Saves the provided game state to the given file. The operation is
     * protected by the write lock to prevent concurrent modifications while
     * a save is in progress.
     */
    public static void save(GameState state, String file) throws IOException {
        LOCK.writeLock().lock();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(state);
            oos.flush();
            byte[] data = baos.toByteArray();

            byte[] checksum = checksum(data);
            ObjectOutputStream fileOut = new ObjectOutputStream(new FileOutputStream(file));
            fileOut.writeObject(new SaveData(data, checksum));
            fileOut.close();
        } finally {
            LOCK.writeLock().unlock();
        }
    }

    /**
     * Loads a game state from the specified file. The read lock allows
     * multiple concurrent load operations while preventing saves.
     */
    public static GameState load(String file) throws IOException, ClassNotFoundException {
        LOCK.readLock().lock();
        try {
            ObjectInputStream fileIn = new ObjectInputStream(new FileInputStream(file));
            SaveData data = (SaveData) fileIn.readObject();
            fileIn.close();

            byte[] expected = checksum(data.stateBytes);
            if (!Arrays.equals(expected, data.checksum)) {
                throw new IOException("Save data corrupted");
            }

            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data.stateBytes));
            return (GameState) ois.readObject();
        } finally {
            LOCK.readLock().unlock();
        }
    }

    private static byte[] checksum(byte[] data) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("Unable to compute checksum", e);
        }
    }

    /**
     * Simple wrapper class used for storing the serialized game state and
     * its checksum. This class is kept private to avoid leaking the
     * internal representation.
     */
    private static class SaveData implements Serializable {
        private static final long serialVersionUID = 1L;
        private final byte[] stateBytes;
        private final byte[] checksum;

        SaveData(byte[] stateBytes, byte[] checksum) {
            this.stateBytes = stateBytes;
            this.checksum = checksum;
        }
    }
}
