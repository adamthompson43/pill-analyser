package com.example.pillanalyser;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;

import java.io.File;

public class HelloController {

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

    int[] pixels;

    @FXML
    private void imageFileChooser() {
        FileChooser fileChooser = new FileChooser();

        //set to images only
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files (*.png, *.jpeg, *.jpg", "*.png", "*.jpg", "*.jpeg"));

        //file chooser title
        fileChooser.setTitle("Open Image");

        //selected image
        File imageFile = fileChooser.showOpenDialog(null);

        //display image in image view
        if (imageFile != null) {
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

            greyScaleImage();
        }


    }

    @FXML
    private void exitApplication() {
        Platform.exit();
    }

    @FXML
    private void mouseCoordinatesOnClick(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();

        //these convert the mouse click coordinates to ints because that what getArgb() likes
        int xInt = (int) x;
        int yInt = (int) y;

        PixelReader pixelReader = loadedImage.getPixelReader();
        if (xInt >= 0 && xInt <= 300 && yInt >= 0 && yInt <= 300) {  //makes sure mouse click is within image bounds
            int argb = pixelReader.getArgb(xInt, yInt);

            int alpha = (argb >> 24) & 0xFF;
            int red = (argb >> 16) & 0xFF;
            int green = (argb >> 8) & 0xFF;
            int blue = argb & 0xFF;

            System.out.print("x value is: " + xInt + "\ny value is: " + yInt + "\nargb value is: " + alpha + ", " + red + ", " + green + ", " + blue + "\n");
        } else System.out.print("getArgb() is broken mate\n");
    }

    @FXML
    private void greyScaleImage() {
        if (loadedImage != null) {
            // Get the pixel reader from the loaded image
            PixelReader pixelReader = loadedImage.getPixelReader();

            // Create a writable image with the same dimensions as the loaded image
            WritableImage greyscaleImage = new WritableImage((int) loadedImage.getWidth(), (int) loadedImage.getHeight());
            PixelWriter pixelWriter = greyscaleImage.getPixelWriter();

            for (int y = 0; y < loadedImage.getHeight(); y++) {
                for (int x = 0; x < loadedImage.getWidth(); x++) {
                    // Get the ARGB values of the current pixel
                    int argb = pixelReader.getArgb(x, y);

                    // Extract the alpha, red, green, and blue components
                    int alpha = (argb >> 24) & 0xFF;
                    int red = (argb >> 16) & 0xFF;
                    int green = (argb >> 8) & 0xFF;
                    int blue = argb & 0xFF;

                    // Calculate the greyscale value
                    int greyscaleValue = (int) (0.299 * red + 0.587 * green + 0.114 * blue);

                    // Create a new ARGB value with the greyscale value
                    int greyscaleArgb = (alpha << 24) | (greyscaleValue << 16) | (greyscaleValue << 8) | greyscaleValue;

                    // Write the new ARGB value to the new image
                    pixelWriter.setArgb(x, y, greyscaleArgb);
                }
            }

            // Set the greyscale image to the main image view and changes button text
            secondaryImageView.setImage(greyscaleImage);
        }
    }

    public void createDisjointSet() {
        Image image = mainImageView.getImage();

        int width = (int) image.getWidth();
        for (int i = 0; i < pixels.length; i++) {
            if (pixels[i] != 0) { // if an index is black
                if ((i + 1) % width != 0 && pixels[i + 1] != 0)
                    union(pixels, i, i + 1); // union that index with the index to the right
                if (i + width < pixels.length && pixels[i + width] != 0)
                    union(pixels, i, i + width); // union index with the index below
            }

        }

    }

    public static int findCompress(int[] a, int id) {
        while (a[id] != id) {
            a[id] = a[a[id]]; //Compress path
            id = a[id];
        }
        return id;
    }

    public  static void union(int[] a, int p, int q) {
        a[findCompress(a, q)] = findCompress(a, p);
    }



    @FXML
    private void processImage() {

    }
}
