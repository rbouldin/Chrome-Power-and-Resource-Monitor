:: Check if Open Hardware Monitoring is running, and start an instance it it is not running.
@echo off
tasklist /FI "IMAGENAME eq OpenHardwareMonitor.exe" 2>NUL | find /I /N "OpenHardwareMonitor.exe">NUL
if NOT "%ERRORLEVEL%"=="0" "OpenHardwareMonitor/OpenHardwareMonitor.exe"
exit