# FaceDetector
###Experimenting face detection with OpenCV
This program experiments with Face Detection using OpenCV. It contains example code to:
* Capture video with the default camera.
* Detect faces with OpenCV using the FrontalFace CascadeSpecifier.
* Scale an image to a different size.
* Overlay an image onto another image with transparency.
* Translate between the OpenCV Mat image format and the BufferedImage format for display.

####Notes on building this program using Eclipse

Unless you have previously added OpenCV library to Eclipse, you may encounter errors trying
to build this program. To make it easier for folks who are beginners to OpenCV, all the
necessary library files are included in this project under the `OpenCV-2_4_13` folder. But
you still need to configure Eclipse to add OpenCV as a User Library. Assuming you have already
started Eclipse and imported this project into your workspace, click the `Window->Preferences`
menu. On the left pane, click open `Java->Build Path->User Libraries`. If you see OpenCV on the
right pane, you have already added OpenCV library previously. If not, click the `New` button and
type `OpenCV-2.4.13` as the User library name, then click `OK`. While having the new library
highlighted, click the `Add External JARs` button. Navigate to your `<workspace>/FaceDetector/OpenCV-2_4_13`
folder and select the `opencv-2412.jar` file and click the `Open` button. Now expand the new library
`OpenCV-2.4.13->opencv-2413.jar - FaceDetector/OpenCV-2_4_13->Native library location: (None)`, then
click the `Edit` button. Click the `External folder` button and navigate to the
`<workspace>/FaceDetector/OpenCV-2_4_13/x64` folder (or x86 if you have a 32-bit system) and click OK
buttons to exit all the dialogs.

Next, you need to add the OpenCV user library to this project. On the left pane (Project Explorer),
right click the `FaceDetector` project, click `Build Path->Add Libraries`, highlight `User Library`
then click `Next`. You will see the checkbox `OpenCV-2.4.13`, check it and click `Finish`.

Now you should be able to build the project without error.