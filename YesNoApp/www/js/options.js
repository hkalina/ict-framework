
angular.module('yesno.options', ['yesno.color'])

.controller('OptionsCtrl', function($scope, $rootScope, $state, Persistence, Copier, $ionicFilterBar, $ionicHistory, $ionicPopup) {
  
  $scope.type = $state.params.type;
  $scope.loggedId = 0;
  
  $scope.refreshOptions = function() {
    $scope.type = $state.params.type;
    $scope.loggedId = ($rootScope.urlParams && $rootScope.urlParams.loggedId) ? $rootScope.urlParams.loggedId : 0;
    if ($state.params.type == 'shared') {
      Persistence.sharedOptions().then(function(response) {
        $scope.options = response;
        $scope.optionsFiltered = response;
      });
    } else {
      Persistence.privateOptions($scope.loggedId).then(function(response) {
        $scope.options = response;
        $scope.optionsFiltered = response;
      });
    }
  };
  $scope.$on('$ionicView.enter', $scope.refreshOptions);
  $rootScope.$on('options-changed', $scope.refreshOptions);
  
  $scope.copyOption = function(option) {
    Copier.showCopyModal($scope, option, true);
  }
  
  $scope.copyAll = function(option) {
    Copier.showCopyAllModal($scope, $scope.optionsFiltered, true);
  }
  
  $scope.removeOption = function(option) {
    $ionicPopup.show({
        title: 'Opravdu odstranit kartu?',
        scope: $scope,
        buttons: [
          { text: 'Zrušit' },
          {
            text: '<b>Smazat</b>',
            type: 'button-assertive',
            onTap: function(e) {
              Persistence.remove(option, function() {
                $rootScope.$broadcast('options-changed');
              });
            }
          }
        ]
    });
  };
  
  $scope.search = function() {
    $ionicFilterBar.show({
      cancelText: "Zrušit",
      items: $scope.options,
      update: function(filteredItems, filterText) {
        $scope.optionsFiltered = filteredItems;
      },
      cancel: function() {
        $scope.optionsFiltered = $scope.options;
      },
      expression: function(filterText, value, index, array) {
        var x = filterText.toLowerCase();
        return (value.name && value.name.toLowerCase().indexOf(x) > -1) ||
               (value.text && value.text.toLowerCase().indexOf(x) > -1);
      }
    });
  };
  
  $scope.selectOption = function(option) {
    if ($state.params.destination == 'pair') {
      Persistence.loadPair($state.params.id).then(function(response) {
        if ($state.params.side == 'right') {
          response.right = option;
        } else {
          response.left = option;
        }
        Persistence.flush(function() {
          $rootScope.$broadcast('listitems-changed');
          $ionicHistory.goBack();
        });
      });
    }
  };
  
})

.controller('OptionCtrl', function($scope, $rootScope, $state, Persistence, Color, $cordovaCapture, $cordovaImagePicker, $cordovaMedia, $cordovaCamera, $ionicHistory, Copier) {
  
  if ($state.params.optionId == "newprivate") {
    $scope.addingInProgress = true;
    $scope.option = new Persistence.entities.Option({
      owner: ($rootScope.urlParams && $rootScope.urlParams.loggedId) ? $rootScope.urlParams.loggedId : 0,
      shared: false
    });
  } else if ($state.params.optionId == "newshared") {
    $scope.addingInProgress = true;
    $scope.option = new Persistence.entities.Option({
      owner: ($rootScope.urlParams && $rootScope.urlParams.loggedId) ? $rootScope.urlParams.loggedId : 0,
      shared: true
    });
  } else {
    $scope.addingInProgress = false;
    Persistence.loadOption($state.params.optionId).then(function(response) {
      $scope.option = response;
    });
  }
  
  $scope.saveOption = function() {
    if ($scope.addingInProgress) {
      Persistence.add($scope.option, function() {
        $ionicHistory.goBack();
        $rootScope.$broadcast('options-changed');
      });
    } else {
      Persistence.flush(function() {
        $ionicHistory.goBack();
        $rootScope.$broadcast('options-changed');
      });
    }
  };
  
  $scope.pickImage = function() {
    var options = {
      destinationType: Camera.DestinationType.FILE_URI,
      sourceType: Camera.PictureSourceType.PHOTOLIBRARY
    };
    $cordovaCamera.getPicture(options).then(function(tempUri) {
      console.log('tempUri', tempUri); // content://media/external/images/media/...
      Copier.storeFile(decodeURI(tempUri), function(storedUrl) {
        $scope.option.image = storedUrl;
      });
    }, function(error) {
      console.log(error);
      if (error.indexOf("cancel") <= -1 && error.indexOf("no image selected") <= -1) alert("Image pick failed: " + error);
    });
  };
  
  $scope.pickSound = function() {
    window.plugins.mediapicker.getAudio(function(data)  {
      console.log(data);
      if (data.constructor === Array) {
        if (data.length == 0) {
          alert('Nebyl vybrán žádný soubor zvuku!');
          return;
        }
        data = data[0];
      }
      console.log(data.exportedurl);
      Copier.storeFile(decodeURI(data.exportedurl), function(storedUrl) {
        $scope.option.sound = storedUrl;
        console.log("value = " + $scope.option.sound);
      });
    }, function(error) {
      console.log(error);
      if (error.indexOf("cancel") <= -1) alert("Sound pick failed: " + error);
    }, false, false); // not multiselect, dont show icloud songs
  };
  
  $scope.pickColor = function() {
    Color.showPicker($scope, $scope.option.color, function(c) {
      $scope.option.color = c;
    });
  }
  
  $scope.captureImage = function() {
    $cordovaCapture.captureImage().then(function(data) {
      var tempUri = decodeURI(data[0].fullPath); // android: file:/storage/.../...png ios: /var/mobile/...jpg
      if (tempUri.substring(0,6) != 'file:/') tempUri = 'file://' + tempUri;
      console.log("image is here: "+tempUri);
      Copier.storeFile(tempUri, function(storedUrl) {
        $scope.option.image = storedUrl;
      });
    }, function(error) {
      console.log(error);
      if(error.code != 3) alert("Image capture failed: "+error);
    });
  };
  
  $scope.captureSound = function() {
    $cordovaCapture.captureAudio().then(function(data) {
      var tempUri = decodeURI(data[0].fullPath); // android: file:/storage/.../...wav  ios: /var/...wav
      if (tempUri.substring(0,6) != 'file:/') tempUri = 'file://' + tempUri;
      console.log("sound is here: "+tempUri);
      Copier.storeFile(tempUri, function(storedUrl) {
        $scope.option.sound = storedUrl;
        console.log("value := " + $scope.option.sound);
      });
    }, function(error) {
      console.log(error);
      if(error.code != 3) alert("Audio capture failed: "+error);
    });
  };
  
  $scope.media = null;
  $scope.playing = false;
  
  $scope.playSound = function() {
    var url = decodeURI($scope.option.sound);
    console.log(url);
    if (url.substring(0,8) == 'file:///') url = url.substring(7); // ios
    console.log(url);
    $scope.media = new Media(url, function(result) {
      console.log("playing success");
      console.log(result);
    }, function(error) {
      console.log("playing failed:");
      console.log(error);
      alert("Zvuk nelze přehrát: "+error.message);
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
  
  $scope.stopSound = function() {
    $scope.media.stop();
  };
  
  $scope.removeImage = function() {
    $scope.option.image = null;
  };
  
  $scope.removeSound = function() {
    $scope.option.sound = null;
  };
  
});

