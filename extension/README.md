# Chrome Extension README
## Current Functionality
### Summary:
This extension displays the resource usage by chrome when you are using chrome. It get its data
from the native app and display the live data along with the other statics, including the number
of tabs user opened and the total carbon footprint by chrome (estimated).

### How to use the extension
1. Make sure have already set up the native app [Native app](https://github.com/rbouldin/Chrome-Power-and-Resource-Monitor/tree/main/native_app).
2. Run the Open Hardware Monitor [Open Hardware Monitor](https://openhardwaremonitor.org/).
3. In google chrome open more tools -> extension. Click on the "Load unpacked" to load this folder "entension/"
4. Copy the ID of loaded extension to the file native_app/native.json to replace the id in the "allowed_origins" field.
5. You can now Click on the extension to see your chrome resource usage. (You can pin the extension to make it easier to find).