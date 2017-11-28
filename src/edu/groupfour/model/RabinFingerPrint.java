package edu.groupfour.model;

import sun.plugin2.jvm.CircularByteBuffer;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.StandardOpenOption;

import java.util.ArrayList;

public class RabinFingerPrint implements FingerPrint {
    //private String polynomial;
    private int mod = 101;
    private int prime = 256;
    private int windowSize = 8; // how large the hash window is in terms of bytes
    byte[] window;
    private int threshold = 8; // how many LSB's need to be zero to be considered a boundary

    public RabinFingerPrint(String file) {
        // default parameters of Rsync
        window = new byte[windowSize];
        // do more stuff here
    }

    public RabinFingerPrint(String file, int windowSize, int threshold) {
        this.windowSize = windowSize;
        this.threshold = threshold;
        window = new byte[windowSize];
        // do more stuff here
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
    public ArrayList<Long> getChunkBoundaries(String infile) throws IOException{

        //open file
        FileInputStream readMe = new FileInputStream(infile);

        long currentIndex = 0; //current index in file
        byte inByte; //current input byte
        long rollingHash = 0 ; // current fingerprint
        ArrayList<Long> indexlist = new ArrayList<Long>(); //list to store the chunk boundaries

        int windowIndex = 0; //where are we in the window

        inByte = (byte) readMe.read();
        //initloop

        System.out.print("Initial rollingHashes: ");
        for (int l = 0; l < windowSize; l++){
            window[l] = inByte;
            rollingHash = (rollingHash * prime + inByte) % mod;
            inByte = (byte) readMe.read();
            currentIndex++;

            System.out.print(rollingHash + " ");
        }
        System.out.println();
        inByte = (byte) readMe.read();

        long p_n = 1;

        for (int l = 0; l<windowSize; l++){
            p_n = (p_n*prime)%mod;
        }

        while (inByte != -1){
            // rabin krap
            rollingHash = (rollingHash*prime + inByte - (window[windowIndex]*p_n)%mod)%mod;
            // update buffer
            window[windowIndex] = inByte;
            windowIndex = (windowIndex + 1) % windowSize;

            //System.out.println(rollingHash);
            //if chunk boundary, update
            if(rollingHash == 896){
                indexlist.add(currentIndex);
                //System.out.println("FLOOOOSH");
                //flush le buffer
                windowIndex = 0;
                rollingHash = 0;
                for (int l = 0; l < windowSize; l++){
                    window[l] = inByte;
                    rollingHash = (rollingHash*prime + inByte)%mod;
                    inByte = (byte) readMe.read();
                    //System.out.println(rollingHash);
                    currentIndex++;
                }
                currentIndex--;
            }

            inByte = (byte) readMe.read();
            currentIndex++;
        }
        return indexlist;
    }
}
