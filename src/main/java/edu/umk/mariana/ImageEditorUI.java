package edu.umk.mariana;

import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ImageEditorUI {
    private static final float MAX_ZOOM = 10;
    private static final float MIN_ZOOM = 1f;
    private static final float ZOOM_FACTOR = 1.1f;
    private final JFrame frame;
    private final JLabel imageLabel;
    private final int defaultWigth;
    private final int defaultHeight;
    float hue = 0;
    float saturation = 0;
    float brightness = 0;
    private float currentZoom = MIN_ZOOM;
    private JSlider hueSlider;
    private JSlider saturationSlider;
    private JSlider brightnessSlider;
    private BufferedImage copiedImage;
    private BufferedImage originalImage;

    public ImageEditorUI() throws Exception {
//        final File imageFile = new File("D:\\AI_generation\\my_redactor\\cyber_girl.jpg");// source\img.jpg
        final URL imageFile = getClass().getResource("/files/cyber_girl.jpg");// source\img.jpg
        assert imageFile != null;
        this.originalImage = ImageIO.read(imageFile);
        this.copiedImage = cloneImage(this.originalImage);


        this.imageLabel = new JLabel();
        final Image scaledImage = getScaledImage(copiedImage);

        int scaledWigth = scaledImage.getWidth(null);
        int scaledHeight = scaledImage.getHeight(null);
        while (scaledWigth == -1 || scaledHeight == -1) {
            scaledWigth = scaledImage.getWidth(null);
            scaledHeight = scaledImage.getHeight(null);
        }
        this.defaultWigth = scaledWigth;
        this.defaultHeight = scaledHeight;

        this.imageLabel.setIcon(new ImageIcon(scaledImage));

        // Create the frame
        frame = new JFrame("Image Editor");

        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        final JPanel panel = addSliders();

        final JButton chooseImageButton = addChooseImgButton();
        final JButton saveImageButton = addSaveImgButton();
        final JButton resetImageButton = addResetImageButton();
        final JButton showHistogramButton = addShowHistogramButton();

        panel.add(chooseImageButton);
        panel.add(saveImageButton);
        panel.add(resetImageButton);
        panel.add(showHistogramButton);
        panel.add(imageLabel, BorderLayout.CENTER);
        frame.add(panel);


    }

    private static Image getScaledImage(@NotNull BufferedImage image) {
        final int newWidth;
        final int newHeight;
        if (image.getHeight() >= image.getWidth()) {
            newHeight = 500;
            newWidth = (int) ((double) image.getWidth() / image.getHeight() * 500);
        } else {
            newWidth = 500;
            newHeight = (int) ((double) image.getHeight() / image.getWidth() * 500);
        }

        return image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
    }

    public void showOriginalImage() {
        this.imageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                final Image scaledImage = getScaledImage(originalImage);
                imageLabel.setIcon(new ImageIcon(scaledImage));

                frame.paint(scaledImage.getGraphics());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                processImage(originalImage, copiedImage);
                final Image scaledImage = getScaledImage(copiedImage);
                imageLabel.setIcon(new ImageIcon(scaledImage));
                frame.repaint();
            }
        });

    }
    private static @NotNull BufferedImage cloneImage(@NotNull BufferedImage originalImage) {
        final ColorModel cm = originalImage.getColorModel();
        final boolean isAlphaPremultiplied = originalImage.isAlphaPremultiplied();
        final WritableRaster raster = originalImage.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }


    private @NotNull JButton addResetImageButton() {
        final JButton resetImageButton = new JButton("Reset");
        resetImageButton.addActionListener(e -> {
            copiedImage = cloneImage(originalImage);
            final Image scaledImage = getScaledImage(copiedImage);
            imageLabel.setIcon(new ImageIcon(scaledImage));
            this.hue = 0;
            this.hueSlider.setValue(0);
            this.saturation = 0;
            this.saturationSlider.setValue(0);
            this.brightness = 0;
            this.brightnessSlider.setValue(0);
            this.currentZoom = 1;
            frame.repaint();
        });
        return resetImageButton;
    }

    public JFrame getFrame() {
        return frame;
    }

    private void addSliderChangeListeners() {
        hueSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                hue = hueSlider.getValue();
                copiedImage = cloneImage(originalImage);
                processImage(originalImage, copiedImage);
            }
        });

        saturationSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                saturation = (float) saturationSlider.getValue() / 100;
                copiedImage = cloneImage(originalImage);
                processImage(originalImage, copiedImage);
            }
        });
        // sat = sliderSat
        brightnessSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                brightness = (float) brightnessSlider.getValue() / 100;
                copiedImage = cloneImage(originalImage);
                processImage(originalImage, copiedImage);
            }
        });
    }


    private @NotNull JButton addSaveImgButton() {
        final JButton saveImageButton = new JButton("Save Image");

        saveImageButton.addActionListener(e -> {
            final JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showSaveDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                saveImage(file, copiedImage);
            }
        });
        return saveImageButton;
    }

    private void saveImage(File file, BufferedImage image) {
        try {
            ImageIO.write(image, "jpg", file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private @NotNull List<Thread> divideIntoChunks(final BufferedImage originalImage,
                                                   final @NotNull BufferedImage copiedImage,
                                                   int chunksHor, int chunksVer) {
        final int chunkWidth = copiedImage.getWidth() / chunksHor;
        final int leftoverWidth = copiedImage.getWidth() % chunksHor;
        final int chunkHeight = copiedImage.getHeight() / chunksVer;
        final int leftoverHeight = copiedImage.getHeight() % chunksVer;

        final List<Thread> threads = new ArrayList<>(chunksHor * chunksVer);
        for (int i = 0; i < chunksHor; i++) {
            for (int j = 0; j < chunksVer; j++) {
                // Create a Runnable that processes the current chunk of the image
                final Runnable task = new ImageProcessingTask(
                        originalImage, copiedImage,
                        i * chunkWidth, j * chunkHeight,
                        chunkWidth + (i == chunksHor - 1 ? leftoverWidth : 0),
                        chunkHeight + (j == chunksVer - 1 ? leftoverHeight : 0),
                        hue, saturation, brightness
                );
                // Create a Thread for the Runnable and start it
                final Thread thread = new Thread(task);
                thread.start();
                threads.add(thread);
            }
        }
        return threads;
    }
    private static void joinThreads(final @NotNull Collection<Thread> threads) {
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    private void processImage(BufferedImage originalImage, BufferedImage copiedImage) {
        final int chunksHor = 2;
        final int chunksVer = 4;

//        final long start = System.currentTimeMillis();

        final Collection<Thread> threads = divideIntoChunks(
                originalImage, copiedImage, chunksHor, chunksVer
        );
        joinThreads(threads);
//
//        final long duration = System.currentTimeMillis() - start;
//        System.out.printf("Image processing duration for %d threads is %d ms.%n", chunksHor * chunksVer, duration);

        // Update the image displayed in the frame
        final Image scaledImage = getScaledImage(copiedImage);

        imageLabel.setIcon(new ImageIcon(scaledImage));
        frame.repaint();
    }

    private @NotNull JButton addShowHistogramButton() {
        final JButton showHistogramButton = new JButton("RGB-histogram");
        final int nChunksX = 4;
        final int nChunksY = 2;
        HistogramVisualizer histogram = new HistogramVisualizer(nChunksX, nChunksY);

        showHistogramButton.addActionListener(e -> {
            try {
                histogram.calculateHistogram(copiedImage);
//                VisualizeHistogram.drawHistogram(copiedImage);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });
        return showHistogramButton;
    }

    private @NotNull JButton addChooseImgButton() {
        final JButton chooseImageButton = new JButton("Choose Image");
        chooseImageButton.addActionListener(e -> {
            final JFileChooser fileChooser = new JFileChooser();
            final int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    this.originalImage = ImageIO.read(file);
                    this.copiedImage = cloneImage(originalImage);
                    Image scaledImage = getScaledImage(copiedImage);
                    this.imageLabel.setIcon(new ImageIcon(scaledImage));
                    this.hue = 0;
                    this.hueSlider.setValue(0);
                    this.saturation = 0;
                    this.saturationSlider.setValue(0);
                    this.brightness = 0;
                    this.brightnessSlider.setValue(0);

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        return chooseImageButton;
    }

    public void zoomImage(int rotation) {
        // Zoom in or out based on the wheel rotation
        if (rotation < 0) {
            this.currentZoom *= ZOOM_FACTOR;
            performZooming(currentZoom >= MAX_ZOOM, MAX_ZOOM);
        } else {
            this.currentZoom /= ZOOM_FACTOR;
            performZooming(currentZoom <= MIN_ZOOM, MIN_ZOOM);
        }
    }

    private void performZooming(boolean isInBounds, float bound) {
        if (isInBounds) {
            this.currentZoom = bound;
            return;
        }
        int newWidth = (int) (this.defaultWigth * currentZoom);
        int newHeight = (int) (this.defaultHeight * currentZoom);
        final Image scaledImage = copiedImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(scaledImage));
        frame.repaint();
    }

    private @NotNull JPanel addSliders() {
        final JPanel sliderPanel = new JPanel();
        this.hueSlider = new JSlider(-180, 180, 0);
        this.saturationSlider = new JSlider(-70, 70, 0);
        this.brightnessSlider = new JSlider(-50, 50, 0);
        addSliderChangeListeners();
        sliderPanel.add(new JLabel("Hue:"));
        sliderPanel.add(hueSlider);
        sliderPanel.add(new JLabel("Saturation:"));
        sliderPanel.add(saturationSlider);
        sliderPanel.add(new JLabel("Value:"));
        sliderPanel.add(brightnessSlider);
        return sliderPanel;
    }

}

