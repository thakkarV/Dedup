package edu.groupfour.model;

import java.io.Serializable;
import java.util.ArrayList;

public class LockerFile implements Serializable{
    String localFilePath;
    ArrayList<FileChunkHash> chunks;
}