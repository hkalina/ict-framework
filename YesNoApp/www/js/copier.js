
angular.module('yesno.copier', ['base64'])

.factory('Copier', function($q, $cordovaFileTransfer, $http, Persistence, $rootScope, $ionicPopup, $base64, $ionicLoading, IonicClosePopupService) {
  
  return {
    
    storeFile: function(tempUri, callback) {
      var extension = /[^./]+$/.exec(tempUri);
      var d = new Date();
      var newFileName = d.getTime() + "." + extension;
      console.log("newFileName = " + newFileName);
      var directory = cordova.file.documentsDirectory != null ? cordova.file.documentsDirectory : cordova.file.externalDataDirectory;
      console.log("directory = " + directory);
      $cordovaFileTransfer.download(tempUri, directory + newFileName, {}, true).then(function(fileEntry) {
        console.log(fileEntry);
        callback(fileEntry.nativeURL);
      }, function (error) {
        alert("File store failed: "+error);
        console.log(error);
      });
    },
    
    getAuthorization: function(server) {
      return 'Basic ' + $base64.encode(unescape(encodeURIComponent(server.username + ":" + server.password)));
    },
    
    serverCardsList: function(server, callback) {
      $http.get(server.url+'/cards/list', {
        headers: { 'Authorization': this.getAuthorization(server) }
      }).then(function(resp) {
        var options = [];
        angular.forEach(resp.data, function(value, key) {
          this.push(new Persistence.entities.Option({
            id: value.id,
            name: value.name,
            text: value.text,
            color: value.color,
            image: value.image ? server.url+'/download/' + value.image : null,
            sound: value.sound ? server.url+'/download/' + value.sound : null
          }));
        }, options);
        callback(options);
      }, function(err) {
        alert("Loading failed: "+err.data);
        console.log(err);
      });
    },
    
    serverCardDelete: function(server, card, callback) {
      $http.delete(server.url+'/cards/delete/'+card.id, {
        headers: { 'Authorization': this.getAuthorization(server) }
      }).then(function(resp) {
        callback();
      }, function(err) {
        alert("Odstraňování karty na serveru selhalo: "+err.data);
        console.log(err);
      });
    },
    
    serverListsList: function(server, callback) {
      $http.get(server.url+'/lists/list', {
        headers: { 'Authorization': this.getAuthorization(server) }
      }).then(function(resp) {
        var lists = [];
        angular.forEach(resp.data, function(value, key) {
          this.push({
            id: value.id,
            name: value.name
          });
        }, lists);
        callback(lists);
      }, function(err) {
        alert("Loading failed: "+err.data);
        console.log(err);
      });
    },
    
    serverListDelete: function(server, list, callback) {
      $http.delete(server.url+'/lists/delete/'+list.id, {
        headers: { 'Authorization': this.getAuthorization(server) }
      }).then(function(resp) {
        callback();
      }, function(err) {
        alert("Odstraňování seznamu karet na serveru selhalo: "+err.data);
        console.log(err);
      });
    },
    
    showCopyModal: function(scope, option, localSource) {
      var Copier = this;
      scope.copyDestination = {};
      var onCopyStart = function() {
        console.log("copy start");
        $ionicLoading.show({
          template: '<ion-spinner></ion-spinner><br>Kopírování...',
          scope: scope
        });
      };
      var onProgress = function(progress) {
        console.log("progress");
      };
      var onCopy = function() {
        console.log("copy finished");
        $ionicLoading.hide();
      };
      var copyPopup = $ionicPopup.show({
        title: 'Kam kopírovat?',
        scope: scope,
        template: '<ion-radio ng-model="copyDestination.value" ng-value="\'private\'">Karty asistenta</ion-radio>' +
                  '<ion-radio ng-model="copyDestination.value" ng-value="\'shared\'">Offline katalog karet</ion-radio>' +
                  '<ion-radio ng-model="copyDestination.value" ng-repeat="server in $root.servers" ng-value="server">Online katalog: {{server.name}}</ion-radio>',
        buttons: [
          { text: 'Zrušit' },
          {
            text: '<b>Kopírovat</b>',
            type: 'button-positive',
            onTap: function(e) {
              if (!scope.copyDestination.value) { // not selected
                e.preventDefault(); // dont close
              } else {
                var destination = scope.copyDestination.value;
                var newOwnerId = ($rootScope.urlParams && $rootScope.urlParams.loggedId) ? $rootScope.urlParams.loggedId : 0;
                if (localSource) {
                  if (destination == 'private' || destination == 'shared') {
                    onCopyStart();
                    Copier.copyLocal(option, newOwnerId, destination, onCopy, onProgress);
                  } else {
                    onCopyStart();
                    Copier.uploadOption(option, destination, onCopy, onProgress);
                  }
                } else {
                  if (destination == 'private' || destination == 'shared') {
                    onCopyStart();
                    Copier.downloadOption(option, newOwnerId, destination, onCopy, onProgress);
                  } else {
                    alert("Kopírování mezi servery není podporováno!");
                  }
                }
                $rootScope.$broadcast('options-changed');
              }
            }
          }
        ]
      });
      IonicClosePopupService.register(copyPopup);
    },
    
    showCopyAllModal: function(scope, optionsToCopy, localSource) {
      var Copier = this;
      scope.copyDestination = {};
      var destination = null;
      var newOwnerId = null;
      var options = optionsToCopy.slice();
      
      var copyNext = function() {
        if (options.length == 0) {
          onCopy();
        } else {
          var option = options.pop();
          destination = scope.copyDestination.value;
          newOwnerId = ($rootScope.urlParams && $rootScope.urlParams.loggedId) ? $rootScope.urlParams.loggedId : 0;
          if (localSource) {
            if (destination == 'private' || destination == 'shared') {
              onCopyStart();
              Copier.copyLocal(option, newOwnerId, destination, copyNext, onProgress);
            } else {
              onCopyStart();
              Copier.uploadOption(option, destination, copyNext, onProgress);
            }
          } else {
            if (destination == 'private' || destination == 'shared') {
              onCopyStart();
              Copier.downloadOption(option, newOwnerId, destination, copyNext, onProgress);
            } else {
              alert("Kopírování mezi servery není podporováno!");
            }
          }
        }
      }
      var onCopyStart = function() {
        console.log("copy start");
        $ionicLoading.show({
          template: '<ion-spinner></ion-spinner><br>Kopírování...',
          scope: scope
        });
      };
      var onProgress = function(progress) {
        console.log("progress");
      };
      var onCopy = function() {
        console.log("copy finished");
        $ionicLoading.hide();
        $rootScope.$broadcast('options-changed');
      };
      var copyPopup = $ionicPopup.show({
        title: 'Kam vše kopírovat?',
        scope: scope,
        template: '<ion-radio ng-model="copyDestination.value" ng-value="\'private\'">Karty asistenta</ion-radio>' +
                  '<ion-radio ng-model="copyDestination.value" ng-value="\'shared\'">Offline katalog karet</ion-radio>' +
                  '<ion-radio ng-model="copyDestination.value" ng-repeat="server in $root.servers" ng-value="server">Online katalog: {{server.name}}</ion-radio>',
        buttons: [
          { text: 'Zrušit' },
          {
            text: '<b>Kopírovat</b>',
            type: 'button-positive',
            onTap: function(e) {
              if (!scope.copyDestination.value) { // not selected
                e.preventDefault(); // dont close
              } else {
                copyNext();
              }
            }
          }
        ]
      });
      IonicClosePopupService.register(copyPopup);
    },
    
    copyLocal: function(option, owner, destination, okCallback, progressCallback) {
      var copy = new Persistence.entities.Option({
        name: option.name,
        text: option.text,
        color: option.color,
        image: option.image,
        sound: option.sound,
        owner: owner,
        shared: destination == 'shared'
      });
      Persistence.add(copy, function(newId) {
        console.log("option copied", newId, copy);
        okCallback(copy);
      });
    },
    
    downloadOption: function(option, owner, destination, okCallback, progressCallback) {
      var Copier = this;
      console.log(option);
      console.log(option.image);
      console.log(option.sound);
      
      var downloaded = function() {
        console.log("downloaded, coping");
        Copier.copyLocal(option, owner, destination, okCallback, progressCallback);
      };
      
      var downloadSound = function() {
        if (!option.sound) {
          console.log("skipping sound");
          return downloaded();
        }
        var targetSound = cordova.file.dataDirectory + option.sound.split("/").pop();
        console.log("sound downloading: "+option.sound+" "+targetSound);
        $cordovaFileTransfer.download(option.sound, targetSound, {}, false).then(function(result) {
          option.sound = targetSound;
          return downloaded();
        }, function(err) {
          alert("Sound downloading failed!");
          console.log(err);
        }, function (progress) {
          console.log(progress);
        });
      };
      
      var downloadImage = function() {
        if (!option.image) {
          console.log("skipping image");
          return downloadSound();
        }
        var targetImage = cordova.file.dataDirectory + option.image.split("/").pop();
        console.log("image downloading: "+option.image+" "+targetImage);
        $cordovaFileTransfer.download(option.image, targetImage, {}, false).then(function(result) {
          option.image = targetImage;
          return downloadSound();
        }, function(err) {
          alert("Image downloading failed!");
          console.log(err);
        }, function (progress) {
          console.log(progress);
        });
      };
      
      downloadImage();
    },
    
    uploadOption: function(option, server, okCallback, progressCallback) {
      var Copier = this;
      
      var uploadSound = function(id) {
        if (!option.sound) {
          console.log("skipping sound");
          okCallback(id);
          return;
        }
        $cordovaFileTransfer.upload(server.url+'/cards/attach', option.sound, {
          fileKey: "file",
          headers: { 'Authorization': Copier.getAuthorization(server) },
          params: {
            id: id,
            type: 'sound'
          }
        }).then(function(result) {
          console.log("sound upload result", result);
          okCallback(id);
        }, function(err) {
          alert("Nepodařilo se nahrát zvuk na server!");
          console.log(err);
        }, function (progress) {
          console.log(progress);
        });
      }
      
      var uploadImage = function(id) {
        if (!option.image) {
          console.log("skipping image");
          return uploadSound(id);
        }
        $cordovaFileTransfer.upload(server.url+'/cards/attach', option.image, {
          fileKey: "file",
          headers: { 'Authorization': Copier.getAuthorization(server) },
          params: {
            id: id,
            type: 'image'
          }
        }).then(function(result) {
          console.log("image upload result", result);
          uploadSound(id);
        }, function(err) {
          alert("Nepodařilo se nahrát obrázek na server!");
          console.log(err);
        }, function (progress) {
          console.log(progress);
        });
      }
      
      $http.post(server.url+'/cards/create', {
        name: option.name,
        text: option.text,
        color: option.color
      }, {
        headers: { 'Authorization': Copier.getAuthorization(server) }
      }).then(function(resp) {
        uploadImage(resp.data);
      }, function(err) {
        alert("Vytvoření karty selhalo!");
        console.log(err);
      });
    },
    
    copyListLocal: function(list, newOwnerId, newUserId, destination, onCopyStart, onCopy) {
      var copy = new Persistence.entities.List({ // upload
        name: list.name,
        user: newUserId,
        owner: newOwnerId,
        shared: destination == 'shared'
      });
      
      Persistence.add(copy, function() {
        console.log("added list", copy);
        list.pairs.prefetch("left").prefetch("right").list(function (pairs) { // prefetch left, right?
          for (i = 0; i < pairs.length; i++) {
            copy.pairs.add(new Persistence.entities.Pair({
              left: pairs[i].left,
              right: pairs[i].right
            }));
          }
          Persistence.flush(function() {
            console.log("flushed");
            onCopy(copy);
          });
        });
      });
    },
    
    copyListUpload: function(list, newOwnerId, newUserId, server, onCopyStart, onCopy, onProgress) {
      var Copier = this;
      var progressAll = 0;
      var progressProcessed = 0;
      
      var uploadPairCards = function (pair, callback) {
        console.log("coping pairs cards...", pair);
        if (pair.left && pair.right) {
          console.log("has left+right, uploading left...");
          Copier.uploadOption(pair.left, server, function(leftId){
            console.log("uploading right...");
            Copier.uploadOption(pair.right, server, function(rightId){
              console.log("pair cards uploaded");
              callback(leftId, rightId);
            }, function(){});
          }, function(){});
          return;
        }
        if (pair.left) {
          console.log("has left");
          Copier.uploadOption(pair.left, server, function(leftId){
            console.log("pair card uploaded");
            callback(leftId, null);
          }, function(){});
          return;
        }
        if (pair.right) {
          console.log("has right");
          Copier.uploadOption(pair.right, server, function(rightId){
            console.log("pair card uploaded");
            callback(null, rightId);
          }, function(){});
          return;
        }
        callback(null, null); // nothing to copy
      };
      
      var uploadPairs = function(pairs, listId, callback) {
        if (pairs.length == 0) {
          callback();
          return;
        }
        var pair = pairs.pop();
        
        uploadPairCards(pair, function(leftId, rightId){
          console.log("uploadPairCards success", listId, leftId, rightId);
          $http.post(server.url+'/lists/addpair', {
            id: listId,
            leftId: leftId,
            rightId: rightId
          }, {
            headers: { 'Authorization': Copier.getAuthorization(server) }
          }).then(function(resp) {
            console.log("list addpair success", resp);
            onProgress(progressAll, ++progressProcessed);
            uploadPairs(pairs, listId, callback);
          }, function(err) {
            alert("Vytvoření páru na serveru selhalo: "+err);
            console.log(err);
          });
        });
      };
      
      $http.post(server.url+'/lists/create', {
        name: list.name
      }, {
        headers: { 'Authorization': Copier.getAuthorization(server) }
      }).then(function(resp) {
        list.pairs.prefetch("left").prefetch("right").list(function(pairs) {
          progressAll = pairs.length;
          uploadPairs(pairs, resp.data, onCopy);
        });
      }, function(err) {
        alert("Vytvoření seznamu na serveru selhalo: "+err);
        console.log(err);
      });
    },
    
    copyListDownload: function(list, newOwnerId, newUserId, server, destination, onCopyStart, onCopy, onProgress) {
      var Copier = this;
      var progressAll = 0;
      var progressProcessed = 0;
      var newList = null;
      
      var downloadCards = function(pair, callback) {
        console.log("downloadCards ", pair.left, pair.right);
        
        if (pair.left && pair.left.image) pair.left.image = server.url+'/download/'+pair.left.image;
        if (pair.right && pair.right.image) pair.right.image = server.url+'/download/'+pair.right.image;
        if (pair.left && pair.left.sound) pair.left.sound = server.url+'/download/'+pair.left.sound;
        if (pair.right && pair.right.sound) pair.right.sound = server.url+'/download/'+pair.right.sound;
        
        if (pair.left && pair.right) {
          console.log("left+right");
          Copier.downloadOption(pair.left, newOwnerId, destination, function(left) {
            Copier.downloadOption(pair.right, newOwnerId, destination, function(right) {
              callback(left, right);
            }, function(){});
          }, function(){});
          return;
        }
        
        if (pair.left) {
          console.log("left only");
          Copier.downloadOption(pair.left, newOwnerId, destination, function(left) {
            callback(left, null);
          }, function(){});
          return;
        }
        
        if (pair.right) {
          console.log("right only");
          Copier.downloadOption(pair.right, newOwnerId, destination, function(right) {
            callback(null, right);
          }, function(){});
          return;
        }
        
        console.log("no left, no right");
        callback(null, null);
      };
      
      var downloadNextPair = function(pairs) {
        if (pairs.length == 0) {
         
          console.log("flushing...");
          Persistence.flush(function() {
            console.log("flushed");
            onCopy();
          });
         
        } else {
          var pair = pairs.pop();
          console.log("downloading pair", pair);
         
          downloadCards(pair, function(left, right) {
            newList.pairs.add(new Persistence.entities.Pair({
              left: left,
              right: right
            }));
            console.log("added into pairs of newList");
            onProgress(progressAll, ++progressProcessed);
            downloadNextPair(pairs);
          });
        }
      };
      
      newList = new Persistence.entities.List({ // download
        name: list.name,
        user: newUserId,
        owner: newOwnerId,
        shared: destination == 'shared'
      });
      console.log("adding list", newList);
      Persistence.add(newList, function() {
        console.log("created list "+newList.id, newList);
        
        $http.get(server.url+'/lists/get/'+list.id, {
          headers: { 'Authorization': Copier.getAuthorization(server) }
        }).then(function(resp) {
          console.log("list get: ", resp.data);
          progressAll = resp.data.length;
          downloadNextPair(resp.data);
        }, function(err) {
          alert("Loading failed: "+err.data);
          console.log(server.url+'/lists/get/'+list.id);
          console.log(err);
        });
        
      });
      
    },
    
    copyList: function(list, newOwnerId, newUserId, localSource, server, destination, onCopyStart, onCopy, onProgress) {
      var Copier = this;
      console.log("coping...");
      
      if (localSource) {
        if (destination == 'private' || destination == 'shared') {
          onCopyStart();
          Copier.copyListLocal(list, newOwnerId, newUserId, destination, onCopyStart, onCopy);
        } else {
          onCopyStart();
          Copier.copyListUpload(list, newOwnerId, newUserId, destination, onCopyStart, onCopy, onProgress);
        }
      } else {
        if (destination == 'private' || destination == 'shared') {
          onCopyStart();
          Copier.copyListDownload(list, newOwnerId, newUserId, server, destination, onCopyStart, onCopy, onProgress);
        } else {
          alert("Kopírování mezi servery není podporováno!");
        }
      }
    },
    
    showCopyListModal: function(scope, list, localSource) {
      var Copier = this;
      scope.copyDestination = {};
      scope.progress = "";
      var onCopyStart = function() {
        console.log("copy start");
        $ionicLoading.show({
          template: '<ion-spinner></ion-spinner><br>Kopírování... {{progress}}',
          scope: scope
        });
      };
      var onProgress = function(total, processed) {
        console.log("list upload progress: ", total, processed);
        scope.progress = processed + "/" + total;
      };
      var onCopy = function() {
        console.log("copy finished");
        $ionicLoading.hide();
        $rootScope.$broadcast('lists-changed');
      };
      var copyPopup = $ionicPopup.show({
        title: 'Kam kopírovat?',
        scope: scope,
        template: '<ion-radio ng-model="copyDestination.value" ng-value="\'private\'">Seznamy párů klienta</ion-radio>' +
                  '<ion-radio ng-model="copyDestination.value" ng-value="\'shared\'">Katalog seznamů párů</ion-radio>' +
                  '<ion-radio ng-model="copyDestination.value" ng-repeat="server in $root.servers" ng-value="server">Online katalog: {{server.name}}</ion-radio>',
        buttons: [
          { text: 'Zrušit' },
          {
            text: '<b>Kopírovat</b>',
            type: 'button-positive',
            onTap: function(e) {
              if (!scope.copyDestination.value) { // not selected
                e.preventDefault(); // dont close
              } else {
                var destination = scope.copyDestination.value;
                var newOwnerId = ($rootScope.urlParams && $rootScope.urlParams.loggedId) ? $rootScope.urlParams.loggedId : 0;
                var newUserId = ($rootScope.urlParams && $rootScope.urlParams.userId) ? $rootScope.urlParams.userId : 0;
                Copier.copyList(list, newOwnerId, newUserId, localSource, scope.server, destination, onCopyStart, onCopy, onProgress);
              }
            }
          }
        ]
      });
      IonicClosePopupService.register(copyPopup);
    }
    
  };
  
});

