package edu.groupfour.model;

import java.io.Serializable;

class FileChunk implements Serializable{
    byte [] payload;
    int refCounter;
    boolean is_mutated;
}
