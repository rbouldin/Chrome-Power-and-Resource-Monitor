var bg = chrome.extension.getBackgroundPage();

var monitorStarted = false;
var monitorStartTime;
var timeUpdate

update_tabCount = function () {
    document.getElementById("tab_counter").innerText = "Number of tabs opened:" + bg.count;
}



var formatTime = function (TimeDiff) {
    var hour = parseInt(TimeDiff / (60 * 60 * 1000));
    var afterHour = TimeDiff - hour * 60 * 60 * 1000;
    var min = parseInt(afterHour / (60 * 1000));
    var afterMin = TimeDiff - hour * 60 * 60 * 1000 - min * 60 * 1000;
    var second = parseInt(afterMin / 1000)
    var output = hour.toString() + ':' + min.toString() + ':' + second.toString() + 's';
    return output;
}

var timer = function () {
    var currTime = new Date();
    var timeElapsed = currTime.getTime() - monitorStartTime.getTime();
    document.getElementById("monitor_timer").innerText = formatTime(timeElapsed);
}


//native message
var message = function (msg) {
    let cpu_data = parseFloat(msg.cpu_usage);
    let gpu_data = parseFloat(msg.gpu_usage);
    let mem_data = parseFloat(msg.mem_usage);
    document.getElementById("curr_power").innerText = "Current power usage: " + cpu_data.toFixed(2) + "W";
    document.getElementById("cpu_info").innerText = "CPU usage: " + cpu_data.toFixed(2) + "%";
    document.getElementById("gpu_info").innerText = "GPU usage: " + gpu_data.toFixed(2) + "%";
    document.getElementById("mem_info").innerText = "MEM usage: " + mem_data.toFixed(2) + "%";
    updateData(cpu_data, gpu_data, mem_data)
}

var disconnect = function () {
    console.warn("Disconnected");
}

var native_connection = function () {
    port = chrome.runtime.connectNative("com.chrome.monitor");
    port.onMessage.addListener(message);
    port.onDisconnect.addListener(disconnect);
}

//content
function processData(csv) {
    var allTextLines = csv.split(/\r\n|\n/);
    var lines = [];
    for (var i = 0; i < allTextLines.length; i++) {
        var data = allTextLines[i].split(',');
        var tarr = [];
        for (var j = 0; j < data.length; j++) {
            tarr.push(data[j].substring(1, data[j].length - 1));
        }
        lines.push(tarr);
    }
    return lines;
}

//load data
var power;
var cpu;
var gpu;
var processPower = function (data) {
    power = data[2][2];
    document.getElementById("curr_power").innerText = "Current power usage: " + power + "W";
}
var processRes = function (data) {
    cpu = data[3][1];
    gpu = data[4][1]
    document.getElementById("cpu_info").innerText = "CPU usage: " + cpu + "%";
    document.getElementById("gpu_info").innerText = "GPU usage: " + gpu + "%";
}

//read data from file
/* chrome.runtime.getPackageDirectoryEntry(function (root) {
    root.getFile("data/output_RES.csv", {}, function (fileEntry) {
        fileEntry.file(function (file) {
            var reader = new FileReader();
            reader.onloadend = function (e) {
                var res_data = processData(e.target.result);
                processRes(res_data);
            };
            reader.readAsText(file);
        });
    });
    root.getFile("data/output_POW.csv", {}, function (fileEntry) {
        fileEntry.file(function (file) {
            var reader = new FileReader();
            reader.onloadend = function (e) {
                var power_data = processData(e.target.result)
                processPower(power_data);
            };
            reader.readAsText(file);
        });
    });
}); */


//chart
var data;
var chart;
var option;
var chart_element;
google.charts.load('current', { 'packages': ['corechart'] }).then(function () {
    //google.charts.setOnLoadCallback(drawChart);

    data = new google.visualization.DataTable();
    data.addColumn('number', 'Time(Second)');
    data.addColumn('number', 'CPU usage');
    data.addColumn('number', 'GPU usage');
    data.addColumn('number', 'MEM usage');

    data.addRows([[0, 0, 0, 0],
    [1, 0, 0, 0]]);

    options = {
        chart: {
            //title: 'Power monitoring data',
        },
        width: 420,
        height: 170,
        chartArea: { width: '100%', height: '100%' },
        theme: 'maximized',
        backgroundColor: 'transparent',
        hAxis: {
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

    let drawChart = function () {
        chart_element = document.getElementById('power_chart');
        chart = new google.visualization.LineChart(chart_element);
        chart.draw(data, options);
    }
    google.charts.setOnLoadCallback(drawChart);
})

var chart_index = 0;
let updateData = function (cpu_num, gpu_num, mem_num) {
    chart_index++;
    data.addRow([chart_index, cpu_num, gpu_num, mem_num]);
    chart.draw(data, options);
}

chrome.runtime.onMessage.addListener(
    function (request, sender, sendResponse) {
        if (request.msg == "tab_count_update")
            update_tabCount();
    });

click_events = function () {
    document.getElementById("start_monitor").addEventListener("click", function () {
        monitorStarted = true;
        native_connection();
        monitorStartTime = new Date();
        timeUpdate = setInterval(timer, 1000);
        document.getElementById("button_layer").style.visibility = "hidden";
        document.getElementById("details").style.cssText = "filter: blur(0px);";

    });
    document.getElementById("stop_monitor").addEventListener("click", function () {

    });
};

document.addEventListener("DOMContentLoaded", function () {
    click_events();
    update_tabCount();
});