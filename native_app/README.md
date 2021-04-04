# Native App and Native Messaging README
## Current Functionality
### Summary:
When you launch the Native App, there will be a couple-second-delay before you see any monitoring due to initial setup within the app. Currently, the Native App runs for 60 loops and then stops. Open Hardware Monitor needs to be running for the program to work properly, but the program will still run without errors if Open Hardware Monitor is not downloaded or running. By default, Native Messaging is enabled and Server Messaging is disabled. See the run options below for how to change this.

### Run Options:
In run_native.bat you can use the following options to run the program in different modes.
```
java -jar "monitor.jar" [OPTIONS]
```
```
-records int            Changes the number of records recorded (the number of times the 
                        monitor will loop) to an integer number of your choice. Each loop
                        should take ~2-3 seconds. Without this option, the default number
                        of records recorded is 60.
                        
-native [on|off]        Turn native messaging output and input on or off. It is ON by default.
-nativeOutput [on|off]  Turn native messaging output on or off. It is ON by default.
-nativeInput [on|off]   Turn native messaging input on or off. It is ON by default.


-server [on|off]        Turn server messaging on or off. It is OFF by default.

-logNative              Ouput the messages sent back and forth in native messaging to a 
                        NativeMessageLog txt file.

-logServer              Ouput the messages sent back and forth in server messaging to a 
                        ServerMessageLog txt file.

-clean             Deletes any output files created by previous runs then exits.
```

## Setting up the Native App and Native Messaging
### Native App Setup
1. If you haven't already, download [Open Hardware Monitor](https://openhardwaremonitor.org/).
2. Download "monitor.jar" from [Google Drive](https://drive.google.com/file/d/1td2oqR6QKf5A3exWgvhPubAaLloNgFoU/view?usp=sharing); or Create a Java project using the files in monitor_src. 
3. If creating a Java project:
    * Download [GSON 2.6.2](https://search.maven.org/artifact/com.google.code.gson/gson/2.6.2/jar) and configure the project build path to include the gson-2.6.2.jar file.
    * Check that the Java project runs as expected.
    * Export the Java project to a runnable jar file named "monitor.jar" inside the "native_app" folder on your machine.
    * Check that "monitor.jar" runs as expected using "java -jar monitor.jar" from the directory it is in.

### Native Messaging Setup
1. Edit native.json so that "chrome-extension://paste_your_extension_ID_here/" contains your extension ID (e.g. "chrome-extension://lconabdfkpmfliemkbbjccjkojgcoapm/").
2. Run add_com_chrome_monitor.bat to add the com.chrome.monitor registry key; or update the registry manually using Registry Editor. This will allow the Chrome extension pass messages back and forth with the Native Application.
3. Check that "run_native.bat" launches the program as expected.
