package edu.groupfour.controller;

import edu.groupfour.model.Locker;
import edu.groupfour.model.RabinFingerPrint;
import edu.groupfour.view.CLI;
import edu.groupfour.view.GUI;
import org.apache.commons.cli.CommandLine;
import java.io.IOException;
import java.util.ArrayList;


public class Main {
    public static void main(String[] args) throws IOException{
        //GUI gui = new GUI();
	    /*CommandLine parsedArgs = new CLI(args).parse();

	    if (parsedArgs.hasOption("l")) {
	        System.out.println(parsedArgs.getOptionValue("l"));
        }

        Locker locker = new Locker(parsedArgs.getOptionValue("l"));

        if(parsedArgs.hasOption("a")){
            System.out.println("Adding file " + parsedArgs.getOptionValue("a") + " to locker!");
            locker.addFile(parsedArgs.getOptionValue("a"));
        }
*/
        RabinFingerPrint rp = new RabinFingerPrint("test");

        ArrayList<Long> indexlist = new ArrayList<Long>();

        indexlist = rp.getChunkBoundaries("test.txt");
        //STATS
        System.out.println();
        System.out.println("Size of Chunk Index List: " + indexlist.size());

        ArrayList<Integer> diff = new ArrayList<>();

        long rhv = 0;
        for(long i : indexlist){
            diff.add((int)(i - rhv));
            rhv = i;
        }

        System.out.println("Chunk Size Array: " + diff);

        long total = 0;
        for(int i : diff)
            total += i;
        long averageChunkSize = 0;
        if(diff.size() > 1) {
            averageChunkSize = total / diff.size();

            System.out.println("Average Chunk Size: " + averageChunkSize);

            int sum = 0;
            for (Integer i : diff)
                sum += Math.pow((i - averageChunkSize), 2);
            System.out.println("Standard Deviation of the Chunk Size: " + Math.sqrt(sum / (diff.size() - 1)));
        }

        }




//        locker.save();


}
