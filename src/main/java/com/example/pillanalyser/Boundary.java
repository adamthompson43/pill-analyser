package com.example.pillanalyser;

import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

public class Boundary {

    // max/min will always be larger than original value
    private int minX = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE;
    private int minY = Integer.MAX_VALUE;
    private int maxY = Integer.MIN_VALUE;
    private int pixelCount = 0;

    public void updateBoundary(int x, int y) {
        if (x < minX) minX = x;
        if (x > maxX) maxX = x;
        if (y < minY) minY = y;
        if (y > maxY) maxY = y;
        pixelCount++;
    }

    public int getPixelCount() {
        return pixelCount;
    }

    public void drawBoundary(PixelWriter writer, int imageWidth, int imageHeight) {
        // draw horizontal boundaries
        for (int x = minX; x <= maxX; x++) {
            if (minY >= 0 && minY < imageHeight) {
                writer.setColor(x, minY, Color.RED);
            }
            if (maxY >= 0 && maxY < imageHeight) {
                writer.setColor(x, maxY, Color.RED);
            }
        }
        // draw vertical boundaries
        for (int y = minY; y <= maxY; y++) {
            if (minX >= 0 && minX < imageWidth) {
                writer.setColor(minX, y, Color.RED);
            }
            if (maxX >= 0 && maxX < imageWidth) {
                writer.setColor(maxX, y, Color.RED);
            }
        }
    }

}

