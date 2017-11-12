package edu.groupfour.view;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.*;

 public class CLI {

     public CLI(String [] args) {
         this.args = args;
         this.options = new Options();

         // these options are mutually exclusive
         Option help = new Option("h", "help", false, "Show help.");
         Option add = new Option("a", "add", true, "Path to the file to be added to the locker.");
         Option retrieve = new Option("r", "retrieve", true, "Local path of the file to be retrieved.");
         Option init = new Option("i", "init", true, "Initialize a new locker at the input path.");
         Option delete = new Option("d", "delete", true, "Name of the file to be deleted from the locker.");

         // these are dependent on the previous mutually exclusive operations
         Option locker = new Option("l", "locker", true, "Path to an existing locker.");
         Option target = new Option("t", "target", true, "Path to where the file retrieved should go.");

         OptionGroup operations = new OptionGroup();
         operations.addOption(help)
                 .addOption(add)
                 .addOption(retrieve)
                 .addOption(init)
                 .addOption(delete);

         operations.setRequired(true);

         this.options.addOptionGroup(operations);
         this.options.addOption(locker);
         this.options.addOption(target);
     }

    public CommandLine parse() {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmdline = null;

        try {
            cmdline = parser.parse(this.options, this.args);

            if (cmdline.hasOption("h"))
                showHelp();

            if (cmdline.hasOption("d")) {
                System.out.println("Deleting form locker is not yet supported.");
                // log.log(Level.INFO, "User tried using unsupported delete flag.");
                System.exit(0);
            }
        }
        catch (ParseException e) {
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

    // members
    private String [] args;
    private Options options;
    // private static final Logger log  = Logger.getLogger(CLI.class.getName());
}
