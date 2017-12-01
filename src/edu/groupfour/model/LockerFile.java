package edu.groupfour.model;

import java.io.Serializable;
import java.util.ArrayList;

class LockerFile implements Serializable {
    String entryName;
    ArrayList<String> chunkHashes;
    boolean isMutated;

    LockerFile() {
        this.chunkHashes = new ArrayList<>();
    }

    LockerFile(String entryName) {
        this.entryName = entryName;
        this.chunkHashes = new ArrayList<>();
        this.isMutated = true;
    }

    LockerFile(String entryName, ArrayList<String> hashes) {
        this.entryName = entryName;
        this.chunkHashes = hashes;
        this.isMutated = true;
    }

    public void addChunk(String hash) {
        this.chunkHashes.add(hash);
        this.isMutated = true;
    }
}