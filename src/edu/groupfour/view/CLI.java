package edu.groupfour.view;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.*;

 public class CLI {
     // members
     private String [] args;
     private Options options;

     public CLI(String [] args) {
         this.args = args;
         this.options = new Options();

         // these options are mutually exclusive
         Option help = new Option("h", "help", false, "Show help.");
         Option addFile = new Option("a", "addFile", true, "Path to the file to be added to the locker.");
         Option addDir = new Option("A", "addDir", true, "Path to the directory to be added to the locker.");
         Option retrieve = new Option("r", "retrieve", true, "Local path of the file to be retrieved.");
         Option init = new Option("i", "init", true, "Initialize a new locker at the input path.");
         Option delete = new Option("d", "delete", true, "Name of the file to be deleted from the locker.");

         // these are dependent on the previous mutually exclusive operations
         Option locker = new Option("l", "locker", true, "Path to an existing locker.");
         Option target = new Option("t", "target", false, "Path to where the file retrieved should go.");
         Option recursiveAdd = new Option("-R", "Recursive", false, "True if recursively adding all child directories");

         OptionGroup operations = new OptionGroup();
         operations.addOption(help)
                 .addOption(addFile)
                 .addOption(addDir)
                 .addOption(retrieve)
                 .addOption(init)
                 .addOption(delete);

         operations.setRequired(true);

         this.options.addOptionGroup(operations);
         this.options.addOption(locker);
         this.options.addOption(target);
         this.options.addOption(recursiveAdd);
     }

    public CommandLine parse() {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmdline = null;

        try {
            cmdline = parser.parse(this.options, this.args);

            if (cmdline.hasOption("h")) {
                showHelp();
                System.exit(0);
            }
        } catch (ParseException e) {
            // log.log(Level.SEVERE, "Failed to parse command line arguments.");
            showHelp();
        }

        return cmdline;
    }

    private void showHelp() {
        HelpFormatter helper = new HelpFormatter();

        helper.printHelp("Main", this.options);
        System.exit(0);
    }

    // private static final Logger log  = Logger.getLogger(CLI.class.getName());
}
