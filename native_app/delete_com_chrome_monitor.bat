:: Deletes the entry created by add_com_chrome_monitor.bat
REG DELETE "HKCU\Software\Google\Chrome\NativeMessagingHosts\com.chrome.monitor" /f
REG DELETE "HKLM\Software\Google\Chrome\NativeMessagingHosts\com.chrome.monitor" /f