# Native App and Native Messaging README
## Summary
The native monitoring app is able to monitor the resource/power usage of Chrome and the system as a whole and provides an interface between the server and extension. Open Hardware Monitor needs to be running for the program to work properly, but the program will still run without errors if Open Hardware Monitor is not downloaded or running. The native app can attempt to start Open Hardware Monitor if prompted. By default, Native Messaging is enabled and Server Messaging is disabled. See the run options below for how to change this.

## Setting up the Native App and Native Messaging
### Native Messaging Setup
1. Edit native.json so that `"chrome-extension://paste_your_extension_ID_here/"` contains your extension ID (e.g. `"chrome-extension://lconabdfkpmfliemkbbjccjkojgcoapm/"`).
2. Run add_com_chrome_monitor.bat to add the com.chrome.monitor registry key; or update the registry manually using Registry Editor. This will allow the Chrome extension pass messages back and forth with the Native Application.

### Run Options
In run_native.bat the following options can be used to run the program in different modes.
```
java -jar "monitor.jar" [OPTIONS] %*
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

-native [on|off]        Turn native messaging on or off. It is ON by default.

-server [on|off]        Turn server messaging on or off. It is OFF by default.
```