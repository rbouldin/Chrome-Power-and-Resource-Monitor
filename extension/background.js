//global variables (settings)
var ACTIVE_MONITOR_INTERVAL = 1;
var BG_MONITOR_INTERVAL = 10;
var CARBON_INTERVAL = 20;

var newUser = true;
//counters
var count = 0;
var totalCO2 = 0;
var avgPower = 5;
var avgPowerLoggedTime = 0;


//native data query
//=======================================================

//unique user id --------------------
var getRandomToken = function () {
    var randomPool = new Uint8Array(4);
    crypto.getRandomValues(randomPool);
    var hex = '';
    for (var i = 0; i < randomPool.length; ++i) {
        hex += randomPool[i].toString(16);
    }
    return hex;
}

var userIdFunc = function (varFunc) {
    chrome.storage.sync.get('userId', function (items) {
        var userId = items.userId;
        if (userId) {
            varFunc(userId);
        } else {
            userId = getRandomToken();
            chrome.storage.sync.set({ userId: userId }, function () {
                varFunc(userId);
            });
        }
    });
}

//native message --------------------
var nativePort;
var popupPort;
var inSession = false;
var popupConnected = false;
var nativeConnected = false;

var sessionData = [[0, 0, 0, 0]];
var sessionIndex = 0;
var sessionPower = 0;
var currPower = 0;
var cpuPackage = 0;
var lastQuery = new Date();
var monitorStartTime = new Date();

var calculate_power = function (cpuPercentage) {
    return (cpuPercentage * cpuPackage) / (cpuPercentage * 0.412 + 0.246);
}

var updateAvgPower = function () {
    var currTime = new Date();
    var sessionTime = (currTime.getTime() - monitorStartTime.getTime()) / (3600 * 1000);
    var sessionAvgPower = sessionPower / sessionTime;

    if (sessionAvgPower != 0){
        avgPower = (avgPowerLoggedTime * avgPower + sessionAvgPower * sessionTime) / (avgPowerLoggedTime + sessionTime);
        avgPowerLoggedTime += sessionTime;
        chrome.storage.sync.set({ avgPower: avgPower });
        chrome.storage.sync.set({ avgPowerLoggedTime: avgPowerLoggedTime });
    }
}


var resetSessionData = function() {
    sessionData = [[0, 0, 0, 0]];
    sessionIndex = 0;
    sessionPower = 0;
    currPower = 0;
    cpuPackage = 0;
    lastQuery = new Date();
    monitorStartTime = new Date();
}

var sendPost = function (userId) {
    nativePort.postMessage({ "message": "POST user", "user_id": "" + userId, "suggestions": [], "tabs": "" + count });
    nativePort.postMessage({ "message": "GET sysInfo" });
}

var listenNative = function (msg) {
    if (!popupConnected || !nativeConnected){
        return;
    }

    var maxCpu = parseFloat(msg.max_cpu_power);
    if (!Number.isNaN(maxCpu)){
        cpuPackage = maxCpu;
    }
    else {
        var cpuData = parseFloat(msg.cpu_usage);
        var gpuData = parseFloat(msg.gpu_usage);
        var memData = parseFloat(msg.mem_usage);
        sessionIndex++;
        sessionData.push([sessionIndex, cpuData, gpuData, memData]);

        currPower = calculate_power(cpuData / 100)
        var currTime = new Date();
        var timeElapsed = (currTime.getTime() - lastQuery.getTime()) / (3600 * 1000);
        sessionPower += currPower * timeElapsed;
        lastQuery = new Date();

        if(popupConnected){
            popupPort.postMessage({type: "newData", cpu: cpuData, gpu: gpuData, mem: memData, curr: currPower, session: sessionPower});
        }
    }
}

var connectNative = function () {
    newUser = false;
    inSession = true;
    nativeConnected = true;
    resetSessionData();

    nativePort = chrome.runtime.connectNative("com.chrome.monitor");
    nativePort.onMessage.addListener(listenNative);
    nativePort.onDisconnect.addListener(function() {console.log("Native Disconnected");});
    nativePort.postMessage({ "message": "connected" });
    userIdFunc(sendPost);
}

var updateData = function () {
    if (nativeConnected) {
        nativePort.postMessage({ "message": "GET MonitorRecord" });
    }
}

var bgTimer;
var fgTimer;

var pauseMonitor = function () {
    clearInterval(fgTimer);
}

var resumeMonitor = function () {
    fgTimer = setInterval(updateData, ACTIVE_MONITOR_INTERVAL * 1000);
}

var stopMonitor = function () {
    inSession = false;
    nativeConnected = false;
    clearInterval(fgTimer);
    clearInterval(bgTimer);
    nativePort.postMessage({ "message": "GET suggestions" });
    nativePort.postMessage({ "message": "EXIT NATIVE" });
}

var moveMonitorToBg = function () {
    clearInterval(fgTimer);
    bgTimer = setInterval(updateData, BG_MONITOR_INTERVAL * 1000);
}

var moveMonitorToFg = function () {
    clearInterval(bgTimer);
    fgTimer = setInterval(updateData, ACTIVE_MONITOR_INTERVAL * 1000);
}

chrome.runtime.onConnect.addListener(function(dataPort) {
    if (dataPort.name === "nativeDataQuery") {
        popupPort = dataPort;
        popupConnected = true;
        popupPort.onMessage.addListener(function(msg) {
            if (msg.type == "startMonitor"){
                connectNative();
                moveMonitorToFg();
            } else if (msg.type == "stopMonitor"){            
                stopMonitor();
            } else if (msg.type == "pause"){
                pauseMonitor();
            } else if (msg.type == "resume"){
                resumeMonitor();
            }
        });
        popupPort.onDisconnect.addListener(function() {
            popupConnected = false;
            if (inSession){
                moveMonitorToBg();
            }
        });
        if (inSession){
            moveMonitorToFg();
        }
    }
});

//Tab count monitoring
//============================================
var pops = chrome.extension.getViews({ type: "popup" });

chrome.tabs.query({}, function (tabs) {
    count = tabs.length;
});

chrome.tabs.onCreated.addListener(function () {
    count++;
    if(popupConnected) {
        popupPort.postMessage({type: "tabCountUpdate"});
    }
})
chrome.tabs.onRemoved.addListener(function () {
    count--;
    if(popupConnected) {
        popupPort.postMessage({type: "tabCountUpdate"});
    }
})

//Carbon footprint calculator
//============================================
//retrieve stored data -----------------------
chrome.storage.sync.get('avgPower', function (items) {
    var lastAvgPower = items.avgPower;
    if (lastAvgPower) {
        avgPower = lastAvgPower;
    } else {
        chrome.storage.sync.set({ avgPower: 5 });
    }
});

chrome.storage.sync.get('avgPowerLoggedTime', function (items) {
    var loggedTime = items.avgPowerLoggedTime;
    if (loggedTime) {
        avgPowerLoggedTime = loggedTime;
    } else {
        chrome.storage.sync.set({ avgPowerLoggedTime: 0 });
    }
});

chrome.storage.sync.get('totalCO2', function (items) {
    var lastCO2 = items.totalCO2;
    if (lastCO2) {
        totalCO2 = lastCO2;
    } else {
        chrome.storage.sync.set({ totalCO2: 0 });
    }
});

//timer to count the CO2 ---------------------
var carbonTimer;

var carbonCounter = function(){
    totalCO2 += (CARBON_INTERVAL / 3600) * avgPower * 0.4173;
    chrome.storage.sync.set({ totalCO2: totalCO2 });
    if (popupConnected){
        popupPort.postMessage({type: "carbonUpdate"});
    }
}

carbonTimer = setInterval(carbonCounter, CARBON_INTERVAL * 1000);

