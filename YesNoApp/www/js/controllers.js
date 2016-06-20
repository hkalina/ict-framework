
angular.module('yesno.controllers', [])

.controller('AppCtrl', function($scope, $rootScope, $timeout, ictService, Persistence) {
  $scope.escapeFromManage = function() {
    $timeout(function() { // wait to menu close
      ictService.escapeApp({ fromManage: true });
    }, 50);
  };
  $scope.installIntoIct = function() {
    if (confirm("Opravdu přidat aplikaci do nabídky i-CT frameworku? (Pokud je na zařízení instalován)")) {
      ictService.escapeApp({ action: "register", name: "Aplikace ANO/NE", scheme: "yesno" });
    }
  };
  $rootScope.$watch('remainingTime', function() {
    $scope.isRemainingTime = $rootScope.remainingTime && $rootScope.remainingTime > 0;
    $scope.remainingTime = Math.floor($rootScope.remainingTime/60) + ":" + $rootScope.remainingTime%60;
  });
  
  $rootScope.refreshServers = function() {
    Persistence.allServersOfUser(($rootScope.urlParams && $rootScope.urlParams.loggedId) ? $rootScope.urlParams.loggedId : 0).then(function(servers) {
      $rootScope.servers = servers;
    });
  };
  $rootScope.$on('database-ready', function(){ $rootScope.refreshServers(); });
  $rootScope.$on('servers-changed', function(){ $rootScope.refreshServers(); });
  $rootScope.$watch('urlParams', function(){ $rootScope.refreshServers(); });
})

.controller('ListsCtrl', function($scope, $rootScope, $state, Persistence, $ionicFilterBar, $ionicPopup, Copier) {
  
  $scope.type = $state.params.type;
  $scope.userId = 0;
  
  $scope.refreshLists = function() {
    console.log("refreshLists");
    $scope.type = $state.params.type;
    $scope.userId = ($rootScope.urlParams && $rootScope.urlParams.userId) ? $rootScope.urlParams.userId : 0;
    $scope.thumbnails = [];
    
    var listsPromis = $state.params.type == 'shared' ? Persistence.allListsShared() : Persistence.allListsOfUser($scope.userId);
    
    listsPromis.then(function(lists) {
      console.log("thumbnails obtaining1");
      for (i = 0; i < lists.length; i++) (function(i) {
        lists[i].thumbnails = [];
        lists[i].pairs.prefetch("left").prefetch("right").limit(2).each(null, function(pair){
          if (pair.left && pair.left.image) lists[i].thumbnails.push(pair.left.image);
          if (pair.right && pair.right.image) lists[i].thumbnails.push(pair.right.image);
          $scope.$apply();
        });
      }) (i);
      $scope.lists = lists;
      $scope.listsFiltered = lists;
    });
  };
  $scope.$on('$ionicView.enter', $scope.refreshLists);
  $rootScope.$on('lists-changed', $scope.refreshLists);
  $rootScope.$watch('urlParams', $scope.refreshLists);
  
  $scope.removeList = function(list) {
    $ionicPopup.show({
        title: 'Opravdu odstranit seznam párů?',
        scope: $scope,
        buttons: [
          { text: 'Zrušit' },
          {
            text: '<b>Smazat</b>',
            type: 'button-assertive',
            onTap: function(e) {
              Persistence.remove(list, function() {
                $rootScope.$broadcast('lists-changed');
              });
            }
          }
        ]
    });
  };
  
  $scope.copyList = function(list) {
    Copier.showCopyListModal($scope, list, true);
  }
  
  $scope.search = function() {
    $ionicFilterBar.show({
      cancelText: "Zrušit",
      items: $scope.lists,
      update: function(filteredItems, filterText) {
        $scope.listsFiltered = filteredItems;
      },
      cancel: function() {
        $scope.listsFiltered = $scope.lists;
      },
      expression: function(filterText, value, index, array) {
        var x = filterText.toLowerCase();
        return (value.name && value.name.toLowerCase().indexOf(x) > -1);
      }
    });
  };
})

.controller('ListEditCtrl', function($scope, $rootScope, $state, $ionicHistory, Persistence, ictService) {
  
  if ($state.params.listId == "newprivate") {
    $scope.addingInProgress = true;
    $scope.list = new Persistence.entities.List({
      user: ($rootScope.urlParams && $rootScope.urlParams.userId) ? $rootScope.urlParams.userId : 0,
      owner: ($rootScope.urlParams && $rootScope.urlParams.loggedId) ? $rootScope.urlParams.loggedId : 0,
      shared: 0
    });
  } else if ($state.params.listId == "newshared") {
    $scope.addingInProgress = true;
    $scope.list = new Persistence.entities.List({
      user: ($rootScope.urlParams && $rootScope.urlParams.userId) ? $rootScope.urlParams.userId : 0,
      owner: ($rootScope.urlParams && $rootScope.urlParams.loggedId) ? $rootScope.urlParams.loggedId : 0,
      shared: 1
    });
  } else {
    $scope.addingInProgress = false;
    Persistence.loadList($state.params.listId).then(function(response) {
      $scope.list = response;
    });
  }
    
  $scope.saveList = function() {
    if ($scope.addingInProgress) {
      Persistence.add($scope.list, function() {
        $rootScope.$broadcast('lists-changed');
        $ionicHistory.goBack();
      });
    } else {
      Persistence.flush(function() {
        $rootScope.$broadcast('lists-changed');
        $ionicHistory.goBack();
      });
    }
  };
  
})

.controller('ListItemsCtrl', function($scope, $rootScope, $state, Persistence) {
  
  Persistence.loadList($state.params.listId).then(function(response) {
    $scope.list = response;
    $scope.refreshPairs();
  });
  
  $scope.refreshPairs = function() {
    Persistence.pairsOfList($scope.list).then(function(response) {
      $scope.pairs = response;
    });
  };
  $rootScope.$on('listitems-changed', $scope.refreshPairs);
  
  $scope.addListItem = function() {
    Persistence.add(new Persistence.entities.Pair({
      list: $scope.list
    }), function() {
      $rootScope.$broadcast('listitems-changed');
      $state.go("app.listitems");
    });
  };
  
  $scope.removePair = function(pair) {
    Persistence.remove(pair, function() {
      $rootScope.$broadcast('listitems-changed');
    });
  };
  
})

.controller('ServersCtrl', function($scope, $rootScope, $state, Persistence, $ionicFilterBar, $ionicPopup) {
  
  $scope.refreshServers = function() {
    Persistence.allServersOfUser(($rootScope.urlParams && $rootScope.urlParams.loggedId) ? $rootScope.urlParams.loggedId : 0).then(function(servers) {
      $scope.servers = servers;
    });
  };
  $scope.$on('$ionicView.enter', $scope.refreshServers);
  $rootScope.$on('servers-changed', $scope.refreshServers);
  
  $scope.demo = function() {
    Persistence.add(new Persistence.entities.Server({
      owner: ($rootScope.urlParams && $rootScope.urlParams.loggedId) ? $rootScope.urlParams.loggedId : 0,
      name: "Demo",
      url: "https://ict-honza889.rhcloud.com/",
      username: "demo",
      password: "kpimwG.f2xd"
    }), function() {
      $rootScope.$broadcast('servers-changed');
    });
  };
  
  $scope.removeServer = function(server) {
    $ionicPopup.show({
        title: 'Opravdu odstranit server?',
        scope: $scope,
        buttons: [
          { text: 'Zrušit' },
          {
            text: '<b>Smazat</b>',
            type: 'button-assertive',
            onTap: function(e) {
              Persistence.remove(server, function() {
                $rootScope.$broadcast('servers-changed');
              });
            }
          }
        ]
    });
  };
})

.controller('ServerEditCtrl', function($scope, $rootScope, $state, Persistence, ictService) {
  
  if ($state.params.serverId == "new") {
    $scope.server = new Persistence.entities.Server({
      owner: ($rootScope.urlParams && $rootScope.urlParams.loggedId) ? $rootScope.urlParams.loggedId : 0
    });
  } else {
    Persistence.loadServer($state.params.serverId).then(function(response) {
      $scope.server = response;
    });
  }
  
  $scope.saveServer = function() {
    if ($scope.server.url.substring(0,7) != "http://" && $scope.server.url.substring(0,8) != "https://") {
      $scope.server.url = "http://" + $scope.server.url;
    }
    if ($state.params.serverId == "new") {
      Persistence.add($scope.server, function() {
        $rootScope.$broadcast('servers-changed');
        $state.go("app.servers");
      });
    } else {
      Persistence.flush(function() {
        $rootScope.$broadcast('servers-changed');
        $state.go("app.servers");
      });
    }
  };
})

.controller('ServerCardsCtrl', function($scope, $rootScope, $state, Persistence, Copier, $ionicFilterBar, $ionicHistory, $http, $ionicPopup) {
  
  $scope.server = null;
  
  $scope.refreshServerCards = function() {
    Persistence.loadServer($state.params.serverId).then(function(server) {
      $scope.server = server;
      Copier.serverCardsList(server, function(cards) {
        $scope.options = cards;
        $scope.optionsFiltered = cards;
      });
    });
  };
  $scope.$on('$ionicView.enter', $scope.refreshServerCards);
  $rootScope.$on('server-cards-changed', $scope.refreshServerCards);
  
  $scope.copyOption = function(option) {
    Copier.showCopyModal($scope, option, false);
  };
  
  $scope.copyAll = function(option) {
    Copier.showCopyAllModal($scope, $scope.optionsFiltered, false);
  };
  
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
              Copier.serverCardDelete($scope.server, option, function() {
                $rootScope.$broadcast('server-cards-changed');
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
  
})

.controller('ServerListsCtrl', function($scope, $rootScope, $state, Persistence, Copier, $ionicFilterBar, $ionicHistory, $http, $ionicPopup) {
  
  $scope.server = null;
  
  $scope.refreshServerLists = function() {
    Persistence.loadServer($state.params.serverId).then(function(server) {
      $scope.server = server;
      Copier.serverListsList(server, function(cards) {
        $scope.options = cards;
        $scope.optionsFiltered = cards;
      });
    });
  };
  $scope.$on('$ionicView.enter', $scope.refreshServerLists);
  $rootScope.$on('server-lists-changed', $scope.refreshServerLists);
  
  $scope.copyList = function(list) {
    Copier.showCopyListModal($scope, list, false);
  };
  
  $scope.removeList = function(list) {
    $ionicPopup.show({
        title: 'Opravdu odstranit seznam?',
        scope: $scope,
        buttons: [
          { text: 'Zrušit' },
          {
            text: '<b>Smazat</b>',
            type: 'button-assertive',
            onTap: function(e) {
              Copier.serverListDelete($scope.server, list, function() {
                $rootScope.$broadcast('server-lists-changed');
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
  
});

