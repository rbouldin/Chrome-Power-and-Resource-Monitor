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
    document.getElementById("pause_start_btn").innerText = "Start timer";
    document.getElementById("stop_start_btn").innerText = "Start";
}

var changeStartedButtons = function () {
    document.getElementById("pause_start_btn").innerText = "Pause";
    document.getElementById("stop_start_btn").innerText = "Stop";
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
var timeTimer = function () {
    var currTime = new Date();
    var timeElapsed = currTime.getTime() - monitorStartTime.getTime();
    document.getElementById("monitor_timer").innerText = formatTime(timeElapsed);
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

    document.getElementById("curr_power").innerText = "Current power usage: 0.00 W";
    document.getElementById("total_power").innerText = "Total power usage: 0.00 Wh";
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
    document.getElementById("curr_power").innerText = "Current power usage: 0.00 W";
    document.getElementById("total_power").innerText = "Total power usage: " + bg.sessionPower.toFixed(2) + " Wh"
}

//native message
//============================
var monitorStarted = false;

var receiveData = function (cpu, gpu, mem, currPower, sessionPower) {
    if (!monitorStarted) {
        return;
    }

    document.getElementById("cpu_info").innerText = "CPU: " + cpu.toFixed(2) + "%";
    document.getElementById("mem_info").innerText = "MEM: " + mem.toFixed(2) + "%";
    if (gpu != null) {
        //in case open hardware monitor is not opened
        document.getElementById("gpu_info").innerText = "GPU: " + gpu.toFixed(2) + "%";
    }

    var powerDisplay = document.getElementById("curr_power").childNodes[0];
    powerDisplay.nodeValue = "Current power usage: " + currPower.toFixed(2) + " W";
    document.getElementById("total_power").innerText = "Total power usage: " + sessionPower.toFixed(2) + " Wh"
    updateData(cpu, gpu, mem);
}

var newSession = function () {
    monitorStarted = true;
    clearChart();
    changeStartedButtons();
    dataPort.postMessage({ type: "startMonitor" });

    monitorStartTime = new Date();
    timer = setInterval(timeTimer, 1000);

    if (bg.newUser) {
        hideButtonLayer();
    }
}

var endSession = function () {
    monitorStarted = false;
    changeStoppedButtons();
    dataPort.postMessage({ type: "stopMonitor" });

    clearInterval(timer);
    finishChart();
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
    }
})

let paused = false;
let click_events = function () {
    document.getElementById("start_monitor").addEventListener("click", function () {
        newSession();
    });
    document.getElementById("stop_start_btn").addEventListener("click", function () {
        if (monitorStarted) {
            endSession();
        } else {
            newSession();
        }
    });
    document.getElementById("pause_start_btn").addEventListener("click", function () {
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
        } else {
            //timer later
        }
    });
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
    } else {
        drawEmpty();
        changeStoppedButtons();
    }
}

document.addEventListener("DOMContentLoaded", function () {
    initBasedOnStatus();
    click_events();
    updateTabCount();
    updateCarbonFootprint();
});