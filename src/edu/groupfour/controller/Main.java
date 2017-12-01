package edu.groupfour.controller;

import edu.groupfour.view.CLI;

public class Main {
    public static void main(String [] args) {
        CLI cli = new CLI(args);

        CLIRunner runner = new CLIRunner(cli.parse());
        runner.run();
    }
}
