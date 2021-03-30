# Native App and Native Messaging README
## Current Functionality
There will be a couple-second-delay when you launch the Native App, due to initial setup within the app itself. The Native App currently runs for 60 loops and then stops. Open Hardware Monitor needs to be running for the program to work properly, but the program will still run without errors if Open Hardware Monitor is not downloaded or running.

## Setting up the Native App and Native Messaging
### Native App Setup
1. If you haven't already, download [Open Hardware Monitor](https://openhardwaremonitor.org/).
3. Create a Java project using the files in monitor_src. 
4. Download [GSON 2.6.2](https://search.maven.org/artifact/com.google.code.gson/gson/2.6.2/jar) and configure the project build path to include the gson-2.6.2.jar file.
5. Check that the Java project runs as expected.
6. Export the Java project to a runnable jar file named "monitor.jar" inside the "native_app" folder on your machine.
7. Check that "monitor.jar" runs as expected using "java -jar monitor.jar" from the directory it is in.

### Native Messaging Setup
The way it native message works is the json file calls the wrapper.bat, in which it calls the runable jar file. You need to export your java project to a runable jar file to replace this runable jar file to test your code.
.
.
.
