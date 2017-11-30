package edu.groupfour.controller;

import edu.groupfour.model.Locker;
import edu.groupfour.model.RabinFingerPrint;
import edu.groupfour.view.CLI;
import edu.groupfour.view.GUI;
import org.apache.commons.cli.CommandLine;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;


public class Main {
    public static void main(String [] args) throws IOException {
        CLI cli = new CLI(args);

        CLIRunner runner = new CLIRunner(cli.parse());
        runner.run();
    }
}
