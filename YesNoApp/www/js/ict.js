
// to test manage in inspect: handleOpenURL("yesno://?manage=true");
function handleOpenURL(urlString) {
  var onIonicLoaded = function() {
    var uri = new URI(urlString);
    var $rootScope = angular.element(document.getElementsByTagName("body")[0]).injector().get('$rootScope');
    var search = uri.search(true);
    if($rootScope.urlParams != search){
      $rootScope.$apply(function(){
        $rootScope.urlParams = search;
      });
    }
  }
  setTimeout(onIonicLoaded, 0);
  setTimeout(onIonicLoaded, 200);
  setTimeout(onIonicLoaded, 500);
  setTimeout(onIonicLoaded, 1000);
}

angular.module('yesno.ict', [])

.factory('ictService', function ictServiceFactory($rootScope, $ionicPopup, $ionicPlatform, $interval, $timeout, $state, $location, $ionicHistory, IonicClosePopupService, $ionicSideMenuDelegate) {
  var ictService = {};
  
  ictService.initIctApp = function() {
    
    $rootScope.timeoutInterval = null;
    
    $rootScope.userEscape = function() {
      console.log("user escaping...");
      if (!$rootScope.urlParams || $rootScope.urlParams.manage) {
        ictService.escapeApp({ fromManage: true });
      } else if ($rootScope.urlParams.canEscapeApp == 'true') {
        console.log("because canEscapeApp");
        ictService.escapeApp({ becauseCanEscapeApp: true });
      } else if (!$rootScope.isInKiosk) {
        console.log("because not in kiosk");
        ictService.escapeApp({ becauseNotInKiosk: true });
      } else {
        ictService.escapeGesture();
      }
    };
    
    if (window.KioskPlugin) {
      console.log("KioskPlugin loaded...");
      window.KioskPlugin.isInKiosk(function(isIn) {
        $rootScope.isInKiosk = isIn;
        console.log("isInKiosk = " + isIn);
      });
    } else {
      $rootScope.isInKiosk = false;
      console.log("KioskPlugin not loaded!");
    }
    
    $rootScope.$watch('urlParams', function() {
      if ($rootScope.urlParams) {
        
        //$ionicHistory.clearHistory();
        //$ionicHistory.clearCache();
        $ionicSideMenuDelegate.toggleLeft(false);
        
        $ionicHistory.nextViewOptions({ historyRoot: true, disableAnimate: true, disableBack: true });
        $state.go('app.lists', { type: 'private' });
        $ionicHistory.nextViewOptions({ historyRoot: false, disableAnimate: false, disableBack: false });
        
        $ionicPlatform.registerBackButtonAction(function(event) {
          if ($state.current.name == 'app.lists') {
            $rootScope.userEscape();
          } else {
            $ionicHistory.goBack();
          }
        }, 110);
        
      }
      
      if ($rootScope.urlParams && $rootScope.urlParams.allowedTime > 0) { // allowed time
        $rootScope.remainingTime = $rootScope.urlParams.allowedTime * 60; // initial
        $rootScope.timeoutInterval = $interval(function() { // decrementing
          $rootScope.remainingTime--;
          if ($rootScope.remainingTime == 0) {
            console.log("remaining time eclapsed - timeout escaping");
            ictService.escapeApp({ back: "timeout" });
          }
        }, 1000);
      }
    });
    
    $timeout(function() {
      if (!$rootScope.urlParams) {
        $rootScope.urlParams = { manage: true, notInstalled: true };
      }
    }, 1000);
    
  };
  
  ictService.escapeApp = function(params) {
      if ($rootScope.timeoutInterval) {
        $interval.cancel($rootScope.timeoutInterval);
        $rootScope.remainingTime = -1;
      }
      var uri;
      if ($rootScope.urlParams && $rootScope.urlParams.return) {
        uri = URI($rootScope.urlParams.return + "://");
      } else if (!params) {
        navigator.app.exitApp();
        return;
      } else {
        uri = URI("ictframework://");
      }
      if (params) uri.addSearch(params);
      $rootScope.urlParams = null;
      var url = uri.build();
      console.log("go to " + url);
      window.location.href = url;
  };
  
  $rootScope.onGesturePopupRender = function() {
      // see http://ignitersworld.com/lab/patternLock.html
      var lock = new PatternLock("#gesture", {
        margin: 10,
        radius: 25,
        onDraw: function(pattern){
          if ($rootScope.urlParams.loggedGesture == sha1(pattern)) {
            ictService.gesturePopup.close();
            ictService.escapeApp({ afterGesture: true });
          } else {
            lock.error();
            ictService.gesturePopup.close();
          }
        }
      });
  };
  
  ictService.escapeGesture = function() {
    if ($rootScope.urlParams.loggedGesture) {
      ictService.gesturePopup = $ionicPopup.show({
          template: '<div id="gesture" ng-init="$root.onGesturePopupRender()"></div>',
          title: 'Zadejte gesto asistenta',
          cssClass: 'gesturePopup',
          scope: $rootScope
      });
      IonicClosePopupService.register(ictService.gesturePopup);
    } else {
      alert("Poznámka: Gesto asistenta není požadováno, protože není nastaveno.");
      ictService.escapeApp({ afterGesture: true });
    }
  };
  
  return ictService;
});

