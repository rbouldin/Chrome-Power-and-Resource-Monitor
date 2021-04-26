var count = 0;

//native data query
//=======================================================

//unique user id --------------------
var getRandomToken = function () {
    var randomPool = new Uint8Array(16);
    crypto.getRandomValues(randomPool);
    var hex = '';
    for (var i = 0; i < randomPool.length; ++i) {
        hex += randomPool[i].toString(8);
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
var popupConnected = false;
var nativeConnected = false;

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
        popupPort.postMessage({type: "maxCpuPower", power: maxCpu});
    }
    else {
        var cpuData = parseFloat(msg.cpu_usage);
        var gpuData = parseFloat(msg.gpu_usage);
        var memData = parseFloat(msg.mem_usage);
        popupPort.postMessage({type: "newData", cpu: cpuData, gpu: gpuData, mem: memData});
    }
}

var connectNative = function () {
    nativeConnected = true;
    nativePort = chrome.runtime.connectNative("com.chrome.monitor");
    nativePort.onMessage.addListener(listenNative);
    nativePort.onDisconnect.addListener(function() {
        console.log("Disconnected");
    });
    nativePort.postMessage({ "message": "connected" });
    userIdFunc(sendPost);
}

var updateData = function () {
    if (nativeConnected) {
        nativePort.postMessage({ "message": "GET MonitorRecord" });
    }
}

var stopMonitor = function () {
    nativeConnected = false;
    nativePort.postMessage({ "message": "STOP monitoring" });
    nativePort.postMessage({ "message": "GET suggestions" });
}

chrome.runtime.onConnect.addListener(function(dataPort) {
    if (dataPort.name === "nativeDataQuery") {
        popupPort = dataPort;
        popupConnected = true;
        popupPort.onMessage.addListener(function(msg) {
            if (msg.type == "startMonitor"){
                connectNative();
            } else if (msg.type == "stopMonitor"){            
                stopMonitor();
            } else if (msg.type == "dataUpdate"){
                updateData();
            }
        });
        popupPort.onDisconnect.addListener(function() {
            console.log("popup has been closed");
            popupConnected = false;
            if (nativeConnected){
                stopMonitor();
            }
        });
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