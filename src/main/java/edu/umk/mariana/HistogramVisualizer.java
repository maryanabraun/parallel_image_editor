package edu.umk.mariana;


import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class HistogramVisualizer {
    private final int nChunksX;
    private final int nChunksY;

    public HistogramVisualizer(int nChunksX, int nChunksY) {
        this.nChunksX = nChunksX;
        this.nChunksY = nChunksY;
    }

    public void calculateHistogram(final @NotNull BufferedImage image) throws InterruptedException {
        final var threads = new ArrayList<Thread>(nChunksX * nChunksY);
        final int height = image.getHeight();
        final int width = image.getWidth();
        final int chunkHeight = height / nChunksY;
        final int chunkWidth = width / nChunksX;
        int nPixels = image.getHeight() * image.getWidth();

        final long start = System.currentTimeMillis();

        final var redBin = new AtomicIntegerArray(256);
        final var greenBin = new AtomicIntegerArray(256);
        final var blueBin = new AtomicIntegerArray(256);

        for (int i = 0; i < nChunksY; i++) {
            final int startY = i * chunkHeight;
            final int endY = calcEnd(i, nChunksY, chunkHeight, height);
            for (int j = 0; j < nChunksX; j++) {
                final int startX = j * chunkWidth;
                final int endX = calcEnd(j, nChunksX, chunkWidth, width);

                Thread thread = new Thread(() -> {
                    for (int y = startY; y < endY; y++) {
                        for (int x = startX; x < endX; x++) {
                            Color color = new Color(image.getRGB(x, y));
                            redBin.getAndIncrement(color.getRed());
                            greenBin.getAndIncrement(color.getGreen());
                            blueBin.getAndIncrement(color.getBlue());
                        }
                    }
                });
                threads.add(thread);
                thread.start();
            }
        }
        for (Thread thread : threads) {
            thread.join();
        }

        final long duration = System.currentTimeMillis() - start;
        System.out.printf("Duration for %d threads is %d ms.%n", nChunksX * nChunksY, duration);

        visualizeHistogram(
                intoIntArray(redBin),
                intoIntArray(greenBin),
                intoIntArray(blueBin),
                nPixels
        );
    }

    private int calcEnd(int idx, int nChunks, int chunkDimSize, int imageDimSize) {
        int end = (idx + 1) * chunkDimSize;
        if (idx == nChunks - 1) {
            end = imageDimSize;
        }
        return end;
    }

    private int @NotNull [] intoIntArray(@NotNull AtomicIntegerArray atomicIntArray) {
        final int[] arr = new int[atomicIntArray.length()];
        for (int i = 0; i < atomicIntArray.length(); i++) {
            arr[i] = atomicIntArray.get(i);
        }
        return arr;
    }

    private void visualizeHistogram(int[] redBin, int[] greenBin, int[] blueBin, int nPixels) {
        JFrame frame = new JFrame("RGB-histogram");
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

//                double max = Double.NEGATIVE_INFINITY;
//                for (int i = 0; i < 256; i++) {
//                    max = Math.max(max, Math.log10(redBin[i]));
//                    max = Math.max(max, Math.log10(greenBin[i]));
//                    max = Math.max(max, Math.log10(blueBin[i]));
//                }
//                assert max != 0;
                for (int i = 0; i < 256; i++) {
//                    int redColH = (int) (Math.log10(redBin[i]) / max * frame.getHeight());
//                    int greenColH = (int) (Math.log10(greenBin[i]) / max * frame.getHeight());
//                    int blueColH = (int) (Math.log10(blueBin[i]) / max * frame.getHeight());
                    int redColH = (int) ((float) redBin[i] / nPixels * frame.getHeight() * 50);
                    int greenColH = (int) ((float) greenBin[i] / nPixels * frame.getHeight() * 50);
                    int blueColH = (int) ((float) blueBin[i] / nPixels * frame.getHeight() * 50);
                    g.setColor(Color.RED);
                    g.fillRect(i * 3, getHeight() - redColH, 1, redColH);
                    g.setColor(Color.GREEN);
                    g.fillRect(i * 3 + 1, getHeight() - greenColH, 1, greenColH);
                    g.setColor(Color.BLUE);
                    g.fillRect(i * 3 + 2, getHeight() - blueColH, 1, blueColH);
                }
            }
        };
        panel.setPreferredSize(new Dimension(256 * 3, 400));
        panel.setBackground(Color.black);
        frame.setResizable(false);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }
}
