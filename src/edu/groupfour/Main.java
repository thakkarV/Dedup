package edu.groupfour;

import org.apache.commons.cli.CommandLine;

import java.util.logging.Level;
import java.util.logging.Logger;


public class Main {

    public static void main(String[] args) {
	    CommandLine parsedArgs = new CLI(args).parse();
    }
}
