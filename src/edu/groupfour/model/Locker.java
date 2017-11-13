package edu.groupfour.model;

import com.sun.javaws.exceptions.InvalidArgumentException;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Locker implements Serializable {

    private String path; //path to locker
    private ArrayList<LockerFile> files; //paths of the files that are to be added to the locker
    private HashMap<FileChunkHash, FileChunk> chunkMap;
    private RabinFingerPrint rabin;

    /**
     * Loads or creates a new locker at the specified path.
     * @param path - path to where the locker is located or to be created
     * @param isNewLocker - true if creating a new locker instead of loading it
     * @throws IOException - if any of the files/folders cannot either be loaded or created
     */
    public Locker(String path, boolean isNewLocker) throws IOException {
        this.path = path;
        this.chunkMap = new HashMap<>();
        this.files = new ArrayList<>();
        // rabin will be loaded by the load function since it is locker dependent
        if(!isNewLocker) {
            try {
                this.load();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            // default chunk size is 4 kibibytes
            this.init(path, 4096);
        }
    }

    /**
     * Loads or creates a new locker at the specified path. If creating, sets the
     * rabin finger print's parameters such that chunk sizes approximates equal to the input size.
     * @param path - path to where the locker is located or to be created
     * @param isNewLocker - true if creating a new locker instead of loading it
     * @param chunkSize - average approximate size of the file chunks in bytes
     * @throws IOException - if any of the files/folders cannot either be loaded or created
     */
    public Locker(String path, boolean isNewLocker, int chunkSize) throws IOException {
        this.path = path;
        this.chunkMap = new HashMap<>();
        this.files = new ArrayList<>();
        // rabin will be loaded by the load function since it is locker dependent
        if(!isNewLocker) {
            try {
                this.load();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            this.init(path, chunkSize);
        }
    }


    /**
     * Creates a new locker at the path specified by path.
     * First creates the .locker and .files folders.
     * Then initializes the fingerprinter and saves its parameters to the .locker folder.
     * @param path - path to where the locker folder will be created
     */
    public void init(String path, int chunkSize) {
        // TODO : folder name of the locker could be made changeable for the user to customize
        Path lockerParentPath = Paths.get(path, "Locker");
        if (Files.exists(lockerParentPath, LinkOption.NOFOLLOW_LINKS)) {
            System.out.println("Folder " + "Locker" + " already exists at specified path.");
            System.exit(1);
        }

        try {
            Files.createDirectory(lockerParentPath);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Holds the serialized Locker Object's chunkMap and metadata about the fingerprinter
        Path lockerlockerPath = Paths.get(lockerParentPath.toString(), ".locker");
        try {
            Files.createDirectory(lockerlockerPath);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // holds the serialized LockerFile objects from Locker's ArrayList of LockerFile
        Path lockerFilePath = Paths.get(lockerParentPath.toString(), ".files");
        try {
            Files.createDirectory(lockerFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        this.rabin = new RabinFingerPrint(chunkSize);

        try {
            this.save();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Adds a single file to the locker.
     * @param filePath - Path to the file to be added to the locker.
     * @throws IOException - if the file to be added cannot be read from disk.
     */
    public void addFile(String filePath) throws IOException {

    }

    /**
     * Adds an entire directory's content to the locker.
     * @param dirPath - Path to the directory whose contents are being added to the locker.
     * @param recursiveAdd - If true, adds all subdirectories recursively to the locker.
     * @throws IOException - if files to be added cannot be read from disk.
     */
    public void addDir(String dirPath, boolean recursiveAdd) throws IOException {

    }


    /**
     * Reconstructs a file or directory that was added to the locker, and writes to the desired location.
     * @param localFilePath - Local path of the file or directory to be reconstructed, starting at the Locker's root.
     * @param targetPath - Path to which the reconstructed file or folder is to be written.
     * @throws IOException - if the reconstruction cannot be written to disk.
     * @throws InvalidArgumentException - if the input file does not exist in the locker.
     */
    public void retrieve(String localFilePath, String targetPath) throws IOException, InvalidArgumentException {

    }


    /**
     * Implements the serialization to save the locker object to disk.
     * @throws IOException - if the object cannot be saved.
     */
    public void save() throws IOException {

    }

    /**
     * Implements the de-serialization to load the locker object from disk.
     * @throws IOException - if the object cannot be loaded
     */
    private void load() throws IOException {

    }
}
