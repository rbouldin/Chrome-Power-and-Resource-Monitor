var pops = chrome.extension.getViews({ type: "popup" });

var count = 0;
chrome.tabs.query({}, function (tabs) {
    count = tabs.length;
});

chrome.tabs.onCreated.addListener(function () {
    count++;
    chrome.runtime.sendMessage({
        msg: "tab_count_update"
    });
})
chrome.tabs.onRemoved.addListener(function () {
    count--;
    chrome.runtime.sendMessage({
        msg: "tab_count_update"
    });
})
