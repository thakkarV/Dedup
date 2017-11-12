package edu.groupfour.controller;

import edu.groupfour.model.Locker;
import edu.groupfour.view.CLI;
import edu.groupfour.view.GUI;
import org.apache.commons.cli.CommandLine;
import java.io.IOException;


public class Main {
    public static void main(String[] args) throws IOException{
        //GUI gui = new GUI();
	    CommandLine parsedArgs = new CLI(args).parse();

	    if (parsedArgs.hasOption("l")) {
	        System.out.println(parsedArgs.getOptionValue("l"));
        }

        Locker locker = new Locker(parsedArgs.getOptionValue("l"));

        if(parsedArgs.hasOption("a")){
            System.out.println("Adding file " + parsedArgs.getOptionValue("a") + " to locker!");
            locker.addFile(parsedArgs.getOptionValue("a"));
        }

        locker.save();

    }
}
