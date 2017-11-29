package edu.groupfour.model;

import sun.plugin2.jvm.CircularByteBuffer;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.StandardOpenOption;

import java.util.ArrayList;

public class RabinFingerPrint implements FingerPrint {
    //private String polynomial;
    private int mod = 1000000;
    private int chunkingCoeff = 1000;
    private int prime = 69691;
    private int windowSize = 48; // how large the hash window is in terms of bytes
    byte[] window;
    private int threshold = 8; // how many LSB's need to be zero to be considered a boundary

    public RabinFingerPrint() {
        // default parameters of Rsync
        window = new byte[windowSize];
    }

    public RabinFingerPrint(int chunkSize) {
        // TODO: set the windowsize and threshold so that average chunk size is the input chunk size
        this.windowSize = windowSize;
        this.threshold = threshold;
        window = new byte[windowSize];
        // do more stuff here
        this.chunkingCoeff = mod / chunkSize;
    }

    //Produce fingerprint after cutting a boundary, skips a bunch of unnecessary processing also good to initialize.
/*    private int InitFingerprint(String input){
        int outprint = 0;
        for (int i = 0; i < input.length(); i++) {
            outprint = (outprint*mod + input.charAt(i))%prime;
        }
        return outprint;
    }
*/
/*
    public String getFingerPrint(String file) {

    }
*/

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

        inByte = byteFile[byteIndex++];
        for (int l = 0; l < windowSize; l++){
            p_n = (p_n * prime) % mod;
        }

        while (inByte != -1) {
            // Rabin Karp - Update rolling hash value for this window
            rollingHash = (rollingHash * prime + inByte - (window[windowIndex] * p_n) % mod) % mod;
            // update buffer
            window[windowIndex] = inByte;
            windowIndex = (windowIndex + 1) % windowSize;

            // if chunk boundary, update
            if(rollingHash % chunkingCoeff == 0){
                // if lowest 3 digits 0, we boundary. WE CAN CHANGE AVERAGE CHUNK SIZE BY CHANGING MOD VALUE
                indexlist.add(currentIndex);
                // flush le buffer
                windowIndex = 0;
                rollingHash = 0;
                for (int l = 0; l < windowSize; l++){
                    window[l] = inByte;
                    rollingHash = (rollingHash * prime + inByte) % mod;
                    inByte = byteFile[byteIndex++];
                    currentIndex++;
                }
                currentIndex--;
            }

            inByte = byteFile[byteIndex++];
            currentIndex++;

            indexlist.add((long)byteFile.length);
        }
        return indexlist;
    }
}
