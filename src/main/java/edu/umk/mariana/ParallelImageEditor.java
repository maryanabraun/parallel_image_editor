package edu.umk.mariana;

import javax.swing.*;
import java.awt.*;

public class ParallelImageEditor {
    public static void main(String[] args) throws Exception {

        ImageEditorUI ui = new ImageEditorUI();
        JFrame frame = ui.getFrame();

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1080, 720));
        frame.pack();

        frame.addMouseWheelListener(e -> {
            int rotation = e.getWheelRotation();
            ui.zoomImage(rotation);
        });
        ui.showOriginalImage();

        frame.setVisible(true);
    }
}

