package edu.groupfour.model;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.HashMap;

public class Locker implements Serializable {

    private String path; //path to locker
    private ArrayList<LockerFile> files; //paths of the files that are to be added to the locker
    private HashMap<FileChunkHash, FileChunk> chunkMap;

    // loads in a locker specified by the path
    public Locker(String path) throws NoSuchFileException {

    }

    // create a new locker
    public void init(String path) {

    }

    // adds a file to the locker
    public void addFile(String filePath) throws IOException {

    }

    // reconstructs and returns a file form the locker
    public void retrieveFile(String localFilePath, String targetPath) throws IOException {

    }

    // adds an entire directory recursively to the locker
    public void addDir(String path) throws IOException {

    }

    // serialize to disk and save
    public void save() throws IOException {

    }

    // deserialize from disk and load in
    public void load() throws IOException{

    }
}
