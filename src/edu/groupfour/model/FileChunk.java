package edu.groupfour.model;

import java.io.Serializable;

public class FileChunk implements Serializable{
    byte [] payload;
    int refCounter;
}
