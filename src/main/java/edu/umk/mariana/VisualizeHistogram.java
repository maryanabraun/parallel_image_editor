package edu.umk.mariana;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

public class VisualizeHistogram {

    public static void drawHistogram(BufferedImage image) {
        int nPixels = image.getHeight() * image.getWidth();

        final long start = System.currentTimeMillis();

        int[] redBin = new int[256];
        int[] greenBin = new int[256];
        int[] blueBin = new int[256];


        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                Color color = new Color(image.getRGB(i, j));
                redBin[color.getRed()]++;
                greenBin[color.getGreen()]++;
                blueBin[color.getBlue()]++;
            }
        }
        final long duration = System.currentTimeMillis() - start;
        System.out.printf("Duration for synchronous task is %d ms.%n",  duration);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setSize(256 * 3, 400);
        frame.setResizable(false);
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                for (int i = 0; i < 256; i++) {
                    int red = (int) ((float) redBin[i] / nPixels * frame.getHeight()*50);
                    int green = (int) ((float) greenBin[i] / nPixels * frame.getHeight()*50);
                    int blue = (int) ((float) blueBin[i] / nPixels * frame.getHeight()*50);

                    g.setColor(Color.RED);
                    g.fillRect(i * 3, getHeight() - red, 1, red);
                    g.setColor(Color.GREEN);
                    g.fillRect(i * 3 + 1, getHeight() - green, 1, green);
                    g.setColor(Color.BLUE);
                    g.fillRect(i * 3 + 2, getHeight() - blue, 1, blue);
                }
            }
        };
        panel.setBackground(Color.black);
        frame.add(panel);
        frame.setVisible(true);
    }

}