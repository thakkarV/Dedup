package edu.groupfour.controller;

import edu.groupfour.model.Locker;
import org.apache.commons.cli.CommandLine;

public class CLIRunner {
    private CommandLine parsedCommands;
    CLIRunner(CommandLine parsedCommands) {
        this.parsedCommands = parsedCommands;
    }

    void run() {
        // init locker
        if (this.parsedCommands.hasOption("i")) {
            Locker locker = new Locker(
                    this.parsedCommands.getOptionValue("i"),
                    this.parsedCommands.getOptionValue("n"),
                    4096
            );
        }

        // add file
        else if (this.parsedCommands.hasOption("a")) {
            Locker locker = new Locker(
                    this.parsedCommands.getOptionValue("l")
            );
            locker.addFile(this.parsedCommands.getOptionValue("a"));
            locker.save();
        }

        // add directory
        else if (this.parsedCommands.hasOption("A")) {
            Locker locker = new Locker(
                    this.parsedCommands.getOptionValue("l")
            );

            locker.addDir(
                    this.parsedCommands.getOptionValue("A"),
                    this.parsedCommands.hasOption("R")
            );

            locker.save();
        }

        // retrieve file
        else if (this.parsedCommands.hasOption("r")) {
            if (!this.parsedCommands.hasOption("t")) {
                System.out.println("Target path not provided. Exiting.");
                System.exit(1);
            }

            Locker locker = new Locker(
                    this.parsedCommands.getOptionValue("l")
            );

            locker.retrieve(
                    this.parsedCommands.getOptionValue("r"),
                    this.parsedCommands.getOptionValue("t")
            );
        }

        // delete file
        else if (this.parsedCommands.hasOption("d")) {
            Locker locker = new Locker(
                    this.parsedCommands.getOptionValue("l")
            );

            locker.deleteFile(this.parsedCommands.getOptionValue("d"));
            locker.save();
        }
    }
}
