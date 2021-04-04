::
:: Run Options: java -jar "monitor.jar" [OPTIONS]"
::
:: OPTIONS:
::
::   -records int            An integer number of records you'd like to record (the number of 
::                           times the monitor will loop). Each loop should take ~2-3 seconds. 
::                           This is 60 by default.
::
::   -native [on|off]        Turn native messaging output & input on or off. It is ON by default.
::   -nativeOutput [on|off]  Turn native messaging output on or off. It is ON by default.
::   -nativeInput [on|off]   Turn native messaging input on or off. It is ON by default.
::
::   -server [on|off]        Turn server messaging on or off. It is OFF by default.
::
::   -logNative              Ouput the messages sent back and forth in native messaging to a 
::                           NativeMessageLog txt file.
::
::   -logServer              Ouput the messages sent back and forth in server messaging to a 
::                           ServerMessageLog txt file.
::
::   -clean                  Deletes any output files created by previous runs then exits.
::
@echo off
java -jar "monitor.jar" -records 60 -nativeOutput on -nativeInput off %*