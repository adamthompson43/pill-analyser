package com.example.pillanalyser;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import javafx.scene.paint.Color;


public class PillController {

    @FXML
    public MenuItem exitMenuItem;
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

    @FXML
    private Label nameLabel;

    @FXML
    private Label amountLabel;

    @FXML
    private Label sizeLabel;

    int[] pixels;
    private int lastClickedArgb = 0;

    int width,height = 300;
    int[] array = new int[width * height];

    private Map<Integer, Boundary> boundaries = new HashMap<>();

    private void countBoundaries() {
        int numberOfBoundaries = boundaries.size();
        System.out.println("Total distinct boundaries found: " + numberOfBoundaries);
        amountLabel.setText("Amount: " + numberOfBoundaries);
    }

    @FXML
    void initialize() {
        toleranceSlider.setMin(0);
        toleranceSlider.setMax(80);
        toleranceSlider.setValue(40);

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

    void loadImage(File imageFile) {
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
        if (loadedImage != null) {
            if (xInt >= 0 && xInt < loadedImage.getWidth() && yInt >= 0 && yInt < loadedImage.getHeight()) {
                PixelReader pixelReader = loadedImage.getPixelReader();
                lastClickedArgb = pixelReader.getArgb(xInt, yInt);
                createBlackAndWhiteImage(lastClickedArgb);
            } else {
                System.out.println("Click outside image bounds");
            }
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
    }


    private void processImage(PixelReader pixelReader, PixelWriter pixelWriter, int referenceArgb, int tolerance) {
        int width = (int) loadedImage.getWidth();
        int height = (int) loadedImage.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                int pixelArgb = pixelReader.getArgb(x, y);
                if (isColorWithinTolerance(pixelArgb, referenceArgb, tolerance)) {
                    pixelWriter.setArgb(x, y, 0xFFFFFFFF);  // white
                    pixels[index] = index;
                } else {
                    pixelWriter.setArgb(x, y, 0xFF000000);  // black
                    pixels[index] = 0;
                }
            }
        }

        // calls all methods related to image processing
        unionAdjacentPixels();
        calculateBoundaries();
        drawBoundaries();
        printPixels();
        countBoundaries();
        calculateAndReportPillSizes();
    }

    boolean isColorWithinTolerance(int color1, int color2, int tolerance) {
        // extract argb components of both colors
        int alpha1 = (color1 >> 24) & 0xff;
        int red1 = (color1 >> 16) & 0xff;
        int green1 = (color1 >> 8) & 0xff;
        int blue1 = color1 & 0xff;

        int alpha2 = (color2 >> 24) & 0xff;
        int red2 = (color2 >> 16) & 0xff;
        int green2 = (color2 >> 8) & 0xff;
        int blue2 = color2 & 0xff;

        // checks if each component is within the tolerance
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
                if (pixels[index] != 0) {  // checks if the pixel is part of a white region
                    // unions with the right neighbor
                    if (x < width - 1 && pixels[y * width + (x + 1)] != 0) {
                        UnionFind.union(pixels, index, y * width + (x + 1));
                    }

                    // unions with the bottom neighbor
                    if (y < height - 1 && pixels[(y + 1) * width + x] != 0) {
                        UnionFind.union(pixels, index, (y + 1) * width + x);
                    }
                    UnionFind.find(pixels,index);
                }
            }
        }
    }

    private void printPixels() {  // not functional, just for testing
        int width = (int) loadedImage.getWidth();
        int height = (int) loadedImage.getHeight();

        System.out.println("Pixels Array:");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                System.out.print(pixels[index] + " ");
            }
            System.out.println();
        }
    }

    private void calculateBoundaries() {
        int width = (int) loadedImage.getWidth();
        int height = (int) loadedImage.getHeight();
        Map<Integer, Boundary> tempBoundaries = new HashMap<>(); // temporary storage for boundaries

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;  // calculates index of pixel in the array
                if (pixels[index] != 0) {  // non zero pixels are pills
                    int root = UnionFind.find(pixels, index);  // finds root of pixel using unionfind
                    tempBoundaries.computeIfAbsent(root, k -> new Boundary()).updateBoundary(x, y);  // if there is no boundary, boundary is updated
                }
            }
        }

        int sizeThreshold = 75;  // threshold for minimum size of a boundary

        // helps filter out unwanted components
        boundaries.clear();
        tempBoundaries.forEach((key, value) -> {
            if (value.getPixelCount() >= sizeThreshold) {
                boundaries.put(key, value);
            }
        });
    }

    private void drawBoundaries() {
        WritableImage boundaryImage = new WritableImage(loadedImage.getPixelReader(),
                (int) loadedImage.getWidth(),
                (int) loadedImage.getHeight());
        PixelWriter writer = boundaryImage.getPixelWriter();

        boundaries.values().forEach(boundary -> boundary.drawBoundary(writer, (int) loadedImage.getWidth(), (int) loadedImage.getHeight()));

        mainImageView.setImage(boundaryImage);
    }

    private void calculateAndReportPillSizes() {
        int width = (int) loadedImage.getWidth();
        int height = (int) loadedImage.getHeight();
        Map<Integer, Boundary> tempBoundaries = new HashMap<>();

        // scans every pixel to determine which root each belongs to and update the boundaries
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                if (pixels[index] != 0) {  // Check if the pixel is part of a detected component
                    int root = UnionFind.find(pixels, index);
                    tempBoundaries.computeIfAbsent(root, k -> new Boundary()).updateBoundary(x, y);
                }
            }
        }

        int sizeThreshold = 50;  // minimum size for a pill to be suitable
        boundaries.clear();  //clears the map
        int totalPixelCount = 0;
        int validPillCount = 0;

        // filter boundaries to find valid pills and calculate the required total pixel count and count of pills
        for (Map.Entry<Integer, Boundary> entry : tempBoundaries.entrySet()) {
            Boundary boundary = entry.getValue();
            if (boundary.getPixelCount() >= sizeThreshold) {
                boundaries.put(entry.getKey(), boundary);
                totalPixelCount += boundary.getPixelCount();
                validPillCount++;
                System.out.println("Pill with root " + entry.getKey() + " has " + boundary.getPixelCount() + " pixels.");
            }
        }

        // calculate the average size if there are valid pills
        double averageSize = validPillCount > 0 ? (double) totalPixelCount / validPillCount : 0;
        System.out.println("Average size of pills: " + averageSize + " pixels");
        sizeLabel.setText("Average size: " + averageSize + "px");

    }

    private Color generateRandomColor() {  // will be used for random pill colours
        Random rand = new Random();
        // Generate random colors with full opacity
        return Color.rgb(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256), 1.0);
    }





}






