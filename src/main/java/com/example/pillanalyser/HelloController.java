package com.example.pillanalyser;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

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
    private int lastClickedArgb = 0;


    @FXML
    private void initialize() {
        toleranceSlider.setMin(0);  // Minimum tolerance
        toleranceSlider.setMax(50000);  // Maximum tolerance, adjust as needed
        toleranceSlider.setValue(25000);  // Default value

        // Add a listener to the slider to update the black and white image when the slider value changes
        toleranceSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                if (loadedImage != null) {  // Only update if an image is loaded
                    createBlackAndWhiteImage(lastClickedArgb);
                }
            }
        });
    }

    @FXML
    private void imageFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files (*.png, *.jpeg, *.jpg)", "*.png", "*.jpg", "*.jpeg"));
        fileChooser.setTitle("Open Image");
        File imageFile = fileChooser.showOpenDialog(null);

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
        int xInt = (int) x;
        int yInt = (int) y;

        if (xInt >= 0 && xInt < loadedImage.getWidth() && yInt >= 0 && yInt < loadedImage.getHeight()) {
            PixelReader pixelReader = loadedImage.getPixelReader();
            lastClickedArgb = pixelReader.getArgb(xInt, yInt);  // Store the clicked color
            createBlackAndWhiteImage(lastClickedArgb);
        } else {
            System.out.print("Click outside image bounds\n");
        }
    }


    @FXML
    private void createBlackAndWhiteImage(int referenceArgb) {
        PixelReader pixelReader = applyGaussianBlur(loadedImage, 1.5).getPixelReader();
        WritableImage blackAndWhiteImage = new WritableImage((int) loadedImage.getWidth(), (int) loadedImage.getHeight());
        PixelWriter pixelWriter = blackAndWhiteImage.getPixelWriter();

        int tolerance = (int) toleranceSlider.getValue();
        for (int y = 0; y < loadedImage.getHeight(); y++) {
            for (int x = 0; x < loadedImage.getWidth(); x++) {
                int pixelArgb = pixelReader.getArgb(x, y);
                int distance = Math.abs(pixelArgb - referenceArgb);
                if (distance < tolerance) {
                    pixelWriter.setArgb(x, y, 0xFFFFFFFF); // White
                } else {
                    pixelWriter.setArgb(x, y, 0xFF000000); // Black
                }
            }
        }

        applyMorphologicalOperations(blackAndWhiteImage);
        secondaryImageView.setImage(blackAndWhiteImage);
    }

    public void createDisjointSet() {
        Image image = mainImageView.getImage();
        int width = (int) image.getWidth();
        for (int i = 0; i < pixels.length; i++) {
            if (pixels[i] != 0) {
                if ((i + 1) % width != 0 && pixels[i + 1] != 0)
                    union(pixels, i, i + 1);
                if (i + width < pixels.length && pixels[i + width] != 0)
                    union(pixels, i, i + width);
            }
        }
    }

    public static int findCompress(int[] a, int id) {
        while (a[id] != id) {
            a[id] = a[a[id]];
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

    private Image applyGaussianBlur(Image source, double radius) {
        ImageView imageView = new ImageView(source);
        imageView.setEffect(new GaussianBlur(radius));
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        return imageView.snapshot(parameters, null);
    }

    private void applyMorphologicalOperations(WritableImage image) {
        // Implement dilation and erosion if needed
        // This is a placeholder and would require actual implementation
    }w
}
