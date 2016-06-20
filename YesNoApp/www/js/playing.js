angular.module('yesno.playing', [])

.controller('PlayingCtrl', function($scope, $rootScope, $state, $ionicPlatform, $ionicPopup, $ionicHistory, Persistence, IonicClosePopupService, ictService) {
  
  $scope.data = {};
  $scope.$watch('data.slider', function(nv, ov) {
    if (!$scope.data.slider) return;
    // api: http://idangero.us/swiper/api/
    $scope.data.slider.params.threshold = 180;
    $scope.data.slider.params.longSwipesRatio = 0.1;
  });
  
  $scope.doEscape = function() {
    window.history.back();
  }
  
  $scope.onBackButton = $scope.doEscape;
  
  $scope.$on('$ionicView.enter', function() {
    if ($rootScope.urlParams && !$rootScope.urlParams.manage && $rootScope.isInKiosk) {
      $scope.onBackButton = $scope.escapeGesture;
      $scope.deregisterFunction = $ionicPlatform.registerBackButtonAction(function(event) {
        $scope.escapeGesture();
      }, 120);
    }
    Persistence.loadList($state.params.listId).then(function(response) {
      $scope.list = response;
      $scope.refreshPairs();
    });
  });
  
  $scope.textColorByBackground = function(backgroundColor) {
    var hsl = new Color(backgroundColor).hslData();
    return hsl[2] < 0.5 ? '#FFF' : '#000';
  };
  
  $scope.refreshPairs = function() {
    Persistence.pairsOfList($scope.list).then(function(response) {
      $scope.pairs = response;
      $scope.pair = $scope.pairs[0];
    });
  };
  
  $scope.slidesOptions = {
    effect: 'slide',
    initialSlide: 1,
    loop: true,
    onInit: function(slides) {
      $scope.slides = slides;
    },
    onSlideChangeEnd: function(slides) {
      console.log('The active slide is ' + slides.activeIndex);
    }
  };
  
  $scope.media = null;
  $scope.playing = false;
  
  $scope.playAudio = function(option) {
    console.log(option);
    if ($scope.media) $scope.media.stop(); // stop if already playing
    if (!option.sound) return;
    var url = decodeURIComponent(option.sound.replace('file:/', '/'));
    $scope.media = new Media(url, function(result) {
      console.log(result);
    }, function(error) {
      console.log("Chyba přehrávání zvuku:", error);
      if(error.code) alert("Zvuk nelze přehrát: "+error.code+" "+error.message);
      $scope.$apply(function() {
        $scope.playing = false;
      });
    }, function(status) {
      console.log("playing status:");
      console.log(status);
      if (status == 4){
        $scope.$apply(function() {
          $scope.playing = false;
        });
      }
    });
    console.log("starting playing...");
    $scope.playing = true;
    $scope.media.play();
    console.log("playing started");
  };
  
  $scope.onGesturePopupRender = function() {
    // see http://ignitersworld.com/lab/patternLock.html
    var lock = new PatternLock("#gesture", {
      margin: 10,
      radius: 25,
      onDraw: function(pattern){
        if ($rootScope.urlParams.loggedGesture == sha1(pattern)) {
          $scope.deregisterFunction();
          $scope.doEscape();
          $scope.gesturePopup.close();
        } else {
          lock.error();
          $scope.gesturePopup.close();
        }
      }
    });
  }
  
  $scope.escapeGesture = function() {
    $scope.gesturePopup = $ionicPopup.show({
          template: '<div id="gesture" ng-init="onGesturePopupRender()"></div>',
          title: 'Zadejte gesto asistenta',
          cssClass: 'gesturePopup',
          scope: $scope
    });
    IonicClosePopupService.register($scope.gesturePopup);
  };
  
});
