package edu.groupfour.controller;

import edu.groupfour.model.Locker;
import edu.groupfour.view.CLI;
import org.apache.commons.cli.CommandLine;

public class CLIRunner {
    private CommandLine parsedCommands;
    public CLIRunner(CommandLine parsedCommands) {
        this.parsedCommands = parsedCommands;
    }

    void run() {
        // add file
        if (this.parsedCommands.hasOption("a")) {
            Locker locker = new Locker(this.parsedCommands.getOptionValue("l"), false);
            locker.addFile(this.parsedCommands.getOptionValue("a"));
        }
        // add directory
        else if (this.parsedCommands.hasOption("A")) {
            Locker locker = new Locker(this.parsedCommands.getOptionValue("l"), false);
            locker.addDir(this.parsedCommands.getOptionValue("A"), this.parsedCommands.hasOption("R"));
            locker.save();
        }
        // retrieve file
        else if (this.parsedCommands.hasOption("r")) {
            if (!this.parsedCommands.hasOption("t")) {
                System.out.println("Target path not provided. Exiting.");
                System.exit(1);
            }

            Locker locker = new Locker(this.parsedCommands.getOptionValue("l"), false);
            locker.retrieve(this.parsedCommands.getOptionValue("r"), this.parsedCommands.getOptionValue("t"));
        }
        // init locker
        else if (this.parsedCommands.hasOption("i")) {
            Locker locker = new Locker(this.parsedCommands.getOptionValue("l"), true);
        }
        // delete file
        else if (this.parsedCommands.hasOption("d")) {
            Locker locker = new Locker(this.parsedCommands.getOptionValue("l"), false);
            locker.deleteFile(this.parsedCommands.getOptionValue("d"));
        }
    }
}
