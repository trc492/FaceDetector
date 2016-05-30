/*
 * Copyright (c) 2016 Titan Robotics Club (http://www.titanrobotics.net)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.awt.*;
import java.awt.image.*;

import javax.swing.*;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

/**
 * This is the main class of the Face Detector program. This program experiments with Face Detection
 * using OpenCV. It contains example code to:
 * - Capture video with the default camera.
 * - Detect faces with OpenCV using the FrontalFace CascadeSpecifier.
 * - Scale an image to a different size.
 * - Overlay an image onto another image with transparency.
 * - Translate between the OpenCV Mat image format and the BufferedImage format for display.
 */
public class FaceDetector extends JPanel
{
    private static final long serialVersionUID = 1L;
    private static final String programTitle = "OpenCV Face Detector";
    private static final String classifierPath = "cascade-files/haarcascade_frontalface_alt.xml";
    private static final String overlayImagePath = "images/Mustache.png";

    private VideoCapture camera;
    private Mat image;
    private MatOfRect faceRects;
    private Mat overlayImage;
    private CascadeClassifier faceDetector;

    /**
     * This is the entry point of the program. It creates and initializes the main window. It also
     * creates and initializes the FaceDetector class.
     *
     * @param args specifies an array of command arguments (not used).
     */
    public static void main(String[] args)
    {
        EventQueue.invokeLater(
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        JFrame frame = new JFrame(programTitle);
                        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        frame.setLayout(null);
                        frame.setResizable(false);
                        frame.setContentPane(new FaceDetector(frame));
                        frame.setLocationRelativeTo(null);
                        frame.setVisible(true);
                    }
                });
    }   //main

    /**
     * Constructor: Creates an instance of the object.
     *
     * @param frame specifies the parent window.
     */
    private FaceDetector(JFrame frame)
    {
        //
        // Load OpenCV library.
        //
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        //
        // Open the default camera.
        //
        camera = new VideoCapture(0);
        if (!camera.isOpened())
        {
            throw new RuntimeException("Failed to open camera");
        }
        //
        // Preallocated some global variables.
        //
        image = new Mat();
        faceRects = new MatOfRect();
        //
        // Load the overlay image and preserves the alpha channel (i.e. transparency).
        //
        overlayImage = Highgui.imread(overlayImagePath, -1);
        //
        // Load the Frontal Face cascade specifier as the face detector.
        //
        faceDetector = new CascadeClassifier(classifierPath);
        if (faceDetector.empty())
        {
            throw new RuntimeException("Failed to load Cascade Classifier <" + classifierPath + ">");
        }
        //
        // Determine the camera image size and make the window size to match.
        //
        camera.read(image);
        frame.setSize(image.width(), image.height() + 35);
        //
        // Create the Refresh thread to refresh the video pane at 30fps.
        //
        new RefreshThread(this).start();
    }   //FaceDetector

    /**
     * This method is called whenever Java VM determined that the JPanel needs to be repainted.
     *
     * @param g specifies the graphics object to be repainted.
     */
    public void paint(Graphics g)
    {
        //
        // Capture an image and subject it for face detection. The face detector produces an array
        // of rectangles representing faces detected.
        //
        camera.read(image);
        faceDetector.detectMultiScale(image, faceRects);
        //
        // We may want to overlay a circle or rectangle on each detected faces or
        // we can overlay a fun image onto a detected face. Play with the code in
        // this for-loop and let your imagination run wild.
        //
        Rect[] rects = faceRects.toArray();
        for (int i = 0; i < rects.length; i++)
        {
            /*
            //
            // Draw a circle around the detected face.
            //
            Core.ellipse(
                    image,
                    new RotatedRect(
                            new Point(rects[i].x + rects[i].width/2, rects[i].y + rects[i].height/2),
                            rects[i].size(),
                            0.0),
                    new Scalar(0, 255, 0));
            */
            /*
            //
            // Draw a rectangle around the detected face.
            //
            Core.rectangle(
                    image,
                    new Point(rects[i].x, rects[i].y),
                    new Point(rects[i].x + rects[i].width, rects[i].y + rects[i].height),
                    new Scalar(0, 255, 0));
            */
            //
            // Only overlay fun image to the first detected face.
            //
            if (i == 0)
            {
                //
                // Scale the fun image to the same size as the face.
                //
                Mat scaledOverlay = new Mat();
                Imgproc.resize(overlayImage, scaledOverlay, rects[i].size());
                //
                // Overlay the scaled image to the camera image.
                //
//                overlayImage(image, scaledOverlay, new Point(rects[i].x, rects[i].y - rects[i].height));
                overlayImage(image, scaledOverlay, new Point(rects[i].x, rects[i].y));
            }
        }
        //
        // Convert the OpenCV Mat image format to BufferedImage format and draw it on the video pane.
        //
        g.drawImage(MatToBufferedImage(image), 0, 0, null);
    }   //paint

    /**
     * This method combines an overlay image to the given background image at the specified location.
     * It is expecting both the background and overlay are color images. It also expects the overlay
     * image contains an alpha channel for opacity information.
     *
     * @param background specifies the background image.
     * @param overlay specifies the overlay image.
     * @param location specifies the location on the background image where the upper left corner of
     *        the overlay image should be at.
     */
    private void overlayImage(Mat background, Mat overlay, Point location)
    {
        //
        // Make sure the background image has 3 channels and the overlay image has 4 channels.
        //
        if (background.channels() == 3 && overlay.channels() == 4)
        {
            //
            // For each row of the overlay image.
            //
            for (int row = 0; row < overlay.rows(); row++)
            {
                //
                // Calculate the corresponding row number of the background image.
                // Skip the row if it is outside of the background image.
                //
                int destRow = (int)location.y + row;
                if (destRow < 0 || destRow >= background.rows()) continue;
                //
                // For each column of the overlay image.
                //
                for (int col = 0; col < overlay.cols(); col++)
                {
                    //
                    // Calculate the corresponding column number of background image.
                    // Skip the column if it is outside of the background image.
                    //
                    int destCol = (int)location.x + col;
                    if (destCol < 0 || destCol >= background.cols()) continue;
                    //
                    // Get the source pixel from the overlay image and the destination pixel from the
                    // background image. Calculate the opacity as a percentage.
                    //
                    double[] srcPixel = overlay.get(row,  col);
                    double[] destPixel = background.get(destRow, destCol);
                    double opacity = srcPixel[3]/255.0;
                    //
                    // Merge the source pixel to the destination pixel with the proper opacity.
                    //
                    for (int channel = 0; channel < background.channels(); channel++)
                    {
                        destPixel[channel] = destPixel[channel]*(1.0 - opacity) + srcPixel[channel]*opacity;
                    }
                    //
                    // Put the resulting pixel into the background image.
                    //
                    background.put(destRow, destCol, destPixel);
                }
            }
        }
        else
        {
            throw new RuntimeException("Invalid image format.");
        }
    }    //overlayImage

    /**
     * This method converts an OpenCV image (i.e. Mat) into a BufferedImage that can be drawn on
     * a Java graphics object.
     *
     * @param mat specifies an OpenCV image.
     * @return converted BufferedImage object.
     */
    public BufferedImage MatToBufferedImage(Mat mat)
    {
        BufferedImage image = new BufferedImage(mat.width(), mat.height(), BufferedImage.TYPE_3BYTE_BGR);
        WritableRaster raster = image.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte)raster.getDataBuffer();
        byte[] data = dataBuffer.getData();
        mat.get(0, 0, data);

        return image;
    }   //MatToBufferedImage

}   //class FaceDetector
