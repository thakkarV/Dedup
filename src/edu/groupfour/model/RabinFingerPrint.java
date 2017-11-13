package edu.groupfour.model;

import sun.plugin2.jvm.CircularByteBuffer;

import java.util.ArrayList;

public class RabinFingerPrint implements FingerPrint {
    private CircularByteBuffer window;
    private String polynomial;
    private int modPrime;
    private int base;
    private int windowSize; // how large the hash window is in terms of bytes
    private int threshold; // how many LSB's need to be zoro to be considered a boundry


    public RabinFingerPrint(String file) {
        // default parameters of Rsync
        this.windowSize = 48;
        this.threshold = 13;
        this.window = new CircularByteBuffer(this.windowSize);
        // do more stuff here
    }

    public RabinFingerPrint(String file, int windowSize, int threshold) {
        this.windowSize = windowSize;
        this.threshold = threshold;
        this.window = new CircularByteBuffer(this.windowSize);
        // do more stuff here
    }

    public String getFingerPrint(String file) {

    }

    // returns the array list of indexes of where the chunks should begin
    public ArrayList<Long> getChunkBoundries(String file) {

    }
}
