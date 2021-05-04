/** options.js
 * 
 *  VERSION: 2021.05.01
 *  AUTHORS: Rae Bouldin
 * 
 *  Written for Dr. Cameron's Systems & Networking Capstone at Virginia Tech. 
 */

// ------------------------------------------------------------------------- //
//                        Set UI Data in options.html                        //
// ------------------------------------------------------------------------- //

document.addEventListener('DOMContentLoaded', async function() {

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
    } else {
      document.getElementById('checkMemLeak').checked = false;
    }
    //
    const clearBrowsingData = result.suggestion_clearBrowsingData;
    if (clearBrowsingData) {
      document.getElementById('clearBrowsingData').checked = true;
    } else {
      document.getElementById('clearBrowsingData').checked = false;
    }
    //
    const disableHardwareAcceleration = result.suggestion_disableHardwareAcceleration;
    if (disableHardwareAcceleration) {
      document.getElementById('disableHardwareAcceleration').checked = true;
    } else {
      document.getElementById('disableHardwareAcceleration').checked = false;
    }
    //
    const manageExtensions = result.suggestion_manageExtensions;
    if (manageExtensions) {
      document.getElementById('manageExtensions').checked = true;
    } else {
      document.getElementById('manageExtensions').checked = false;
    }
    //
    const useAdBlocker = result.suggestion_useAdBlocker;
    if (useAdBlocker) {
      document.getElementById('useAdBlocker').checked = true;
    } else {
      document.getElementById('useAdBlocker').checked = false;
    }
    //
    const useTabDiscarder = result.suggestion_useTabDiscarder;
    if (useTabDiscarder) {
      document.getElementById('useTabDiscarder').checked = true;
    } else {
      document.getElementById('useTabDiscarder').checked = false;
    }
    //
    const useTabSuspender = result.suggestion_useTabSuspender;
    if (useTabSuspender) {
      document.getElementById('useTabSuspender').checked = true;
    } else {
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

  // Set save button action
  const saveButton = document.getElementById('save');
  saveButton.addEventListener('click', saveOptions);

});
// ------------------------------------------------------------------------- //



// ------------------------------------------------------------------------- //
//                            Save Button Action                             //
// ------------------------------------------------------------------------- //

function saveOptions() {

  // Save enabled suggestions
  const checkMemLeakChecked = document.getElementById('checkMemLeak').checked;
  const clearBrowsingDataChecked = document.getElementById('clearBrowsingData').checked;
  const disableHardwareAccelerationChecked = document.getElementById('disableHardwareAcceleration').checked;
  const manageExtensionsChecked = document.getElementById('manageExtensions').checked;
  const useAdBlockerChecked = document.getElementById('useAdBlocker').checked;
  const useTabDiscarderChecked = document.getElementById('useTabDiscarder').checked;
  const useTabSuspenderChecked = document.getElementById('useTabSuspender').checked;
  chrome.storage.sync.set({ suggestion_checkMemLeak: checkMemLeakChecked }, function () {
    if (checkMemLeakChecked) {
      console.log("checkMemLeakChecked = " + checkMemLeakChecked);
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
// ------------------------------------------------------------------------- //