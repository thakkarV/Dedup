package edu.groupfour.model;

import java.io.Serializable;
import java.util.ArrayList;

class LockerFile implements Serializable {
    String localFilePath;
    ArrayList<FileChunkHash> chunks;
    boolean is_mutated;
}