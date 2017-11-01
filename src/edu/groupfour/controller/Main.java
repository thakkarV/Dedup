package edu.groupfour.controller;

import edu.groupfour.view.CLI;
import org.apache.commons.cli.CommandLine;

import javax.sound.midi.SysexMessage;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Main {

    public static void main(String[] args) {
	    CommandLine parsedArgs = new CLI(args).parse();

	    if (parsedArgs.hasOption("l")) {
	        System.out.println(parsedArgs.getOptionValue("l"));
        }

        System.exit(0);
    }
}
