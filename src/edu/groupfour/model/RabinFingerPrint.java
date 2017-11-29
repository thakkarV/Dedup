package edu.groupfour.model;

import sun.plugin2.jvm.CircularByteBuffer;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.StandardOpenOption;

import java.util.ArrayList;

public class RabinFingerPrint implements FingerPrint {
    //private String polynomial;
    private int mod = 1000000; // Range of the rolling hash values
    private int chunkSize;
    private int prime = 69691;
    private int windowSize = 48; // how large the hash window is in terms of bytes
    byte[] window;
    private int threshold = 8; // how many LSB's need to be zero to be considered a boundary

    public RabinFingerPrint() {
        // default parameters of Rsync
        window = new byte[windowSize];

        chunkSize = 1000;
    }

    public RabinFingerPrint(int chunkSize) {
        this.windowSize = windowSize;
        this.threshold = threshold;
        window = new byte[windowSize];
        // do more stuff here
        this.chunkSize = chunkSize;
    }

    // returns the array list of indexes of where the chunks should begin
    public ArrayList<Long> getChunkBoundaries(byte [] byteFile) {
        int byteIndex = 0;
        long currentIndex = 0; //current index in file
        byte inByte; //current input byte
        long rollingHash = 0 ; // current fingerprint
        long p_n = 1;
        ArrayList<Long> indexlist = new ArrayList<>(); //list to store the chunk boundaries

        int windowIndex = 0; // where are we in the window

        inByte = byteFile[byteIndex++];
        for (int l = 0; l < windowSize; l++){
            window[l] = inByte;
            rollingHash = (rollingHash * prime + inByte) % mod;
            inByte = byteFile[byteIndex++];
            currentIndex++;
        }

        // Generating prime ^ windowSize
        for (int l = 0; l < windowSize; l++){
            p_n = (p_n * prime) % mod;
        }

        while (byteIndex < byteFile.length) {
            // Rabin Karp - Update rolling hash value for this window
            rollingHash = (rollingHash * prime + inByte - (window[windowIndex] * p_n) % mod) % mod;
            // update buffer
            window[windowIndex] = inByte;
            windowIndex = (windowIndex + 1) % windowSize;

            // if chunk boundary, update
            if(rollingHash % chunkSize == 0){
                // if lowest 3 digits 0, we boundary. WE CAN CHANGE AVERAGE CHUNK SIZE BY CHANGING MOD VALUE
                indexlist.add(currentIndex);

                // flush le buffer
                windowIndex = 0;
                rollingHash = 0;
                for (int l = 0; l < windowSize; l++){
                    window[l] = inByte;
                    rollingHash = (rollingHash * prime + inByte) % mod;

                    if(l == 0 && byteIndex + windowSize > byteFile.length)
                        break;
                    inByte = byteFile[byteIndex++];
                    currentIndex++;
                }
                currentIndex--;
            }

            inByte = byteFile[byteIndex++];
            currentIndex++;
        }
        if(!(indexlist.get(indexlist.size() - 1) == (long)byteFile.length - 1))
            indexlist.add((long)byteFile.length - 1);

        return indexlist;
    }
}
