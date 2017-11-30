package edu.groupfour.view;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class Selector extends JPanel {
    private JFileChooser fileChooser;
    private JButton selectFiles;

    public Selector(){
        setLayout(new FlowLayout());

        selectFiles = new JButton("Select Input Files");

        selectFiles.addActionListener(e -> {
            fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
            int returnValue = fileChooser.showOpenDialog(null);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                System.out.println(selectedFile.getAbsolutePath());
            }
        });

        add(selectFiles);

    }
}
