package edu.groupfour.model;

import java.io.Serializable;

class FileChunk implements Serializable{
    private byte [] payload;
    private int refCounter;

    FileChunk(String payload) {
        this.payload = payload.getBytes();
        this.refCounter = 1;
    }

    FileChunk(byte [] payload) {
        this.payload = payload;
        this.refCounter = 1;
    }

    public void addReference() {
        this.refCounter++;
    }

    public void deleteReference() {
        this.refCounter--;
    }

    public boolean isDeletable() {
        return this.refCounter == 0;
    }

    public byte [] getPayload() {
        return this.payload;
    }
}
