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
    private ArrayList<LockerFile> dirFiles;
    private String dirName;
    private HashMap<String, FileChunk> chunkMap; // maps the hashes of chunks to the chunks of the files
    private int chunkSize;

    // these are initialized every time the locker starts a-new
    transient private String path; // path to locker
    transient private boolean isMutated; // true if the chunk map was changed since loading the last state
    transient private ReentrantReadWriteLock mapLock;
    transient private ReentrantReadWriteLock dirFileListLock;


    /**
     * Loads or creates a new locker at the specified path.
     * @param path - path to where the locker is located or to be created
     */
    public Locker(String path) {
        this.path = path;
        this.files = new ArrayList<>();
        this.dirFiles = null;
        this.chunkMap = new HashMap<>();
        this.mapLock = new ReentrantReadWriteLock();
		this.dirFileListLock = new ReentrantReadWriteLock();
        this.load();
        this.isMutated = false;
    }


    /**
     * Loads or creates a new locker at the specified path. If creating, sets the
     * rabin finger print's parameters such that chunk sizes approximates equal to the input size.
     * @param path - path to where the locker is located or to be created
     * @param chunkSize - average approximate size of the file chunks in bytes
     */
    public Locker(String path, String lockerName, int chunkSize) {
        if (lockerName == null)
            lockerName = "Locker";

        this.path = Paths.get(path, lockerName).toString();
        this.files = null;
        this.dirFiles = null;
        this.chunkMap = new HashMap<>();
		this.mapLock = new ReentrantReadWriteLock();
        this.dirFileListLock = new ReentrantReadWriteLock();

        // rabin will be loaded by the load function since it is locker dependent
        this.isMutated = true;
        this.chunkSize = chunkSize;
        this.initialize(this.path);
        this.save();
    }


    /**
     * Creates a new locker at the path specified by path.
     * First creates the .locker and .files folders.
     * Then initializes the finger printer and saves its chunkSize to the .locker folder.
     * @param path - path to where the locker folder will be created
     */
    private void initialize(String path) {
        // create root directory of the locker
        try {
            File lockerRootDir = new File(path);
            if (lockerRootDir.exists()) {
                System.err.println("The provided directory name for the locker already exists. Exiting.");
                System.exit(1);
            } else {
                if (!lockerRootDir.mkdir()) {
                    System.err.println("Could not create new directory at provided path. Exiting.");
//                    System.exit(1);
                }
            }
        } catch (SecurityException e) {
            System.err.println("Did not have write permission to provided path. Exiting.");
            System.exit(1);
        }
        
        // Holds the serialized Locker Object's chunkMap and metadata about the chunkSize
        Path lockerlockerPath = Paths.get(path, ".locker");
        try {
            File lockerlockerDir = new File(lockerlockerPath.toString());
            if (!lockerlockerDir.mkdir()) {
                System.err.println("Could not create new directory at provided path. Exiting.");
//                    System.exit(1);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // holds the serialized LockerFile objects from Locker's ArrayList of LockerFiles
        Path lockerFilePath = Paths.get(path, ".files");
        try {
            File lockerFileDir = new File(lockerFilePath.toString());
            if (!lockerFileDir.mkdir()) {
                System.err.println("Could not create new directory at provided path. Exiting.");
//                    System.exit(1);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    /**
     * Adds a single file to the locker.
     * @param filePath - Path to the file to be added to the locker.
     */
    public void addFile(String filePath, boolean isDir) {
        File originFile = new File(filePath);

        // check if a file by this name is already in the locker
        if (new File(Paths.get(this.path, ".files", originFile.getName()).toString() + ".ser").exists()) {
            System.err.println("File of this name already exists in the locker. Please rename it and try again.");
            System.exit(1);
        }

        // first read the file as a large string
        byte [] fileBytes;
        try {
            fileBytes = new Scanner(originFile).useDelimiter("\\Z").next().getBytes();
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
        long previous = -1L; // end of the previous virtual chunk
        for (Long boundry : boundries) {
            // get file substring (chunk) and its MD5 hash
            long currentBound = boundry;
            int startIndex = (int) (previous + 1);
            int chunkSize = (int) (currentBound - previous);
            byte[] chunk = new byte[chunkSize];

            System.arraycopy(fileBytes, startIndex, chunk, 0, chunkSize);

            byte[] hash = md.digest(chunk);
            String chunkHash = Base64.getEncoder().encodeToString(hash);

            // now check if this hash exists in the chunk map
            this.mapLock.readLock().lock();
            if (this.chunkMap.containsKey(chunkHash)) {
                this.mapLock.readLock().unlock();
                // if exists, increase reference to the chunk
                this.mapLock.writeLock().lock();
                chunkMap.get(chunkHash).addReference();
                this.mapLock.writeLock().unlock();
            } else {
                this.mapLock.readLock().unlock();
                // else insert a new chunk to the map
                FileChunk fileChunk = new FileChunk(chunk);
                this.mapLock.writeLock().lock();
                this.chunkMap.put(chunkHash, fileChunk);
                this.mapLock.writeLock().unlock();
            }

            lockerFileHashes.add(chunkHash);
            previous = currentBound;
        }

        // now insert this new locker file into the array list of locker files
        LockerFile lfile = new LockerFile(originFile.getName(), lockerFileHashes);

        if (isDir) {
            this.dirFileListLock.writeLock().lock();
                this.dirFiles.add(lfile);
            this.dirFileListLock.writeLock().unlock();
        } else {
            this.files.add(lfile);
        }

        this.isMutated = true;
    }

    
    /**
     * Adds an entire directory's content to the locker.
     * @param dirPath - Path to the directory whose contents are being added to the locker.
     * @param recursiveAdd - If true, adds all subdirectories recursively to the locker.
     */
    public void addDir(String dirPath, boolean recursiveAdd) {
        this.dirFiles = new ArrayList<>();
        this.isMutated = true;
        File dir  = new File(dirPath);
        this.dirName = dir.getName();
        if (!dir.isDirectory()) {
            System.err.println("Input path does not point to a directory.");
            System.exit(1);
        }

        // now check if it is already in the locker
        Path checkPath = Paths.get(this.path, ".files", dir.getName() + ".ser");
        File checkFile = new File(checkPath.toString());
        if (checkFile.exists()) {
            System.err.println("A directory by the name " + dir.getName() + " already exists in locker. Exiting.");
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
                ArrayList<Thread> threads = new ArrayList<>();
                if (f.isFile()) {
                    Thread T = new Thread(() -> addFile(f.toString(), true));
                    T.start();
                    threads.add(T);
                }

                for (Thread T : threads) {
                    try {
                        T.join();
                    } catch (InterruptedException e) {
                        System.err.println("Thread interrupted.");
                    }
                }
            }
        } else {
            // recursively add all the contents of dir and all its child dirs
            if (dirListing == null) {
                System.err.println("Directory " + dirPath + " is empty. Nothing added to locker.");
                return;
            }
            
            for (File f : dirListing) {
                ArrayList<Thread> threads = new ArrayList<>();
				if (f.isFile()) {
                    Thread T = new Thread(() -> addFile(f.toString(), true));
                    T.start();
                    threads.add(T);
                }

				else if (f.isDirectory())
					this.addDir(f.getPath(), true);

				for (Thread T : threads) {
				    try {
                        T.join();
                    } catch (InterruptedException e) {
				        System.err.println("Thread interrupted");
                    }
                }
			}
        }
    }


    /**
     * Reconstructs a file or directory that was added to the locker, and writes to the desired location.
     * @param entryName - Local path of the file or directory to be reconstructed, starting at the Locker's root.
     * @param targetPathStr - Path to which the reconstructed file or folder is to be written.
     */
    public void retrieve(String entryName, String targetPathStr) {
        // check if input is valid
        try {
            this.deferredFileLoader(entryName);
        } catch (NoSuchFileException e) {
            System.err.println("Input file name does not exist in the locker.");
            return;
        }

        if (this.dirName != null) {
            Path outDirPath = Paths.get(targetPathStr);
            // create the dir first before writing files
            File dir = new File(outDirPath.toString());
            System.out.println(dir.toString());

            // check for existence
            if (dir.exists()) {
                System.err.println("A directory of name " + dir.getName() + " already exists at input path. Exiting.");
                System.exit(1);
            }

            // create parent dir
            try {
                if (!dir.mkdir()) {
                    System.err.println("Could not create new directory to output path.");
                    System.exit(1);
                }
            } catch (SecurityException e) {
                System.err.println("Did not have write permission to locker. Exiting");
                System.exit(1);
            }

            // write all files
            ArrayList<Thread> threads = new ArrayList<>();
            for (LockerFile lfile : this.dirFiles) {
                Thread T = new Thread (() -> this.retrieveFileWrite(outDirPath.toString(), lfile));
                T.start();
                threads.add(T);
            }

            for(Thread T : threads) {
                try {
                    T.join();
                } catch (InterruptedException e) {
                    System.err.println("Thread interrupted.");
                }
            }

            this.dirName = null;
            this.dirFiles = null;
        }
        else {
            // write single file
            LockerFile lfile = null;
            for (LockerFile f : this.files) {
                if (f.entryName.equals(entryName))
                    lfile = f;
            }

            if (lfile == null) {
                System.err.println("Could not find file to be deleted in the locker.");
                return;
            }

            this.retrieveFileWrite(targetPathStr, lfile);
        }
    }

    private void retrieveFileWrite(String targetPathStr, LockerFile lfile) {
        // read in the chunks and concatenate
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileChunk chunk;

        // now deserialize it
        for (String hash : lfile.chunkHashes) {
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
            Path outputPath = Paths.get(targetPathStr, lfile.entryName);
            File f = new File(outputPath.toString());
            FileOutputStream fos = new FileOutputStream(f);
            baos.writeTo(fos);
            baos.close();
            fos.close();
        } catch (SecurityException e) {
            System.err.println("Did not have permission to write to provided output path. Exiting.");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (InvalidPathException e) {
            System.err.println("Provided output path is invalid.");
            System.exit(1);
        }
    }


    /**
     * Deletes a given files from the locker. Here the input path is the path of the
     * file that was provided when the file was first inserted into the locker.
     * @param entryName - The path of the file to be deleted from when it was first inserted into the locker.
     */
    public void delete(String entryName) {
        this.isMutated = true;
        try {
            this.deferredFileLoader(entryName);
        } catch (NoSuchFileException e) {
            System.err.println("Input file does not exist in the locker.");
            return;
        }

        if (this.dirName != null) {
            Path dirPath = Paths.get(this.path, ".files", this.dirName + ".ser");
            File dir = dirPath.toFile();

            // permission check
            if (!dir.canWrite()) {
                System.err.println("You do not have write permission to locker. Exiting.");
                System.exit(1);
            }

            // gather entries and delete individually in separate threads
            File [] dirList = dir.listFiles();
            if (dirList == null) {
                System.err.println("Error deleting directory from locker. Exiting.");
                System.exit(1);
            }

            ArrayList<Thread> threads = new ArrayList<>();
            for (File f : dirList) {
                Thread T = new Thread (() -> this.deleteFileInDir(f.toString()));
                T.start();
                threads.add(T);
            }

            for(Thread T : threads) {
                try {
                    T.join();
                } catch (InterruptedException e) {
                    System.err.println("Thread interrupted.");
                }
            }

            // now delete the dir itself
            try {
                if (!dir.delete()) {
                    System.err.println("All files deleted from locker, but could not delete .files dir.");
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }

            this.dirName = null;
            this.dirFiles = null;
        } else {
            File target = new File(Paths.get(this.path, ".files", entryName + ".ser").toString());

            // first load in the file into the LockerFile array
            LockerFile targetFile = null;
            for (LockerFile f : this.files) {
                if (f.entryName.equals(entryName))
                    targetFile = f;
            }

            if (targetFile == null) {
                System.err.println("Could not find file to be deleted in the locker.");
                return;
            }

            // now first delete the serialized LockerFile from disk
            // if we cannot do this, then we cannot remove the references to these chunks in the main chunkMap
            try {
                if (!target.delete()) {
                    System.err.println("Could not delete file on disk.");
                    System.exit(1);
                }
            } catch (SecurityException e) {
                System.err.println("Did not have permission to delete file on disk. Exiting.");
                System.exit(1);
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
        }
    }

    private void deleteFileInDir(String filePath) {
        File targetOnDisk = new File(filePath);

        // first load in the file into the LockerFile array
        LockerFile targetInLocker = null;
        this.dirFileListLock.readLock().lock();
        for (LockerFile f : this.dirFiles) {
            String fileName = f.entryName + ".ser";
            if (fileName.equals(targetOnDisk.getName())) {
                targetInLocker = f;
                break;
            }
        }
        this.dirFileListLock.readLock().unlock();

        if (targetInLocker == null) {
            System.err.println("Could not find file to be deleted in the locker.");
            return;
        }

        // now first delete the serialized LockerFile from disk
        // if we cannot do this, then we cannot remove the references to these chunks in the main chunkMap
        try {
            if (!targetOnDisk.delete()) {
                System.err.println("Could not delete file on disk.");
                System.exit(1);
            }
        } catch (SecurityException e) {
            System.err.println("Did not have permission to delete file on disk. Exiting.");
            System.exit(1);
        }

        // decrement chunk references from the chunk map. Delete if references are zero.
        for (String hash : targetInLocker.chunkHashes) {
            this.mapLock.readLock().lock();
                FileChunk chunk = this.chunkMap.get(hash);
            this.mapLock.readLock().unlock();

            this.mapLock.writeLock().lock();
            chunk.deleteReference();
            if (chunk.isDeletable()) {
                this.chunkMap.remove(hash);
            }
            this.mapLock.writeLock().unlock();
        }

        // remove the file entry in the dirFiles array list
        this.dirFileListLock.writeLock().lock();
            this.dirFiles.remove(targetInLocker);
        this.dirFileListLock.writeLock().unlock();
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
                fos = new FileOutputStream(mapPath.toFile(), false);
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.chunkMap);
                oos.writeInt(this.chunkSize);
                oos.close();
                fos.close();
            } catch (FileNotFoundException e) {
                System.err.println("Error while saving the locker. Could not find specified path.");
                e.printStackTrace();
                System.exit(1);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        // save all file changed since the last save
        if (this.files != null && !this.files.isEmpty()) {
            for (LockerFile f : this.files) {
                if (f.isMutated) {
                    try {
                        Path filePath = Paths.get(this.path, ".files", f.entryName + ".ser");
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
            this.files.clear();
        }

        // save dir as a single entity if that's what was added to the locker
        if (this.dirFiles != null && !this.dirFiles.isEmpty()) {
            Path dirPath = Paths.get(this.path, ".files", this.dirName + ".ser");
            File dir = new  File(dirPath.toString());

            // first create dir in .files dir of locker
            try {
                if (!dir.mkdir()) {
                    System.err.println("Could not create new directory in locker.");
                    System.exit(1);
                }
            } catch (SecurityException e) {
                System.err.println("Did not have write permission to locker. Exiting");
                System.exit(1);
            }

            // now write all the files into it
            for (LockerFile f : this.dirFiles) {
                if (f.isMutated) {
                    try {
                        Path filePath = Paths.get(dir.getPath(), f.entryName + ".ser");
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
            this.dirFiles.clear();
        }

        this.isMutated = false;
    }

    
    /**
     * Implements the de-serialization to load the locker object from disk.
     */
    @SuppressWarnings("unchecked")
    private void load() {
        // set everything's isMutated to false when loading
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
     * @param entryName - path to the file to be loaded
     * @throws NoSuchFileException - if the serialized object cannot be read.
     */
    private void deferredFileLoader(String entryName) throws NoSuchFileException {
        Path filePath = Paths.get(this.path, ".files", entryName + ".ser");
        File loadFile = new File(filePath.toString());

        if (!loadFile.exists())
            throw new NoSuchFileException("File " + entryName + " not found in locker.");

        // load a single file
        if (loadFile.isFile()) {
            this.files = new ArrayList<>();
            FileInputStream fis;
            ObjectInputStream ois;
            try {
                fis = new FileInputStream(filePath.toString());
                ois = new ObjectInputStream(fis);
                LockerFile file = (LockerFile) ois.readObject();
                this.files.add(file);
                ois.close();
                fis.close();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error while loading the locker file.");
                e.printStackTrace();
                System.exit(1);
            }
        }

        // load a directory as a single entity
        else if (loadFile.isDirectory()) {
            // init stuff needed to load a dir
            this.dirName = entryName;
            this.dirFiles = new ArrayList<>();

            File [] dirList = loadFile.listFiles();
            if (dirList == null || dirList.length == 0) {
                System.err.println("Directory to be loaded was empty. Exiting.");
                System.exit(1);
            }

            ArrayList<Thread> threads = new ArrayList<>();
            for (File f : dirList) {
                // spawn up a new thread for each file to be loaded
                Thread T = new Thread(()-> {
                    FileInputStream fis;
                    ObjectInputStream ois;
                    try {
                        fis = new FileInputStream(f);
                        ois = new ObjectInputStream(fis);
                        LockerFile file = (LockerFile) ois.readObject();
                        this.dirFileListLock.writeLock().lock();
                            this.dirFiles.add(file);
                        this.dirFileListLock.writeLock().unlock();
                        ois.close();
                        fis.close();
                    } catch (IOException | ClassNotFoundException e) {
                        System.err.println("Error while loading the locker file.");
                        e.printStackTrace();
                        System.exit(1);
                    }
                });
                T.start();
                threads.add(T);
            }

            for (Thread T : threads) {
                try {
                    T.join();
                } catch (InterruptedException e) {
                    System.err.println("Thread interrupted.");
                }
            }
        }
    }

    /**
     * Helper for the GUI which returns all the files in the Locker right now
     * without actually deserialzing and loading them into the Locker class.
     * @return ArrayList of Files that are in the locker.
     */
    public ArrayList<File> getLockerContents() {
        Path lockerFilePath = Paths.get(this.path, ".files");
        File lockerFileRoot = new File(lockerFilePath.toString());
        File [] files = lockerFileRoot.listFiles();
        ArrayList<File> retList = new ArrayList<>();
        if (files != null) {
            Collections.addAll(retList, files);
            return retList;
        } else {
            return null;
        }
    }
}
