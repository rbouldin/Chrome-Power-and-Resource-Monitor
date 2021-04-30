 
  function saveOptions() {
    // Save user info options
    // retrieve from html element by id (userName)
    const newUserName = document.getElementById('userNameInput').value;
    if (newUserName) {
      // Check if the name is already taken on the server.
      // var nameTaken = false;
      // var message = "";
      // chrome.runtime.sendNativeMessage(
      //   'com.chrome.monitor',
      //   { "message": "GET user exists", "user": "" + newUserName },
      //   function(response) {
      //     message = JSON.stringify(response);
      //     console.log("Received " + JSON.stringify(response));
      //   });
      // chrome.runtime.sendNativeMessage(
      //   'com.chrome.monitor',
      //   { "message": "EXIT NATIVE" },
      //   function(response) {
      //     console.log("Received " + JSON.stringify(response));
      //   });
      // nameTaken = message.includes('true');
      var nameTaken = userExists(newUserName);
      console.log("nameTaken = " + nameTaken);
      if (!nameTaken) {
        // Save the value using the Chrome extension storage API.
        chrome.storage.sync.set({ userName: newUserName }, function () {
          console.log("newUserName = " + newUserName);
        });
      }
    }

    // Save enabled suggestions
    const checkMemLeakChecked = document.getElementById('checkMemLeak').checked;
    const clearBrowsingDataChecked = document.getElementById('clearBrowsingData').checked;
    const disableHardwareAccelerationChecked = document.getElementById('disableHardwareAcceleration').checked;
    const manageExtensionsChecked = document.getElementById('manageExtensions').checked;
    const useAdBlockerChecked = document.getElementById('useAdBlocker').checked;
    const useTabDiscarderChecked = document.getElementById('useTabDiscarder').checked;
    const useTabSuspenderChecked = document.getElementById('useTabSuspender').checked;
    chrome.storage.sync.set({ suggestion_checkMemLeak: checkMemLeakChecked }, function () {
      if (serverChecked) {
        console.log("serverChecked = " + serverChecked);
      }
    });
    chrome.storage.sync.set({ suggestion_clearBrowsingData: clearBrowsingDataChecked }, function () {
      if (clearBrowsingDataChecked) {
        console.log("clearBrowsingDataChecked = " + clearBrowsingDataChecked);
      }
    });
    chrome.storage.sync.set({ suggestion_disableHardwareAcceleration: disableHardwareAccelerationChecked }, function () {
      if (disableHardwareAccelerationChecked) {
        console.log("disableHardwareAccelerationChecked = " + disableHardwareAccelerationChecked);
      }
    });
    chrome.storage.sync.set({ suggestion_manageExtensions: manageExtensionsChecked }, function () {
      if (manageExtensionsChecked) {
        console.log("manageExtensionsChecked = " + manageExtensionsChecked);
      }
    });
    chrome.storage.sync.set({ suggestion_useAdBlocker: useAdBlockerChecked }, function () {
      if (useAdBlockerChecked) {
        console.log("useAdBlockerChecked = " + useAdBlockerChecked);
      }
    });
    chrome.storage.sync.set({ suggestion_useTabDiscarder: useTabDiscarderChecked }, function () {
      if (useTabDiscarderChecked) {
        console.log("useTabDiscarderChecked = " + useTabDiscarderChecked);
      }
    });
    chrome.storage.sync.set({ suggestion_useTabSuspender: useTabSuspenderChecked }, function () {
      if (useTabSuspenderChecked) {
        console.log("useTabSuspenderChecked = " + useTabSuspenderChecked);
      }
    });
    
    // Save run options
    const serverChecked = document.getElementById('serverCheckbox').checked;
    const loggingChecked = document.getElementById('loggingCheckbox').checked;
    chrome.storage.sync.set({ serverEnabled: serverChecked }, function () {
      if (serverChecked) {
        console.log("serverChecked = " + serverChecked);
      }
    });
    chrome.storage.sync.set({ loggingEnabled: loggingChecked }, function () {
      if (loggingChecked) {
        console.log("loggingChecked = " + loggingChecked);
      }
    });

    window.location.reload(true);
    alert('Options saved!');
  }
  
  // Set user info on the UI
  document.addEventListener('DOMContentLoaded', async function() {
    // Set user info on the UI
    chrome.storage.sync.get(['userName'], function(result) {
      const name = result.userName;
      if (name) {
        document.getElementById('userInfo').innerHTML = "You are logged in as " + name + ".";
      }
      else {
        document.getElementById('userInfo').innerHTML = "You not logged in.";
      }
    });

    // Set suggestions enabled on the UI
    chrome.storage.sync.get(['suggestion_checkMemLeak', 
                             'suggestion_clearBrowsingData', 
                             'suggestion_disableHardwareAcceleration', 
                             'suggestion_manageExtensions', 
                             'suggestion_useAdBlocker', 
                             'suggestion_useTabDiscarder', 
                             'suggestion_useTabSuspender'], function(result) {
      //
      const checkMemLeak = result.suggestion_checkMemLeak;
      if (checkMemLeak) {
        document.getElementById('checkMemLeak').checked = true;
      }
      else {
        document.getElementById('checkMemLeak').checked = false;
      }
      //
      const clearBrowsingData = result.suggestion_clearBrowsingData;
      if (clearBrowsingData) {
        document.getElementById('clearBrowsingData').checked = true;
      }
      else {
        document.getElementById('clearBrowsingData').checked = false;
      }
      //
      const disableHardwareAcceleration = result.suggestion_disableHardwareAcceleration;
      if (disableHardwareAcceleration) {
        document.getElementById('disableHardwareAcceleration').checked = true;
      }
      else {
        document.getElementById('disableHardwareAcceleration').checked = false;
      }
      //
      const manageExtensions = result.suggestion_manageExtensions;
      if (manageExtensions) {
        document.getElementById('manageExtensions').checked = true;
      }
      else {
        document.getElementById('manageExtensions').checked = false;
      }
      //
      const useAdBlocker = result.suggestion_useAdBlocker;
      if (useAdBlocker) {
        document.getElementById('useAdBlocker').checked = true;
      }
      else {
        document.getElementById('useAdBlocker').checked = false;
      }
      //
      const useTabDiscarder = result.suggestion_useTabDiscarder;
      if (useTabDiscarder) {
        document.getElementById('useTabDiscarder').checked = true;
      }
      else {
        document.getElementById('useTabDiscarder').checked = false;
      }
      //
      const useTabSuspender = result.suggestion_useTabSuspender;
      if (useTabSuspender) {
        document.getElementById('useTabSuspender').checked = true;
      }
      else {
        document.getElementById('useTabSuspender').checked = false;
      }
    });

    // Set run options on the UI
    chrome.storage.sync.get(['serverEnabled', 'loggingEnabled'], function(result) {
      // Set server checkbox on the UI
      const serverEnabled = result.serverEnabled;
      if (serverEnabled) {
        document.getElementById('serverCheckbox').checked = true;
      }
      else {
        document.getElementById('serverCheckbox').checked = false;
      }
      // Set logging checkbox on the UI
      const loggingEnabled = result.loggingEnabled;
      if (loggingEnabled) {
        document.getElementById('loggingCheckbox').checked = true; 
      }
      else {
        document.getElementById('loggingCheckbox').checked = false;
      }
    });

    const saveButton = document.getElementById('save');
    saveButton.addEventListener('click', saveOptions);

   });



// ------------------------------------------------------------------------- //
//                              NATIVE MESSAGING                             //
// ------------------------------------------------------------------------- //
var connected = false;
var lastMessage = "err";

function userExists(name) {
  var gotMsgFromNative = false;
  var exists = true;
  var hostName = "com.chrome.monitor";
  var getMsg = {"message": "GET user exists", "user": "" + name};
  var exitMsg = {"message": "EXIT NATIVE"};

  // chrome.runtime.sendNativeMessage(hostName, getMsg, function(response) {
  //   console.log("made it here");
  //   console.log("sent: " + getMsg);
  //   if (chrome.runtime.lastError) {
  //       alert("ERROR: " + chrome.runtime.lastError.message);
  //   } else {
  //     console.log("got: " + response);
  //     exists = (response.userExists == 'true');
  //     chrome.runtime.sendNativeMessage(hostName, exitMsg);
  //     console.log("sent: " + exitMsg);
  //   }
  // });

  if (!connected) {
    connectNative();
  }
  if (connected) {
    sendNativeMessage(getMsg);
    if (lastMessage.includes("err")) {
      console.log("something went wrong");
    }
    sendNativeMessage(exitMsg);
  } else {
    console.log("Couldn't connect to native.");
    alert("Error connecting to the server. Your username couldn't be updated.");
  }
  return exists;
}

function sendNativeMessage(message) {
  port.postMessage(message);
  console.log("Sent native message: " + JSON.stringify(message));
}

function onNativeMessage(message) {
  console.log("Received native message: " + message);
  var lastMessage = "" + message.userExists;
  console.log("lastMessage = " + lastMessage);
}

function onNativeDisconnect() {
  connected = false;
  console.log(chrome.runtime.lastError.message);
}

function connectNative() {
  var hostName = "com.chrome.monitor";
  console.log("Connecting to native messaging host: " + hostName);
  port = chrome.runtime.connectNative(hostName);
  port.onMessage.addListener(onNativeMessage);
  port.onDisconnect.addListener(onNativeDisconnect);
  connected = true;
}
// ------------------------------------------------------------------------- //