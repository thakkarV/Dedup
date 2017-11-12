package edu.groupfour.view;

import javax.swing.*;

public class GUI extends JFrame{
    Selector selector;

    private void createAndShowGUI() {
        selector = new Selector();
        this.add(selector);

        this.setDefaultLookAndFeelDecorated(true);
        this.setVisible(true);
        this.setSize(800,600);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public GUI(){
        super("The Deduplicator");
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
