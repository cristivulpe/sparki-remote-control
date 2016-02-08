# Android Remote Control for Arbotics Sparki

## Disclaimer
The source code and other deliverables available on this page are designed just for educational purposes. They come with no warranties and they can be used "as-are" under the permissions of the attached license (Apache License Version 2.0).

## Purpose
The Android Remote Control for Arbotics Sparki is yet another example of how one can control a robot from a mobile phone using bluetooth. The example has been build for a group of children participating at a technology club in the Swedesboro-Woolwich school district (hence the package names) but since the intention for this was purely instructive/educational I eventually decided to share the code.

The code consists of two main elements:

1.  An [Arduino](https://www.arduino.cc/en/Reference/HomePage) application that can be installed on the ["Sparki robot"](http://arcbotics.com/products/sparki/). 
2.  An [Android](https://www.android.com/) application that one can run on their mobile phone, assuming the phone supports the Android os.

For convenience, the source code is distributed as a single bundle.

## User's guide
In order to be able to use the application, one needs to:

1.  Fetch the source code from GitHub (the android app can be installed directly from [Google Play Store] (https://play.google.com/store/apps/details?id=com.swedesboro_woolwich.remotecontrol)).
2.  Build and deploy the source code (please see next section for details on how to do that).
3.  Once both the android app and the arduino code are installed on the respective devices, pair your phone with the robot.

The application provides a menu for connecting/disconnecting from robot's bluetooth (connection works only if the name of the device is 'ArcBotics').

Once the two devices are paired, the user can perform the following:

1.  Control the movement from the 'Movement control' section (buttons are available for left rotation, right rotation, moving forward, moving backward and stopping the robot).
2.  Control the gripper (closing, opening and stopping the gripper) using the buttons in 'Gripper control' section.
3.  Display the light levels detected by the light sensors (they are displayed as gray scale using three buttons). Reading the sensor data can be controlled using a 'pause' button present in the 'Light sensors' section.
4.  Measure the distance from the closest object and rotate the head of the ultrasonic range sensor (see section  called 'Distance').

The application also provides an 'Emergency shutdown' button that stops all the robot's activities.

Also, a set of vocal commands are supported, as follows (note that during the test phase, it turned out that the user experience of using the vocal commands was not that great):

| Vocal Command | Action         |
|---------------|----------------|
|forward        |Move forward    |
|backward       |Move backward   |
|left           |Rotate left     |
|right          |Rotate right    |
|close          |Close gripper   |
|open           |Open gripper    |
|stop           |Stop all actions|

Saying 'cancel' will stop the voice command listening.

## Building the code
### Building the Arduino code
Note that the development and testing have been done using a Windows 7 machine.

1.  Download and install SparkiDuino following the instructions available on [ArcBotics](http://arcbotics.com/lessons/sparkiduino-windows-install-guide/)' site.
2.  Import the arduino/bluetooth.ino file.
3.  Compile and upload your code to the robot (this assumes that the USB connectivity to your robot works).

### Building the Android code
The instructions are specific to a Linux operating system ([Ubuntu 14.04](http://www.ubuntu.com/download/desktop) has been used in this case). The development had been done using [Android Studio](http://developer.android.com/sdk/index.html).

*  Set the path to the JAVA_HOME:
```bash
export JAVA_HOME=<your JDK path>
```
*  Set the path to the ANDROID_HOME:
```bash
export ANDROID_HOME=<the path to the andorid SDK>
```
*  Open a bash prompt and execute this command:
```bash
./gradlew clean build
```

This will produce the APK that can be installed on your Android phone. The app is also avaialble on [Google Play Store] (https://play.google.com/store/apps/details?id=com.swedesboro_woolwich.remotecontrol).

## Compatibility
This application has been tested using Google Nexus 6 and Google Nexus 5 running Android Marshmallow. The arduino code has been deployed and tested using an Arduino Sparki robot.
