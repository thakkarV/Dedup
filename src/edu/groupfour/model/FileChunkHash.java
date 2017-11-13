package edu.groupfour.model;

import java.io.Serializable;

public class FileChunkHash implements Serializable {
    // a base 64 encode of the hash that is returned by hashing the chunk of the file.
    // The chunk is determined by Rabin fingerprinting
    String hash;
}
