click_events = function(){
    document.getElementById("stop_monitor").addEventListener("click", function() {
        alert("monitoring stoped");
    });
    document.getElementById("start_monitor").addEventListener("click", function() {
        alert("monitoring started");
    });
};

document.addEventListener("DOMContentLoaded", function(){
    click_events();
}); 