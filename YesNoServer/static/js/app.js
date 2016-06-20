angular.module('yesnoweb', ['ui.router', 'ngResource', 'ngFileUpload'])

.config(function($stateProvider, $urlRouterProvider) {
  $stateProvider

  .state('cards', {
      url: '/cards',
      views: {
        'content': {
          templateUrl: 'templates/cards.html',
          controller: 'CardsCtrl'
        }
      }
  })
  
  .state('card', {
      url: '/card/:cardId',
      views: {
        'content': {
          templateUrl: 'templates/card.html',
          controller: 'CardCtrl'
        }
      }
  })
  
  .state('lists', {
      url: '/lists',
      views: {
        'content': {
          templateUrl: 'templates/lists.html',
          controller: 'ListsCtrl'
        }
      }
  })
  
  .state('list', {
      url: '/list/:listId',
      views: {
        'content': {
          templateUrl: 'templates/list.html',
          controller: 'ListCtrl'
        }
      }
  });
  
  // if none of the above states are matched, use this as the fallback
  $urlRouterProvider.otherwise('/cards');
})

.run(function($state) {
  console.log("run app");
})

.controller('CardsCtrl', function($scope, $resource) {
  console.log('cards ctr');
  $resource('/cards/list', {}).query(function(response) {
    $scope.cards = response;
    console.log($scope.cards);
  });
})

.controller('ListsCtrl', function($scope, $resource) {
  console.log('lists ctr');
  $resource('/lists/list', {}).query(function(response) {
    $scope.lists = response;
    console.log($scope.lists);
  });
})

.controller('ListCtrl', function($scope, $resource, $stateParams) {
  console.log('lists ctr');
  $resource('/lists/get/'+$stateParams.listId, {}).query(function(response) {
    $scope.pairs = response;
    console.log($scope.pairs);
  });
})

.controller('CardCtrl', function($scope, Upload, $resource, $http, $stateParams, $timeout, $location) {
  
  $scope.card = {};
  
  console.log("loading "+$stateParams.cardId);
  if ($stateParams.cardId != "new") {
    $http.get('/cards/get/'+$stateParams.cardId).then(function(resp) {
      console.log(resp.data);
      $scope.card = resp.data;
    }, function(err) {
      alert("Loading failed");
      console.log(err);
    });
  }
  
  $scope.save = function() {
    if ($scope.card.id) {
      $http.post('/cards/update', {
        id: $scope.card.id,
        name: $scope.card.name,
        text: $scope.card.text,
        color: $scope.card.color
      }).then(function(resp) {
        $location.path("/card/"+resp.data);
      }, function(err) {
        alert("Updating failed");
        console.log(err);
      });
    } else {
      $http.post('/cards/create', {
        name: $scope.card.name,
        text: $scope.card.text,
        color: $scope.card.color
      }).then(function(resp) {
        console.log("resp:",resp);
        $location.path("/card/"+resp.data);
      }, function(err) {
        alert("Creating failed");
        console.log(err);
      });
    }
  };
  
  $scope.uploadFile = function(file, errFiles, type) {
    $scope.f = file;
    $scope.errFile = errFiles && errFiles[0];
    if (file) {
      file.upload = Upload.upload({
        url: '/cards/attach',
        data: {
          file: file,
          'type': type,
          'id': $scope.card.id
        }
      });
      file.upload.then(function (response) {
        $timeout(function() {
          file.result = response.data;
        });
      }, function (response) {
        if (response.status > 0) $scope.errorMsg = response.status + ': ' + response.data;
      }, function (evt) {
        file.progress = Math.min(100, parseInt(100.0 * evt.loaded / evt.total));
      });
    };
  };
  
  $scope.delete = function(card) {
    console.log("removing "+card.id);
    $http.delete('/cards/delete/' + card.id).then(function(resp) {
      $location.path("/cards");
    }, function(err) {
      alert("Removing failed");
      console.log(err);
    });
  };
  
});

