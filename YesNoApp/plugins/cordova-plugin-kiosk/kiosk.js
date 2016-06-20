
var exec = require('cordova/exec');

var KioskPlugin = {
    
    exitKiosk: function () {
        exec(null, null, "KioskPlugin", "exitKiosk", []);
    },
    
    isInKiosk: function (callback) {
        if(/ios|iphone|ipod|ipad/i.test(navigator.userAgent)) {
            callback(false);
            return;
        }
        exec(function(out){
            callback(out == "true");
        }, function(error){
            alert("KioskPlugin.isInKiosk failed: " + error);
        }, "KioskPlugin", "isInKiosk", []);
    }
    
}

module.exports = KioskPlugin;

