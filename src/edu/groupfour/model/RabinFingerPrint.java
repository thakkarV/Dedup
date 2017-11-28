package edu.groupfour.model;

import sun.plugin2.jvm.CircularByteBuffer;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.StandardOpenOption;

import java.util.ArrayList;

public class RabinFingerPrint implements FingerPrint {
    //private String polynomial;
    private int modPrime = 101;
    private int base = 256;
    private int windowSize = 8; // how large the hash window is in terms of bytes
    byte[] window;
    private int threshold = 8; // how many LSB's need to be zero to be considered a boundary

    public RabinFingerPrint(String file) {
        // default parameters of Rsync
        byte[] window = new byte[windowSize];
        // do more stuff here
    }

    public RabinFingerPrint(String file, int windowSize, int threshold) {
        this.windowSize = windowSize;
        this.threshold = threshold;
        byte[] window = new byte[windowSize];
        // do more stuff here
    }

    //Produce fingerprint after cutting a boundary, skips a bunch of unnecessary processing also good to initialize.
/*    private int InitFingerprint(String input){
        int outprint = 0;
        for (int i = 0; i < input.length(); i++) {
            outprint = (outprint*modPrime + input.charAt(i))%base;
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

        long currind = 0; //current index in file
        byte x; //current input byte
        long nowprint = 0 ; // current fingerprint
        ArrayList<Long> indexlist = new ArrayList<Long>(); //list to store the chunk boundaries

        byte[] window = new byte[windowSize]; //the window
        int whereami = 0; //where are we in the window


        x = (byte) readMe.read();
        currind++;
        //initloop
        for (int l = 0; l<windowSize; l++){
            window[l] = x;
            nowprint = (nowprint + modPrime + x)%modPrime;
            x = (byte) readMe.read();
            System.out.println(nowprint);
        }
        x = (byte) readMe.read();
        currind++;
        while (x != -1){

            //rabin krap
            nowprint = ((nowprint + modPrime - window[whereami]*(base%modPrime)*base%modPrime)*base+ x)%modPrime;

            //update buffer
            window[whereami] = x;
            whereami = (whereami+1)%windowSize;

            System.out.println(nowprint);
            //if chunk boundary, update
            if(nowprint == 57){
                indexlist.add(currind);

                //System.out.println(nowprint);
                //flush le buffer
                whereami = 0;
                for (int l = 0; l<windowSize; l++){
                    x = (byte) readMe.read();
                    currind++;
                    window[l] = x;
                    nowprint = (nowprint*modPrime + window[l])%base;
                }
            }

            x = (byte) readMe.read();
            currind++;
        }
        return indexlist;
    }
}
