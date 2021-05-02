# Native App and Native Messaging README
## Summary
The native monitoring app is able to monitor the resource/power usage of Chrome and the system as a whole and provides an interface between the server and extension. Open Hardware Monitor needs to be running for the program to work properly, but the program will still run without errors if Open Hardware Monitor is not downloaded or running. By default, Native Messaging is enabled and Server Messaging is disabled. See the run options below for how to change this.

## Setting up the Native App and Native Messaging
### Native App Setup
1. If you haven't already, download [Open Hardware Monitor](https://openhardwaremonitor.org/).
2. Download "monitor.jar" from [Google Drive](https://drive.google.com/file/d/1td2oqR6QKf5A3exWgvhPubAaLloNgFoU/view?usp=sharing); or Create a Java project using the files in monitor_src. 
3. If creating a Java project:
    * Configure the project build path to include the files in native_app/lib/.
    * Export the Java project to a runnable jar file named "monitor.jar" inside the "native_app" folder on your machine.

### Native Messaging Setup
1. Edit native.json so that "chrome-extension://paste_your_extension_ID_here/" contains your extension ID (e.g. "chrome-extension://lconabdfkpmfliemkbbjccjkojgcoapm/").
2. Run add_com_chrome_monitor.bat to add the com.chrome.monitor registry key; or update the registry manually using Registry Editor. This will allow the Chrome extension pass messages back and forth with the Native Application.
3. Check that "run_native.bat" launches the program as expected.

### Run Options
In run_native.bat you can use the following options to run the program in different modes.
```
java -jar "monitor.jar" [OPTIONS]
```
```
OPTIONS:

-log                    Output program actions (including errors, native 
                        messaging, and server messaging) to a file named 
                        LOG-<SessionID>.txt

-logNative              Ouput the messages sent back and forth in native 
                        messaging to a file named LOG-<SessionID>-NativeMessage.txt

-logServer              Ouput the messages sent back and forth in server 
                        messaging to a file named LOG-<SessionID>-Server.txt

-native [on|off]        Turn native messaging output & input on or off. 
                        It is ON by default.

-nativeInput [on|off]   Turn native messaging input on or off. 
                        It is ON by default.

-nativeOutput [on|off]  Turn native messaging output on or off. 
                        It is ON by default.

-server [on|off]        Turn server messaging on or off. 
                        It is OFF by default.

-records int            An integer number of records you'd like to record (the 
                        number of times the monitor will loop if native input 
                        is off). This is 60 by default.
```
