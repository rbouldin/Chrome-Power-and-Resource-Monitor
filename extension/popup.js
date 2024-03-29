const countSeconds = (str) => {
    const [mm = '0', ss = '0'] = (str || '0:0').split(':');
    const minute = parseInt(mm, 10) || 0;
    const second = parseInt(ss, 10) || 0;
    return (minute*60) + (second);
 };

//visual parts
var hideButtonLayer = function () {
    document.getElementById("button_layer").style.visibility = "hidden";
    document.getElementById("details").style.cssText = "filter: blur(0px);";
}

var showButtonLayer = function () {
    document.getElementById("details").style.cssText = "filter: blur(3px) brightness(70%);";
    document.getElementById("button_layer").style.visibility = "visible";
}

var changeStoppedButtons = function () {
    document.getElementById("stop_start_btn").innerText = "Start";

    var SessionTimeElem = document.getElementById("pause_start_btn");
    SessionTimeElem.removeEventListener("click", null);
    SessionTimeElem.setAttribute("contenteditable", "true");
    SessionTimeElem.classList.add("edit_hover");
    SessionTimeElem.innerHTML = "00:00";
}

var changeStartedButtons = function () {
    document.getElementById("pause_start_btn").innerText = "Pause";
    document.getElementById("stop_start_btn").innerText = "Stop";

    var SessionTimeElem = document.getElementById("pause_start_btn");
    SessionTimeElem.addEventListener("click", pauseBtn);
    SessionTimeElem.setAttribute("contenteditable", "false");
    SessionTimeElem.classList.remove("edit_hover");
}

//header info module
//============================
//tab count---------------
var bg = chrome.extension.getBackgroundPage();
var dataPort = chrome.runtime.connect({ name: "nativeDataQuery" });

var updateTabCount = function () {
    document.getElementById("tab_counter").innerText = "Number of tabs opened:" + bg.count;
}

//carbon footprint---------------
var updateCarbonFootprint = function () {
    document.getElementById("carbon_counter").innerText = "Total Carbon footprint by chrome: " + bg.totalCO2.toFixed(2) + " Gram of CO2";
}

//time count
//=================================
var monitorStartTime;

//Time formatter -------------------
var padNum = function (num) {
    if (num < 10) {
        return "0" + num.toString();
    }
    else {
        return num.toString();
    }
}

var formatTime = function (TimeDiff) {
    var hour = parseInt(TimeDiff / (60 * 60 * 1000));
    var afterHour = TimeDiff - hour * 60 * 60 * 1000;
    var min = parseInt(afterHour / (60 * 1000));
    var afterMin = TimeDiff - hour * 60 * 60 * 1000 - min * 60 * 1000;
    var second = parseInt(afterMin / 1000)
    var output = padNum(hour) + ':' + padNum(min) + ':' + padNum(second) + 's';
    return output;
}

//Timer -------------------
var timer;
var monitorTimeLimit = 0; 
var monitorStarted = false;

var timeTimer = function () {
    var currTime = new Date();
    var timeElapsed = currTime.getTime() - monitorStartTime.getTime();
    if(monitorTimeLimit != 0 && (timeElapsed / 1000) > monitorTimeLimit) {
        monitorStarted = false;
        changeStoppedButtons();
        clearInterval(timer);
        finishChart();
    } else {
        document.getElementById("timer_text").innerText = formatTime(timeElapsed);
    }   
}

//data chart
//===========================
var data;
var chart;
var chartElement;
var chartIndex = 0;
var maxDataPoints = 10;

var options = {
    width: 420,
    height: 170,
    chartArea: { width: '100%', height: '100%' },
    theme: 'maximized',
    backgroundColor: 'transparent',
    hAxis: {
        viewWindow: { min: 0, max: maxDataPoints },
        textStyle: {
            color: '#FFF',
            fontSize: 10
        },
        baselineColor: {
            color: 'transparent'
        },
        gridlines: {
            color: 'transparent',
            count: 10
        }
    },
    vAxis: {
        viewWindowMode: 'maximized',
        viewWindow: { min: 0, max: 100 },
        textStyle: { color: '#FFF', fontSize: 10 },
        baselineColor: { color: 'transparent' }
    },
    legend: {
        textStyle: { color: '#FFF', fontSize: 10 }
    },
    explorer: { axis: 'horizontal' }
};

var drawEmpty = function () {
    google.charts.load('current', { 'packages': ['corechart'] }).then(function () {
        data = new google.visualization.DataTable();
        data.addColumn('number', 'Time(Second)');
        data.addColumn('number', 'CPU usage');
        data.addColumn('number', 'GPU usage');
        data.addColumn('number', 'MEM usage');
        data.addRow([0, 0, 0, 0]);
    
        var drawChart = function () {
            chartElement = document.getElementById('power_chart');
            chart = new google.visualization.LineChart(chartElement);
            chart.draw(data, options);
        }
        google.charts.setOnLoadCallback(drawChart);
    });
}

var updateData = function (cpuNum, gpuNum, memNum) {
    chartIndex++;
    data.addRow([chartIndex, cpuNum, gpuNum, memNum]);
    if (chartIndex > maxDataPoints) {
        options.hAxis.viewWindow.max = chartIndex;
        options.hAxis.viewWindow.min = chartIndex - maxDataPoints;
    }
    chart.draw(data, options);
}

var finishChart = function () {
    options.hAxis.viewWindow.max = chartIndex;
    options.hAxis.viewWindow.min = 0;
    chart.draw(data, options);
}

var clearChart = function () {
    data = new google.visualization.DataTable();
    data.addColumn('number', 'Time(Second)');
    data.addColumn('number', 'CPU usage');
    data.addColumn('number', 'GPU usage');
    data.addColumn('number', 'MEM usage');
    data.addRow([0, 0, 0, 0]);
    chartIndex = 0;

    options.hAxis.viewWindow.min = 0;
    options.hAxis.viewWindow.max = maxDataPoints;
    chart.draw(data, options);

    var powerDisplay = document.getElementById("curr_power").childNodes[0];
    powerDisplay.nodeValue = "Current power usage: 0.00 W  ";
    document.getElementById("total_power").innerText = "Session power usage: 0.00 Wh ";
    document.getElementById("cpu_info").innerText = "CPU: 00.00%";
    document.getElementById("gpu_info").innerText = "GPU: 00.00%";
    document.getElementById("mem_info").innerText = "MEM: 00.00%";
}

var loadFromBg = function () {
    google.charts.load('current', { 'packages': ['corechart'] }).then(function () {
        data = new google.visualization.DataTable();
        data.addColumn('number', 'Time(Second)');
        data.addColumn('number', 'CPU usage');
        data.addColumn('number', 'GPU usage');
        data.addColumn('number', 'MEM usage');
        data.addRows(bg.sessionData);
        chartIndex = bg.sessionIndex;
        if (chartIndex > maxDataPoints) {
            options.hAxis.viewWindow.max = chartIndex;
            options.hAxis.viewWindow.min = chartIndex - maxDataPoints;
        }
        chartElement = document.getElementById('power_chart');
        chart = new google.visualization.LineChart(chartElement);
        chart.draw(data, options);
    });
    var powerDisplay = document.getElementById("curr_power").childNodes[0];
    powerDisplay.nodeValue = "Current power usage: 0.00 W  ";
    var totalPowerDisplay = document.getElementById("total_power").childNodes[0];
    totalPowerDisplay.nodeValue = "Session power usage: " + bg.sessionPower.toFixed(2) + " Wh ";
    // document.getElementById("total_power").innerText = "Session power usage: " + bg.sessionPower.toFixed(2) + " Wh ";

    monitorStartTime = bg.monitorStartTime;
    monitorTimeLimit = bg.monitorLimit;
    timer = setInterval(timeTimer, 1000);
}

var drawLagacy = function () {
    google.charts.load('current', { 'packages': ['corechart'] }).then(function () {
        data = new google.visualization.DataTable();
        data.addColumn('number', 'Time(Second)');
        data.addColumn('number', 'CPU usage');
        data.addColumn('number', 'GPU usage');
        data.addColumn('number', 'MEM usage');
        data.addRows(bg.sessionData);
        chartIndex = bg.sessionIndex;
        if (chartIndex > maxDataPoints) {
            options.hAxis.viewWindow.max = chartIndex;
            options.hAxis.viewWindow.min = chartIndex - maxDataPoints;
        }
        chartElement = document.getElementById('power_chart');
        chart = new google.visualization.LineChart(chartElement);
        chart.draw(data, options);
    });
}

//native message
//============================
var receiveData = function (cpu, gpu, mem, currPower, sessionPower) {
    if (!monitorStarted) {
        return;
    }
    if (bg.runWithOhm && !bg.ohmRunning) {
        return;
    }
    if (cpu == null && gpu == null && mem == null) {
        return;
    }

    if (cpu != null) {
        document.getElementById("cpu_info").innerText = "CPU: " + cpu.toFixed(2) + "%";
    }
    if (mem != null) {
        document.getElementById("mem_info").innerText = "MEM: " + mem.toFixed(2) + "%";
    }
    if (gpu != null) {
        //in case open hardware monitor is not opened
        document.getElementById("gpu_info").innerText = "GPU: " + gpu.toFixed(2) + "%";
    }

    if (currPower != null) {
        var powerDisplay = document.getElementById("curr_power").childNodes[0];
        powerDisplay.nodeValue = "Current power usage: " + currPower.toFixed(2) + " W  ";
    }
    if (sessionPower != null) {
        var totalPowerDisplay = document.getElementById("total_power").childNodes[0];
        totalPowerDisplay.nodeValue = "Session power usage: " + bg.sessionPower.toFixed(2) + " Wh ";
    }
    // document.getElementById("total_power").innerText = "Session power usage: " + sessionPower.toFixed(2) + " Wh"
    updateData(cpu, gpu, mem);
}

var newSession = function (timeLimit) {
    monitorStarted = true;
    clearChart();
    changeStartedButtons();

    if (bg.newUser) {
        var serverOption = document.getElementById("serverCheckbox").checked;
        var loggingOption = document.getElementById("loggingCheckbox").checked;
        dataPort.postMessage({ type: "options", server: serverOption, log: loggingOption});
        hideButtonLayer();
    }
    dataPort.postMessage({ type: "startMonitor", limit: timeLimit});

    monitorStartTime = new Date();
    monitorTimeLimit = timeLimit;
    timer = setInterval(timeTimer, 1000);
}

var endSession = function () {
    monitorStarted = false;
    changeStoppedButtons();
    dataPort.postMessage({ type: "stopMonitor" });

    clearInterval(timer);
    finishChart();
}

//suggestion display
var updateSuggestion = function(suggestionText){
    document.getElementsByClassName("suggestion_cover")[0].style.visibility = "hidden";
    document.getElementsByClassName("suggestion_text")[0].style.visibility = "visible";
    document.getElementsByClassName("suggestion_text")[0].innerHTML = "<b>Based on the last session:</b><br>" + suggestionText;
}

//binding events
//=============================
dataPort.onMessage.addListener(function (msg) {
    if (msg.type == "tabCountUpdate") {
        updateTabCount();
    } else if (msg.type == "carbonUpdate") {
        updateCarbonFootprint();
    } else if (msg.type == "newData") {
        receiveData(msg.cpu, msg.gpu, msg.mem, msg.curr, msg.session);
    } else if (msg.type == "suggestion") {
        updateSuggestion(msg.text);
    }
})

var pauseBtn = function() {
    if (monitorStarted) {
        if (paused) {
            dataPort.postMessage({ type: "resume" });
            timer = setInterval(timeTimer, 1000);
            document.getElementById("pause_start_btn").innerText = "Pause";
        } else {
            dataPort.postMessage({ type: "pause" });
            clearInterval(timer);
            document.getElementById("pause_start_btn").innerText = "Resume";
        }
        paused = !paused;
    }
}

let paused = false;
let click_events = function () {
    document.getElementById("start_monitor").addEventListener("click", function () {
        newSession(0);
    });
    document.getElementById("stop_start_btn").addEventListener("click", function () {
        if (monitorStarted) {
            endSession();
        } else {
            var timeLimit = 0;
            var timeLimitText = document.getElementById("pause_start_btn").innerText;
            if (timeLimitText != "00:00"){
                timeLimit = countSeconds(timeLimitText);
            }
            newSession(timeLimit);
        }
    });
    document.getElementById("pause_start_btn").addEventListener("click", pauseBtn);
};

var initBasedOnStatus = function () {
    //if the user is opening the popup the first time since chrome started
    if (!bg.newUser) {
        hideButtonLayer();
    }

    //if the background is running one session
    monitorStarted = bg.inSession;
    if (monitorStarted) {
        loadFromBg();
    } else if (bg.lagacyData){
        bg.lagacyData = false;
        drawLagacy();
        changeStoppedButtons();
    }
    else {
        drawEmpty();
        changeStoppedButtons();
    }
}

var fetchSuggestion = function (){
    document.getElementsByClassName("suggestion_text")[0].style.visibility = "hidden";
    dataPort.postMessage({ type: "updateSuggestion" });
}

document.addEventListener("DOMContentLoaded", function () {
    initBasedOnStatus();
    click_events();
    updateTabCount();
    updateCarbonFootprint();
    fetchSuggestion();

    chrome.storage.sync.get(['serverEnabled', 'loggingEnabled'], function(result) {
        if (result.serverEnabled) {
            document.getElementById('serverCheckbox').checked = true;
        } else {
            document.getElementById('serverCheckbox').checked = false;
        }
        if (result.loggingEnabled) {
            document.getElementById('loggingCheckbox').checked = true;
        } else {
            document.getElementById('loggingCheckbox').checked = false;
        }
    });
});