package edu.groupfour.model;

import java.io.*;
import java.nio.file.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Locker {
    // haa! PUNS!
    transient private final String chunkMapSerName = "ChunkMap.ser";

    private ArrayList<LockerFile> files; // paths of the files that are to be added to the locker
    private HashMap<String, FileChunk> chunkMap; // maps the hashes of chunks to the chunks of the files
    private int chunkSize;

    // these are initialized every time the locker starts a-new
    transient private String path; // path to locker
    transient private boolean isMutated; // true if the chunk map was changed since loading the last state
    transient private ReentrantReadWriteLock mapLock;
    transient private ReentrantReadWriteLock fileListLock;

    /**
     * Loads or creates a new locker at the specified path.
     * @param path - path to where the locker is located or to be created
     */
    public Locker(String path) {
        this.path = path;
        this.chunkMap = new HashMap<>();
        this.files = new ArrayList<>();
		this.mapLock = new ReentrantReadWriteLock();
		this.fileListLock = new ReentrantReadWriteLock();
		// rabin will be loaded by the load function since it is locker dependent
        try {
            this.load();
            this.isMutated = false;
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Loads or creates a new locker at the specified path. If creating, sets the
     * rabin finger print's parameters such that chunk sizes approximates equal to the input size.
     * @param path - path to where the locker is located or to be created
     * @param chunkSize - average approximate size of the file chunks in bytes
     */
    public Locker(String path, String lockerName, int chunkSize) {
        this.path = path;
        if (lockerName == null)
            lockerName = "Locker";

        Path lockerRootPath = Paths.get(this.path, lockerName);

        try {
            File lockerRootDir = new File(lockerRootPath.toString());
            if (!lockerRootDir.exists()) {
                System.err.println("The provided directory name for the locker already exists. Exiting.");
                System.exit(1);
            } else {
                lockerRootDir.mkdir();
            }
        } catch (SecurityException e) {
            System.err.println("Did not have write permission to provided path. Exiting.");
            System.exit(1);
        }

		this.mapLock = new ReentrantReadWriteLock();
        this.fileListLock = new ReentrantReadWriteLock();

        // rabin will be loaded by the load function since it is locker dependent
        this.isMutated = true;
        this.chunkSize = chunkSize;
        this.initialize(path, lockerName);
        this.save();
    }


    /**
     * Creates a new locker at the path specified by path.
     * First creates the .locker and .files folders.
     * Then initializes the fingerprinter and saves its parameters to the .locker folder.
     * @param path - path to where the locker folder will be created
     */
    public void initialize(String path, String lockerName) {
        // TODO : folder name of the locker could be made changeable for the user to customize
        Path lockerRootPath = Paths.get(path, lockerName);
        if (Files.exists(lockerRootPath, LinkOption.NOFOLLOW_LINKS)) {
            System.err.println("Folder " + lockerName + " already exists at specified path.");
            System.exit(1);
        }

        try {
            Files.createDirectory(lockerRootPath);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Holds the serialized Locker Object's chunkMap and metadata about the fingerprinter
        Path lockerlockerPath = Paths.get(lockerRootPath.toString(), ".locker");
        try {
            Files.createDirectory(lockerlockerPath);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // holds the serialized LockerFile objects from Locker's ArrayList of LockerFile
        Path lockerFilePath = Paths.get(lockerRootPath.toString(), ".files");
        try {
            Files.createDirectory(lockerFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    /**
     * Adds a single file to the locker.
     * @param filePath - Path to the file to be added to the locker.
     */
    public void addFile(String filePath) {
        this.isMutated = true;

        // first read the file as a large string
        byte [] fileBytes;
        try {
            fileBytes = new Scanner(new File(filePath)).useDelimiter("\\Z").next().getBytes();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        RabinFingerPrint fingerPrint = new RabinFingerPrint(this.chunkSize);
        ArrayList<Long> boundries = fingerPrint.getChunkBoundaries(fileBytes);

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return;
        }

        ArrayList<String> lockerFileHashes = new ArrayList<>();
        Long previous = 0L;
        for (int i = 0; i < boundries.size() - 1; i++) {
            // get file substring (chunk) and its MD5 hash
            int chunkSize = (int)(boundries.get(i) - previous);
            byte [] chunk = new byte [chunkSize];
            System.arraycopy(fileBytes, previous.intValue(), chunk, 0, chunkSize);

            byte [] hash = md.digest(chunk);
            String chunkHash = Base64.getEncoder().encodeToString(hash);

            // now check if this hash exists in the chunk map
            this.mapLock.readLock().lock();
            if (this.chunkMap.containsKey(chunkHash)) {
                // if exists, increase reference to the chunk
                this.mapLock.writeLock().lock();
                    chunkMap.get(chunkHash).addReference();
                this.mapLock.writeLock().unlock();
            } else {
                // else insert a new chunk to the map
                FileChunk fileChunk = new FileChunk(chunk);
                this.mapLock.writeLock().lock();
                    this.chunkMap.put(chunkHash, fileChunk);
                this.mapLock.writeLock().unlock();
            }
            this.mapLock.readLock().unlock();

            lockerFileHashes.add(chunkHash);
            previous = boundries.get(i);
        }

        // now insert this new locker file into the array list of locker files
        LockerFile lfile = new LockerFile(filePath, lockerFileHashes);
        this.fileListLock.writeLock().lock();
            this.files.add(lfile);
        this.fileListLock.writeLock().unlock();
    }

    /**
     * Adds an entire directory's content to the locker.
     * @param dirPath - Path to the directory whose contents are being added to the locker.
     * @param recursiveAdd - If true, adds all subdirectories recursively to the locker.
     */
    public void addDir(String dirPath, boolean recursiveAdd) {
        this.isMutated = true;
        File dir  = new File(dirPath);
        if (!dir.isDirectory()) {
            System.err.println("Input path does not point to a directory.");
            System.exit(1);
        }

        File [] dirListing = dir.listFiles();

        if (!recursiveAdd) {
            // only add the contents of this dir, do not recurse into child dirs
            if (dirListing == null) {
                System.err.println("Directory to be added is empty. Exiting.");
                System.exit(1);
            }

            for (File f : dirListing) {
                if (f.isFile()) {
                    new Thread(() -> addFile(f.toString())).start();
                }
            }
        } else {
            // recursively add all the contents of dir and all its child dirs
            if (dirListing == null) {
                System.err.println("Directory " + dirPath + " is empty. Nothing added to locker.");
                return;
            }
            
            for (File f : dirListing) {
				if (f.isFile())
					new Thread(() -> addFile(f.toString())).start();
				else if (f.isDirectory())
					this.addDir(f.getPath(), true);
			}
        }
    }


    /**
     * Reconstructs a file or directory that was added to the locker, and writes to the desired location.
     * @param localFilePath - Local path of the file or directory to be reconstructed, starting at the Locker's root.
     * @param targetPathStr - Path to which the reconstructed file or folder is to be written.
     */
    public void retrieve(String localFilePath, String targetPathStr) {
        // check if input is valid
        try {
            this.deferredFileLoader(localFilePath);
        } catch (NoSuchFileException e) {
            System.err.println("Input file name does not exist in the locker. Please try again.");
            return;
        }

        // read in the chunks and concatenate
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileChunk chunk;
        for (String hash : this.files.get(0).chunkHashes) {
            chunk = this.chunkMap.get(hash);
            try {
                baos.write(chunk.getPayload());
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        // try writing to output path
        try {
            File f = new File(targetPathStr);
            if (f.canWrite()) {
                FileOutputStream fos = new FileOutputStream(f);
                baos.writeTo(fos);
                fos.close();
            } else {
                System.err.println("Did not have write permission to path. Exiting.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (InvalidPathException e) {
            System.err.println("Provided path is invalid. Please try again.");
        }
    }

    public void deleteFile(String localFilePath) {
        try {
            this.deferredFileLoader(localFilePath);
        } catch (NoSuchFileException e) {
            System.err.println("No such file exists in the locker.");
            return;
        }

        LockerFile targetFile = null;
        for (LockerFile f : this.files) {
            if (f.localFilePath.equals(localFilePath))
                targetFile = f;
        }

        if (targetFile == null) {
            System.err.println("Could not find file to be deleted in the locker.");
            return;
        }

        // decrement chunk references from the chunk map. Delete if references are zero.
        for (String hash : targetFile.chunkHashes) {
            FileChunk chunk = this.chunkMap.get(hash);
            chunk.deleteReference();
            if (chunk.isDeletable()) {
                this.chunkMap.remove(hash);
            }
        }

        // remove the file entry in the files array list
        this.files.remove(targetFile);

        // finally delete the serialized LockerFile from disk
        File target = new File(Paths.get(this.path, ".files", localFilePath + ".ser").toString());
        if (!target.delete()) {
            System.err.println("Could not delete serialized file on disk.");
        }
    }

    /**
     * Implements the serialization to save the locker object to disk.
     */
    public void save() {
        FileOutputStream fos;
        ObjectOutputStream oos;

        // first save chunkMap and chunkSize
        if (this.isMutated) {
            try {
                Path mapPath = Paths.get(this.path, ".locker" , this.chunkMapSerName);
                System.out.println(mapPath);
                fos = new FileOutputStream(mapPath.toFile(), false);
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.chunkMap);
                oos.writeInt(this.chunkSize);
                oos.close();
                fos.close();
            } catch (FileNotFoundException e) {
                System.err.println("Error while saving the locker. Could not find specified path.");
                System.exit(1);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        // now all files if they were mutated
        for (LockerFile f : this.files) {
            if (f.isMutated) {
                try {
                    Path filePath = Paths.get(this.path, ".files", f.localFilePath + ".ser");
                    fos = new FileOutputStream(filePath.toString(), false);
                    oos = new ObjectOutputStream(fos);
                    oos.writeObject(f);
                    oos.close();
                    fos.close();
                } catch (IOException e) {
                    System.err.println("Error while saving the locker.");
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }
    }

    /**
     * Implements the de-serialization to load the locker object from disk.
     * @throws IOException - if the object cannot be loaded
     */
    @SuppressWarnings("unchecked")
    private void load() throws IOException {
        // set everything's isMutated to flase when loading
        // chunk map and finger printer are reloaded every time
        FileInputStream fis;
        ObjectInputStream ois;

        try {
            Path mapPath = Paths.get(this.path, ".locker" , this.chunkMapSerName);
            fis = new FileInputStream(mapPath.toString());
            ois = new ObjectInputStream(fis);
            // unchecked cast, not sure what else can be done
            this.chunkMap = (HashMap<String, FileChunk>) ois.readObject();
            this.chunkSize = ois.readInt();
            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error while loading the locker.");
            e.printStackTrace();
            System.exit(1);
        }

        // That's it, none of the LockerFiles are actually loaded here.
        // We defer the file reading from disk to actual operations on those files.
        // This is to save on disk IO bottle necks in case of large lockers.
    }


    /**
     * Intended to be called after the class is instantiated, this method only
     * loads in a single file from the disk and deserializes it, either to make modifications to the file,
     * or to overwrite it or to retrieve it. This allows us to not load all the deserialized LockerFile classes
     * every time we load in a locker that already exists. Removed disk IO as a major bottleneck.
     * @param localPath - path to the file to be loaded
     * @throws IOException - if the serialized object cannot be read.
     */
    private void deferredFileLoader(String localPath) throws NoSuchFileException{
        Path filePath = Paths.get(this.path, localPath + ".ser");
        if (!filePath.toFile().exists())
            throw new NoSuchFileException("File " + localPath + " not found in locker.");

        FileInputStream fis;
        ObjectInputStream ois;
        try {
            fis = new FileInputStream(filePath.toString());
            ois = new ObjectInputStream(fis);
            LockerFile file = (LockerFile) ois.readObject();
            this.files.add(file);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error while loading the locker file.");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
