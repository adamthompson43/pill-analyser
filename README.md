# Pill Analyser

This is a JavaFX project that analyses a picture of pills and groups them by their colours. This project was developed as part of CA1 for the Data Structures and Algorithms module at SETU.

## Instructions
* Open an image in the program using File>Open
* Click a pill in the top image view to find other pills of similar colour
* Use the tolerance slider to adjust the colour variance needed for the program to group pills

## Details
* This program uses a custom linked list algorithm developed in previous labs
* The image is analysed on a pixel by pixel basis
* The image is converted into a 2D array and each pixel is compared
* Pixels with similar colours are grouped together
* The second image view is then updated to show the pixel groups, resulting in all similar colour pills being highlighted
