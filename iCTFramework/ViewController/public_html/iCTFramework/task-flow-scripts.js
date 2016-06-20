// reference: https://docs.oracle.com/middleware/maf220/mobile/js-ref/adf.mf.api.amx.htm
document.addEventListener("deviceready", function () {
    document.addEventListener("backbutton", onBackButton, true);
    document.addEventListener("menubutton", onIgnoredButton, false);
    document.addEventListener("searchbutton", onIgnoredButton, false);
    document.addEventListener("startcallbutton", onIgnoredButton, false);
    document.addEventListener("endcallbutton", onIgnoredButton, false);
    document.addEventListener("volumedownbutton", onIgnoredButton, false);
    document.addEventListener("volumeupbutton", onIgnoredButton, false);
    obtainKioskStatus();
},
false);

// hardware back button will trigger button with id="back"
function onBackButton() {
    var backButton = document.getElementById("back");
    if (backButton != null) {
        adf.mf.api.amx.triggerBubbleEventListener(backButton, "tap");
    }
}

function onIgnoredButton() {}

function obtainKioskStatus() {
    KioskPlugin.isInKiosk(function(is) {
        adf.mf.el.setValue({ "name": "#{ApplicationBean.isInKiosk}", "value": is }, function(){}, function() {
            alert("LoginBean.isInKiosk setting failed");
        });
    });
}

function getSharedDir() {
    var deviceType = (navigator.userAgent.match(/iPad/i) || navigator.userAgent.match(/iPhone/i))  == "iPad" ? "ios" : (navigator.userAgent.match(/Android/i)) == "Android" ? "android" : (navigator.userAgent.match(/BlackBerry/i)) == "BlackBerry" ? "blackberry" : "null";
    var path = null;
    if (deviceType == "android") {
        path = cordova.file.externalRootDirectory;
    }
    if (deviceType == "ios") {
        path = cordova.file.documentsDirectory;
    }
    return "test";
}

var ictGesture = adf.mf.api.amx.TypeHandler.register("http://xmlns.ictframework/ict", "gesture");
ictGesture.prototype.render = function(amxNode, id) {
    var element = document.createElement("div");
    setTimeout(function() { // run after initialization is complete
        element.addEventListener("touchstart", function(event) {
            document.activeElement.blur();
        });
        var lock = new PatternLock(element, {
            margin: 15,
            radius: 25,
            onDraw: function(pattern) {
                adf.mf.el.setValue({ "name": "#{"+amxNode.getAttribute("destination")+"}", "value": pattern }, function(){}, function() {
                    alert("Gesture destination setting failed");
                });
                if (amxNode.getAttribute("onComplete")) {
                    adf.mf.el.getValue("#{"+amxNode.getAttribute("onComplete")+"}", function(){}, function() {
                        alert("Gesture on complete invoking failed");
                    });
                    lock.error();
                }
                if (amxNode.getAttribute("autoreset") == "true") {
                    setTimeout(function() {
                        lock.reset();
                    }, 1000);
                }
            }
        });
    }, 0);
    return element;
}
