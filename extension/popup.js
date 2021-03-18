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
    var output = hour.toString() + 'H ' + min.toString() + 'M ' + second.toString() + 'S';
    return output;
}

var timer = function () {
    var currTime = new Date();
    var timeElapsed = currTime.getTime() - monitorStartTime.getTime();
    document.getElementById("monitor_timer").innerText = formatTime(timeElapsed);
}

//content
function processData(csv) {
    var allTextLines = csv.split(/\r\n|\n/);
    var lines = [];
    for (var i=0; i<allTextLines.length; i++) {
        var data = allTextLines[i].split(',');
            var tarr = [];
            for (var j=0; j<data.length; j++) {
                tarr.push(data[j].substring(1, data[j].length-1));
            }
            lines.push(tarr);
    }
   return lines;
}

//load data
var power;
var cpu;
var gpu;
var processPower = function(data){
    power = data[2][2];
    document.getElementById("curr_power").innerText = "Current power usage: " + power + "W";
}
var processRes = function(data){
    cpu = data[3][1];
    gpu = data[4][1]
    document.getElementById("cpu_info").innerText = "CPU usage: " + cpu + "%";
    document.getElementById("gpu_info").innerText = "GPU usage: " + gpu + "%";
}
chrome.runtime.getPackageDirectoryEntry(function (root) {
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
});


//chart
google.charts.load('current', { 'packages': ['corechart'] });
google.charts.setOnLoadCallback(drawChart);

function drawChart() {
    var element = document.getElementById('power_chart');

    var data = new google.visualization.DataTable();
    data.addColumn('number', 'Time(Second)');
    data.addColumn('number', 'Power usage');
    data.addColumn('number', 'Memory usage');
    data.addColumn('number', 'CPU usage');

    data.addRows([
        [1, 37.8, 80.8, 41.8],
        [2, 30.9, 69.5, 32.4],
        [3, 25.4, 57, 25.7],
        [4, 11.7, 18.8, 10.5],
        [5, 11.9, 17.6, 10.4],
        [6, 8.8, 13.6, 7.7],
        [7, 7.6, 12.3, 9.6],
        [8, 12.3, 29.2, 10.6],
        [9, 16.9, 42.9, 14.8],
        [10, 12.8, 30.9, 11.6],
        [11, 5.3, 7.9, 4.7],
        [12, 6.6, 8.4, 5.2],
        [13, 4.8, 6.3, 3.6],
        [14, 4.2, 6.2, 3.4]
    ]);

    var options = {
        chart: {
            //title: 'Power monitoring data',
        },
        width: 380,
        height: 160,
        backgroundColor: 'transparent',
        hAxis: {
            textStyle: { color: '#FFF', fontSize: 10 },
            gridlines: {
                color: 'transparent'
            }
        },
        vAxis: {
            textStyle: { color: '#FFF', fontSize: 10 },
            baselineColor: {
                color: 'transparent'
            }
        },
        legend: {
            textStyle: { color: '#FFF', fontSize: 10 }
        },
        theme: 'maximized'
    };

    var chart = new google.visualization.LineChart(element);
    chart.draw(data, options);
}

chrome.runtime.onMessage.addListener(
    function (request, sender, sendResponse) {
        if (request.msg == "tab_count_update")
            update_tabCount();
    });

click_events = function () {
    document.getElementById("start_monitor").addEventListener("click", function () {
        if (monitorStarted == false) {
            monitorStartTime = new Date();
            timeUpdate = setInterval(timer, 1000);
            document.getElementById("start_monitor").innerText = "stop monitor";
            monitorStarted = true;
        } else {
            document.getElementById("start_monitor").innerText = "start monitor";
            clearInterval(timeUpdate);
            monitorStarted = false;
            document.getElementById("button_layer").style.visibility = "hidden";
            document.getElementById("details").style.cssText = "filter: blur(0px);";;
            /* .style.filter = "filter: blur(10px);"; */
        }
    });
};

document.addEventListener("DOMContentLoaded", function () {
    click_events();
    update_tabCount();
});