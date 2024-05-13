package com.example.pillanalyser;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import java.io.File;


public class PillController {

    @FXML
    private MenuItem openFileMenuItem;
    @FXML
    private Image loadedImage;
    @FXML
    private ImageView mainImageView;
    @FXML
    private ImageView secondaryImageView;
    @FXML
    private Slider toleranceSlider;

    private UnionFind unionFind;
    int[] pixels;
    private int lastClickedArgb = 0;

    int width,height = 300;
    int[] array = new int[width * height];




    @FXML
    private void initialize() {
        toleranceSlider.setMin(0);  // Minimum tolerance
        toleranceSlider.setMax(80);  // Maximum tolerance, adjust as needed
        toleranceSlider.setValue(40);  // Default value

        toleranceSlider.valueProperty().addListener((ov, old_val, new_val) -> {
            if (loadedImage != null) {
                createBlackAndWhiteImage(lastClickedArgb);
            }
        });
    }

    @FXML
    private void imageFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        fileChooser.setTitle("Open Image");
        File imageFile = fileChooser.showOpenDialog(null);

        if (imageFile != null) {
            loadImage(imageFile);
        }
    }

    private void loadImage(File imageFile) {
        loadedImage = new Image(imageFile.toURI().toString(), 300, 300, false, false);
        mainImageView.setImage(loadedImage);
        mainImageView.setPreserveRatio(true);
        mainImageView.setFitWidth(300);
        mainImageView.setFitHeight(300);

        secondaryImageView.setImage(loadedImage);
        secondaryImageView.setPreserveRatio(true);
        secondaryImageView.setFitWidth(300);
        secondaryImageView.setFitHeight(300);

        pixels = new int[(int) (loadedImage.getWidth() * loadedImage.getHeight())];
    }

    @FXML
    private void exitApplication() {
        Platform.exit();
    }

    @FXML
    private void mouseCoordinatesOnClick(MouseEvent event) {
        int xInt = (int) event.getX();
        int yInt = (int) event.getY();

        if (xInt >= 0 && xInt < loadedImage.getWidth() && yInt >= 0 && yInt < loadedImage.getHeight()) {
            PixelReader pixelReader = loadedImage.getPixelReader();
            lastClickedArgb = pixelReader.getArgb(xInt, yInt);
            createBlackAndWhiteImage(lastClickedArgb);
        } else {
            System.out.println("Click outside image bounds");
        }
    }

    private void createBlackAndWhiteImage(int referenceArgb) {
        if (loadedImage == null) return;

        PixelReader pixelReader = loadedImage.getPixelReader();
        WritableImage blackAndWhiteImage = new WritableImage((int) loadedImage.getWidth(), (int) loadedImage.getHeight());
        PixelWriter pixelWriter = blackAndWhiteImage.getPixelWriter();

        int tolerance = (int) toleranceSlider.getValue();
        processImage(pixelReader, pixelWriter, referenceArgb, tolerance);

        secondaryImageView.setImage(blackAndWhiteImage);
        unionAdjacentPixels();
        printPixels();
    }

    private void processImage(PixelReader pixelReader, PixelWriter pixelWriter, int referenceArgb, int tolerance) {
        int width = (int) loadedImage.getWidth();
        int height = (int) loadedImage.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                int pixelArgb = pixelReader.getArgb(x, y);
                if (isColorWithinTolerance(pixelArgb, referenceArgb, tolerance)) {
                    pixelWriter.setArgb(x, y, 0xFFFFFFFF);  // White
                    pixels[index] = index;
                } else {
                    pixelWriter.setArgb(x, y, 0xFF000000);  // Black
                    pixels[index] = 0;
                }
            }
        }
    }

    private boolean isColorWithinTolerance(int color1, int color2, int tolerance) {
        // Extract ARGB components of both colors
        int alpha1 = (color1 >> 24) & 0xff;
        int red1 = (color1 >> 16) & 0xff;
        int green1 = (color1 >> 8) & 0xff;
        int blue1 = color1 & 0xff;

        int alpha2 = (color2 >> 24) & 0xff;
        int red2 = (color2 >> 16) & 0xff;
        int green2 = (color2 >> 8) & 0xff;
        int blue2 = color2 & 0xff;

        // Check if each component is within the specified tolerance
        return Math.abs(alpha1 - alpha2) <= tolerance &&
                Math.abs(red1 - red2) <= tolerance &&
                Math.abs(green1 - green2) <= tolerance &&
                Math.abs(blue1 - blue2) <= tolerance;
    }

    private void unionAdjacentPixels() {
        int width = (int) loadedImage.getWidth();
        int height = (int) loadedImage.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                if (pixels[index] != 0) {  // Check if the pixel is part of a white region
                    // Union with the right neighbor
                    if (x < width - 1 && pixels[y * width + (x + 1)] != 0) {
                        UnionFind.union(pixels, index, y * width + (x + 1));
                    }

                    // Union with the bottom neighbor
                    if (y < height - 1 && pixels[(y + 1) * width + x] != 0) {
                        UnionFind.union(pixels, index, (y + 1) * width + x);
                    }
                    UnionFind.find(pixels,index);
                }
            }
        }
    }

    private void printPixels() {
        int width = (int) loadedImage.getWidth();
        int height = (int) loadedImage.getHeight();

        System.out.println("Pixels Array:");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;  // Calculate the index in the 1D array
                System.out.print(pixels[index] + " ");  // Print the pixel value followed by a space
            }
            System.out.println();  // New line after each row to form the grid
        }
    }
}






