# Chrome Extension README
## Summary
This extension displays the Chrome web browser's power and resource usage. It gets system and monitoring data from the native app and displays live data along with other statistics, including the number of tabs open and the total carbon footprint by Chrome (estimated).

## Setting up the Extension
1. In Google Chrome, open Toolbar Settings -> More Tools -> Extension. 
2. Enable Developer Mode, and click on "Load unpacked" to load the extension from this directory (i.e. `../Chrome-Power-and-Resource-Monitor/extension/`) on your local machine.
3. Copy the ID of your loaded extension and go read the Setup steps in [native_app/README.md](https://github.com/rbouldin/Chrome-Power-and-Resource-Monitor/tree/main/native_app) to enable native messaging for your extension.
4. You can now start the extension to see your browser's power and resource usage. Pin the extension to the toolbar to make it easier to find.
