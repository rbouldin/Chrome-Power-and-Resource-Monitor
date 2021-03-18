var bg = chrome.extension.getBackgroundPage();

update_tabCount = function () {
    document.getElementById("tab_counter").innerText = "Number of tabs opened:" + bg.count;
}

click_events = function () {
    /* document.getElementById("stop_monitor").addEventListener("click", function() {
        alert("monitoring stoped");
    }); */
    document.getElementById("start_monitor").addEventListener("click", function () {
        alert("monitoring started");
    });
};

//content
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
            baselineColor:{
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

document.addEventListener("DOMContentLoaded", function () {
    click_events();
    update_tabCount();
});