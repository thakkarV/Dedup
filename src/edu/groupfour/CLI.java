package edu.groupfour;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.*;

 class CLI {

     CLI(String [] args) {
        this.args = args;

        this.options = new Options();
        options.addOption("h", "help", false, "Show help.");
        options.addOption("a", "addFile", true, "Path to the file to be added to the locker.");
        options.addOption("l", "locker", true, "Path to the locker.");
        options.addOption("d", "deleteFile", true, "Name of the file to be deleted from the locker.");
    }

    CommandLine parse() {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmdline = null;

        try {
            cmdline = parser.parse(this.options, this.args);

            if (cmdline.hasOption("h"))
                showHelp();

            if (cmdline.hasOption("d")) {
                System.out.println("Deleting form locker is not yet supported.");
                log.log(Level.INFO, "User tried using unsupported delete flag.");
                System.exit(0);
            }
        }
        catch (ParseException e) {
            log.log(Level.SEVERE, "Failed to parse command line arguments.");
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
    private static final Logger log  = Logger.getLogger(CLI.class.getName());
}
