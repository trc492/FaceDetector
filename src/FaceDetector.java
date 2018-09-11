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
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
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
 * - Detect faces with OpenCV using the FrontalFace CascadeClassifier.
 * - Scale an image to a different size.
 * - Overlay an image onto another image with transparency.
 * - Translate between the OpenCV Mat image format and the BufferedImage format for display.
 */
public class FaceDetector extends JPanel implements WindowListener
{
    private static final long serialVersionUID = 1L;
    private static final String programTitle = "OpenCV Face Detector";
    private static final String classifierPath = "cascade-files/haarcascade_frontalface_alt.xml";
    private static final String overlayImagePath = "images/Mustache.png";
    private static final boolean perfCheckEnabled = false;

    private VideoCapture camera;
    private Mat image;
    private MatOfRect faceRects;
    private Mat overlayImage;
    private CascadeClassifier faceDetector;
    private long totalProcessingTime = 0;
    private long framesProcessed = 0;
    private boolean doOverlayImage = true;
    private boolean overlayRectangle = false;
    private boolean overlayCircle = false;
    private RefreshThread cameraThread;

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
                        //
                        // No layout, non-resizable, auto location at center.
                        //
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
        frame.addWindowListener(this);
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
            throw new RuntimeException("Failed to open camera.");
        }
        //
        // Preallocate some global variables.
        //
        image = new Mat();
        faceRects = new MatOfRect();
        //
        // Load the overlay image and preserve the alpha channel (i.e. transparency).
        //
        overlayImage = Highgui.imread(overlayImagePath, -1);
        //
        // Cannot do overlay unless the overlay image has all the rgba channels.
        //
        if (overlayImage.channels() < 4) doOverlayImage = false;
        //
        // Load the Frontal Face cascade classifier as the face detector.
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
        //
        // Cannot do overlay unless the camera image has the rgb channels (i.e. color).
        //
        if (image.channels() < 3) doOverlayImage = false;
        frame.setSize(image.width(), image.height() + 35);
        //
        // Create the Refresh thread to refresh the video pane at 10fps (i.e. every 100 msec).
        //
        cameraThread = new RefreshThread(this, 100);
        cameraThread.start();
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
        long startTime = System.currentTimeMillis();
        camera.read(image);
        faceDetector.detectMultiScale(image, faceRects);
        long elapsedTime = System.currentTimeMillis() - startTime;

        if (perfCheckEnabled)
        {
            totalProcessingTime += elapsedTime;
            framesProcessed++;
            if (framesProcessed%10 == 0)
            {
                System.out.printf("Average Processing Time = %d\n", totalProcessingTime/framesProcessed);
            }
        }

        //
        // We may want to overlay a circle or rectangle on each detected faces or
        // we can overlay a fun image onto a detected face. Play with the code in
        // this for-loop and let your imagination run wild.
        //
        Rect[] rects = faceRects.toArray();
        int maxArea = 0;
        int maxIndex = -1;
        for (int i = 0; i < rects.length; i++)
        {
            //
            // Draw a circle around the detected face.
            //
            if (overlayCircle)
            {
                Core.ellipse(
                        image,
                        new RotatedRect(
                                new Point(rects[i].x + rects[i].width/2, rects[i].y + rects[i].height/2),
                                rects[i].size(),
                                0.0),
                        new Scalar(0, 255, 0));
            }
            //
            // Draw a rectangle around the detected face.
            //
            if (overlayRectangle)
            {
                Core.rectangle(
                        image,
                        new Point(rects[i].x, rects[i].y),
                        new Point(rects[i].x + rects[i].width, rects[i].y + rects[i].height),
                        new Scalar(0, 255, 0));
            }
            //
            // Find the largest detected face.
            //
            if (doOverlayImage)
            {
                int area = rects[i].width * rects[i].height;
                if (area > maxArea)
                {
                    maxArea = area;
                    maxIndex = i;
                }
            }
        }

        //
        // Only overlay fun image to the largest detected face.
        //
        if (doOverlayImage && maxIndex != -1)
        {
            //
            // Scale the fun image to the same size as the face.
            //
            Mat scaledOverlay = new Mat();
            Imgproc.resize(overlayImage, scaledOverlay, rects[maxIndex].size());
            //
            // Overlay the scaled image to the camera image.
            //
//            combineImage(image, scaledOverlay, rects[maxIndex].x, rects[maxIndex].y - rects[maxIndex].height);
            combineImage(image, scaledOverlay, rects[maxIndex].x, rects[maxIndex].y);
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
     * @param locX specifies the X location on the background image where the upper left corner of the overlay
     *        image should be at
     * @param locY specifies the Y location on the backgorund image where the upper left corner of the overlay
     *        image should be at.
     */
    private void combineImage(Mat background, Mat overlay, int locX, int locY)
    {
        //
        // Make sure the background image has 3 channels and the overlay image has 4 channels.
        //
        if (background.channels() >= 3 && overlay.channels() >= 4)
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
                int destRow = locY + row;
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
                    int destCol = locX + col;
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
                    // Each color pixel consists of 3 channels: BGR (Blue, Green, Red).
                    // The fourth channel is opacity and is only applicable for the overlay image.
                    //
                    for (int channel = 0; channel < 3; channel++)
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
            throw new RuntimeException(
                "Invalid image format (src=" + overlay.channels() + ",dst=" + background.channels() + ").");
        }
    }    //combineImage

    /**
     * This method converts an OpenCV image (i.e. Mat) into a BufferedImage that can be drawn on
     * a Java graphics object.
     *
     * @param mat specifies an OpenCV image.
     * @return converted BufferedImage object.
     */
    private BufferedImage MatToBufferedImage(Mat mat)
    {
        BufferedImage image = new BufferedImage(mat.width(), mat.height(), BufferedImage.TYPE_3BYTE_BGR);
        WritableRaster raster = image.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte)raster.getDataBuffer();
        byte[] data = dataBuffer.getData();
        mat.get(0, 0, data);

        return image;
    }   //MatToBufferedImage

    //
    // Implements WindowListener interface.
    //

    @Override
    public void windowActivated(WindowEvent e)
    {
    }   //windowActivated

    @Override
    public void windowClosed(WindowEvent e)
    {
    }   //windowClosed

    /**
     * This method is called when the "X" Window Close button is clicked. This will exit the program. It makes sure
     * the program shuts down properly by terminating the camera thread releasing the camera.
     */
    @Override
    public void windowClosing(WindowEvent e)
    {
        cameraThread.terminate();
        camera.release();
    }   //windowClosing

    @Override
    public void windowDeactivated(WindowEvent e)
    {
    }   //windowDeactivated

    @Override
    public void windowDeiconified(WindowEvent e)
    {
    }   //windowDeiconified

    @Override
    public void windowIconified(WindowEvent e)
    {
    }   //windowIconified

    @Override
    public void windowOpened(WindowEvent e)
    {
    }   //windowOpened

}   //class FaceDetector
