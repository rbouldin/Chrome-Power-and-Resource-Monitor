# Native App and Native Messaging README
## Current Functionality
The Native App currently runs for 60 loops and then stops.

## Setting up the Native App and Native Messaging
### Native App Setup
1. If you haven't already, download Open Hardware Monitor from https://openhardwaremonitor.org/.
2. Download GSON 2.6.2 from: https://search.maven.org/artifact/com.google.code.gson/gson/2.6.2/jar.
3. Create a Java project using the files in monitor_src.
4. Configure the project build path to include the gson-2.6.2.jar file.
5. Check that the Java project runs as expected (if you haven't already, launch Open Hardware Monitor before you run).
6. Export the Java project to a .jar file named "monitor.jar" inside the "native_app" folder on your machine. If using Eclipse Do: "File > Export > Java/Runnable Jar File > Next". Select "Browse" and find the native_app folder on your machine. Change the jar file name to "monitor.jar" and click "Finish."
7. Check that "monitor.jar" runs as expected using "java -jar monitor.jar" from the directory it is in (if you haven't already, launch Open Hardware Monitor before you execute the command).

### Native Messaging Setup
The way it native message works is the json file calls the wrapper.bat, in which it calls the runable jar file. You need to export your java project to a runable jar file to replace this runable jar file to test your code.
.
.
.
