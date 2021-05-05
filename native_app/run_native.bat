:: Launch monitor.jar to communicate with the Chrome extension through Native Messaging.
:: Check whether Open Hardware Monitor is running first and alert the native app.
@echo off
tasklist /FI "IMAGENAME eq OpenHardwareMonitor.exe" 2>NUL | find /I /N "OpenHardwareMonitor.exe">NUL
if NOT "%ERRORLEVEL%"=="0" (
    java -jar "monitor.jar" -OHM off %*
) else (
    java -jar "monitor.jar" -OHM on %*
)
