//determine whether need to blur
var newUser = true;

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
var dataTimer = function () {
    var currTime = new Date();
    var timeElapsed = currTime.getTime() - monitorStartTime.getTime();
    document.getElementById("monitor_timer").innerText = formatTime(timeElapsed);
    dataPort.postMessage({ type: "dataUpdate" });
}

//data chart
//===========================
var data;
var chart;
var option;
var chartElement;

google.charts.load('current', { 'packages': ['corechart'] }).then(function () {
    data = new google.visualization.DataTable();
    data.addColumn('number', 'Time(Second)');
    data.addColumn('number', 'CPU usage');
    data.addColumn('number', 'GPU usage');
    data.addColumn('number', 'MEM usage');
    data.addRow([0, 0, 0, 0]);

    options = {
        width: 420,
        height: 170,
        chartArea: { width: '100%', height: '100%' },
        theme: 'maximized',
        backgroundColor: 'transparent',
        hAxis: {
            viewWindow: { min: 0, max: 10 },
            textStyle:
            {
                color: '#FFF',
                fontSize: 10
            },
            baselineColor: {
                color: 'transparent'
            },
            gridlines:
            {
                color: 'transparent',
                count: 10
            }
        },
        vAxis: {
            viewWindowMode: 'maximized',
            viewWindow: { min: 0, max: 100 },
            textStyle: { color: '#FFF', fontSize: 10 },
            baselineColor: {
                color: 'transparent'
            }
        },
        legend: {
            textStyle: { color: '#FFF', fontSize: 10 }
        },
    };

    var drawChart = function () {
        chartElement = document.getElementById('power_chart');
        chart = new google.visualization.LineChart(chartElement);
        chart.draw(data, options);
    }
    google.charts.setOnLoadCallback(drawChart);
})

var chartIndex = 0;
var maxDataPoints = 10
var updateData = function (cpuNum, gpuNum, memNum) {
    chartIndex++;
    data.addRow([chartIndex, cpuNum, gpuNum, memNum]);
    if (chartIndex > maxDataPoints) {
        options.hAxis.viewWindow.max = chartIndex;
        options.hAxis.viewWindow.min = chartIndex - maxDataPoints;
    }
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
    chart.draw(data, options);

    document.getElementById("cpu_info").innerText = "CPU: 00.00%";
    document.getElementById("gpu_info").innerText = "GPU: 00.00%";
    document.getElementById("mem_info").innerText = "MEM: 00.00%";
}

//native message
//============================
var monitorStarted = false;
var lastQuery = new Date();
var cpuPackage;
var totalPower;
var calculate_power = function (cpuPercentage) {
    return (cpuPercentage * cpuPackage) / (cpuPercentage * 0.412 + 0.246);
}

var receiveData = function(cpu, gpu, mem){
    if (!monitorStarted){
        return;
    }

    document.getElementById("cpu_info").innerText = "CPU: " + cpu.toFixed(2) + "%";
    document.getElementById("mem_info").innerText = "MEM: " + mem.toFixed(2) + "%";
    if (gpu != null){
        //in case open hardware monitor is not opened
        document.getElementById("gpu_info").innerText = "GPU: " + gpu.toFixed(2) + "%";
    }

    var currTime = new Date();
    //time elapse in hour
    var timeElapsed = (currTime.getTime() - lastQuery.getTime()) / (3600 * 1000);
    var currPower = calculate_power(cpu / 100);
    totalPower += currPower * timeElapsed;

    var powerDisplay = document.getElementById("curr_power").childNodes[0];
    powerDisplay.nodeValue = "Current power usage: " + currPower.toFixed(2) + " W";
    document.getElementById("total_power").innerText = "Total power usage: " + totalPower.toFixed(2) + " Wh"
    updateData(cpu, gpu, mem);
    lastQuery = new Date();
}

var newSession = function(){
    monitorStarted = true;

    totalPower = 0;
    lastQuery = new Date();
    monitorStartTime = new Date();
    
    dataPort.postMessage({type: "startMonitor"});
    timer = setInterval(dataTimer, 1000);
    document.getElementById("button_layer").style.visibility = "hidden";
    document.getElementById("details").style.cssText = "filter: blur(0px);";
}

var endSession = function(){   
    clearInterval(timer);
    monitorStarted = false;
    
    dataPort.postMessage({type: "stopMonitor"});
    var currTime = new Date();
    var timeElapsed = (currTime.getTime() - monitorStartTime.getTime()) / (3600 * 1000);
    var avgPower = totalPower / timeElapsed;
    dataPort.postMessage({type: "avgPower", power: avgPower});
    
    clearChart();
    document.getElementById("details").style.cssText = "filter: blur(3px) brightness(70%);";
    document.getElementById("button_layer").style.visibility = "visible";
}


//binding events
//=============================
dataPort.onMessage.addListener(function(msg) {
    if (msg.type == "tabCountUpdate"){
        updateTabCount();
    }else if (msg.type == "carbonUpdate"){
        updateCarbonFootprint();
    }
    else if (msg.type == "maxCpuPower"){
        cpuPackage = msg.power;
    } else if (msg.type == "newData"){
        receiveData(msg.cpu, msg.gpu, msg.mem);
    }
})

let paused = false;
let click_events = function () {
    document.getElementById("start_monitor").addEventListener("click", function () {
        newSession();
    });
    document.getElementById("stop_monitor").addEventListener("click", function () {
        endSession();
    });
    document.getElementById("pause_monitor").addEventListener("click", function () {
        if (paused){
            document.getElementById("pause_monitor").innerText = "Pause";
            timer = setInterval(dataTimer, 1000);
        } else {
            document.getElementById("pause_monitor").innerText = "Resume";
            clearInterval(timer);
        }
        paused = !paused;
    });
};

document.addEventListener("DOMContentLoaded", function () {
    click_events();
    updateTabCount();
    updateCarbonFootprint();
});