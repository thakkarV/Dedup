package edu.groupfour.model;

import java.io.Serializable;
import java.util.ArrayList;

class LockerFile implements Serializable {
    String localFilePath;
    ArrayList<String> chunkHashes;
    boolean isMutated;

    LockerFile() {
        this.chunkHashes = new ArrayList<>();
    }

    LockerFile(String localFilePath) {
        this.localFilePath = localFilePath;
        this.chunkHashes = new ArrayList<>();
        this.isMutated = true;
    }

    LockerFile(String localFilePath, ArrayList<String> hashes) {
        this.localFilePath = localFilePath;
        this.chunkHashes = hashes;
        this.isMutated = true;
    }

    public void addChunk(String hash) {
        this.chunkHashes.add(hash);
        this.isMutated = true;
    }
}