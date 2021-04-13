//tab count module
//============================
var bg = chrome.extension.getBackgroundPage();

update_tabCount = function () {
    document.getElementById("tab_counter").innerText = "Number of tabs opened:" + bg.count;
}

//timer to update the time count
//============================
var monitorStarted = false;
var monitorStartTime;
var timeUpdate

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

var newTimer = function () {
    var currTime = new Date();
    var timeElapsed = currTime.getTime() - monitorStartTime.getTime();
    document.getElementById("monitor_timer").innerText = formatTime(timeElapsed);
    port.postMessage({ "message": "GET MonitorRecord" });
}

//unique user id
//============================
function getRandomToken() {
    var randomPool = new Uint8Array(16);
    crypto.getRandomValues(randomPool);
    var hex = '';
    for (var i = 0; i < randomPool.length; ++i) {
        hex += randomPool[i].toString(8);
    }
    return hex;
}

function userIdFunc(var_func) {
    chrome.storage.sync.get('userid', function (items) {
        var userid = items.userid;
        if (userid) {
            var_func(userid);
        } else {
            userid = getRandomToken();
            chrome.storage.sync.set({ userid: userid }, function () {
                var_func(userid);
            });
        }
    });
}

//native message
//============================
var port;
var cpu_package;
var calculate_power = function (cpu_percentage) {
    console.log("cpu_package", cpu_package);
    console.log("cpu_percentage", cpu_percentage);
    return (cpu_percentage * cpu_package) / (cpu_percentage * 0.412 + 0.246);
}

var message = function (msg) {
    if (monitorStarted) {
        if (!Number.isNaN(parseFloat(msg.max_cpu_power))) {
            cpu_package = parseFloat(msg.max_cpu_power)
        } else {
            let cpu_data = parseFloat(msg.cpu_usage);
            let gpu_data = parseFloat(msg.gpu_usage);
            let mem_data = parseFloat(msg.mem_usage);
            document.getElementById("cpu_info").innerText = "CPU: " + cpu_data.toFixed(2) + "%";
            document.getElementById("gpu_info").innerText = "GPU: " + gpu_data.toFixed(2) + "%";
            document.getElementById("mem_info").innerText = "MEM: " + mem_data.toFixed(2) + "%";
            document.getElementById("curr_power").innerText = "Current power usage: " + calculate_power(cpu_data / 100).toFixed(2) + " W";
            updateData(cpu_data, gpu_data, mem_data)
        }
    }
}

var disconnect = function () {
    //console.warn("Disconnected");
}

var send_post = function (user_id) {
    //console.log("user id: ", user_id);
    port.postMessage({ "message": "POST user", "user_id": "" + user_id, "suggestions": [], "tabs": "" + bg.count });
    port.postMessage({ "message": "GET sysInfo" });
}

var native_connection = function () {
    port = chrome.runtime.connectNative("com.chrome.monitor");
    port.onMessage.addListener(message);
    port.onDisconnect.addListener(disconnect);
    port.postMessage({ "message": "connected" });
    userIdFunc(send_post);
}

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

    data.addRow([0, 0, 0, 0]);

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

    let drawChart = function () {
        chart_element = document.getElementById('power_chart');
        chart = new google.visualization.LineChart(chart_element);
        chart.draw(data, options);
    }
    google.charts.setOnLoadCallback(drawChart);
})

var chart_index = 0;
let updateData = function (cpu_num, gpu_num, mem_num) {
    //if cpu_num != 
    chart_index++;
    data.addRow([chart_index, cpu_num, gpu_num, mem_num]);
    if (chart_index > 10) {
        options.hAxis.viewWindow.max = chart_index;
        options.hAxis.viewWindow.min = chart_index - 10;
    }
    chart.draw(data, options);
}

chrome.runtime.onMessage.addListener(
    function (request, sender, sendResponse) {
        if (request.msg == "tab_count_update")
            update_tabCount();
    });

let clear_chart = function () {
    data = new google.visualization.DataTable();
    data.addColumn('number', 'Time(Second)');
    data.addColumn('number', 'CPU usage');
    data.addColumn('number', 'GPU usage');
    data.addColumn('number', 'MEM usage');
    data.addRow([0, 0, 0, 0]);
    chart_index = 0;
    chart.draw(data, options);

    document.getElementById("cpu_info").innerText = "CPU: 00.00%";
    document.getElementById("gpu_info").innerText = "GPU: 00.00%";
    document.getElementById("mem_info").innerText = "MEM: 00.00%";
}

//click functions
//============================
let click_events = function () {
    document.getElementById("start_monitor").addEventListener("click", function () {
        monitorStarted = true;
        native_connection();
        monitorStartTime = new Date();
        timeUpdate = setInterval(newTimer, 1000);
        document.getElementById("button_layer").style.visibility = "hidden";
        document.getElementById("details").style.cssText = "filter: blur(0px);";
    });
    document.getElementById("stop_monitor").addEventListener("click", function () {
        monitorStarted = false;
        clearInterval(timeUpdate);
        port.postMessage({ "message": "STOP monitoring" });
        port.postMessage({ "message": "GET suggestions" });
        clear_chart();
        document.getElementById("details").style.cssText = "filter: blur(3px) brightness(70%);";
        document.getElementById("button_layer").style.visibility = "visible";
    });
};

document.addEventListener("DOMContentLoaded", function () {
    click_events();
    update_tabCount();
});