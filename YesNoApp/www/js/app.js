// i-CT Starter App

angular.module('yesno', ['ionic', 'ionic.service.core', 'ngCordova', 'ionic-color-picker', 'jett.ionic.filter.bar', 'yesno.controllers', 'yesno.database', 'yesno.copier', 'yesno.playing', 'yesno.ict', 'yesno.options', 'ionic.closePopup', 'clickForOptions', 'ngMaterial'])

.config(function($stateProvider, $urlRouterProvider, $ionicFilterBarConfigProvider, $mdThemingProvider, $mdGestureProvider) {
  $stateProvider

  .state('app', {
    url: '/app',
    abstract: true,
    templateUrl: 'templates/menu.html',
    controller: 'AppCtrl'
  })

  .state('app.options', {
      url: '/options/:type',
      views: {
        'menuContent': {
          templateUrl: 'templates/options.html',
          controller: 'OptionsCtrl'
        }
      }
  })
  
  .state('app.option', {
      url: '/option/:optionId',
      views: {
        'menuContent': {
          templateUrl: 'templates/option.html',
          controller: 'OptionCtrl'
        }
      }
  })
  
  .state('app.lists', {
      url: '/lists/:type',
      views: {
        'menuContent': {
          templateUrl: 'templates/lists.html',
          controller: 'ListsCtrl'
        }
      }
  })
  
  .state('app.listedit', {
      url: '/listedit/:listId',
      views: {
        'menuContent': {
          templateUrl: 'templates/listedit.html',
          controller: 'ListEditCtrl'
        }
      }
  })
  
  .state('app.listitems', {
      url: '/listitems/:listId',
      views: {
        'menuContent': {
          templateUrl: 'templates/listitems.html',
          controller: 'ListItemsCtrl'
        }
      }
  })
  
  .state('app.selectoption', {
      url: '/selectoption/:destination/:id/:side',
      views: {
        'menuContent': {
          templateUrl: 'templates/selectoption.html',
          controller: 'OptionsCtrl'
        }
      }
  })
  
  .state('app.servers', {
      url: '/servers',
      views: {
        'menuContent': {
          templateUrl: 'templates/servers.html',
          controller: 'ServersCtrl'
        }
      }
  })
  
  .state('app.serveredit', {
      url: '/serveredit/:serverId',
      views: {
        'menuContent': {
          templateUrl: 'templates/serveredit.html',
          controller: 'ServerEditCtrl'
        }
      }
  })
  
  .state('app.servercards', {
      url: '/servercards/:serverId',
      views: {
        'menuContent': {
          templateUrl: 'templates/servercards.html',
          controller: 'ServerCardsCtrl'
        }
      }
  })
  
  .state('app.serverlists', {
      url: '/serverlists/:serverId',
      views: {
        'menuContent': {
          templateUrl: 'templates/serverlists.html',
          controller: 'ServerListsCtrl'
        }
      }
  })
  
  .state('app.about', {
      url: '/about',
      views: {
        'menuContent': {
          templateUrl: 'templates/about.html'
        }
      }
  })
  
  .state('playing', {
    url: '/playing/:listId',
    templateUrl: 'templates/playing.html',
    controller: 'PlayingCtrl'
  });
  
  // if none of the above states are matched, use this as the fallback
  $urlRouterProvider.otherwise('/app/lists/private');
  
  $ionicFilterBarConfigProvider.placeholder('Hledat...');
  
  $mdThemingProvider.theme('default')
    .primaryPalette('orange')
    .accentPalette('orange');
  
  $mdGestureProvider.skipClickHijack(); // https://github.com/driftyco/ionic/issues/1022
})

.run(function($ionicPlatform, ictService, $cordovaStatusbar, Persistence) {
  
  ictService.initIctApp();
  
  $ionicPlatform.ready(function() {
    console.log("ionic ready");
    
    var hideStatusbar = function() {
      if (window.StatusBar) {
        console.log("hiding statubar...");
        window.StatusBar.hide();
      }
    };
    
    hideStatusbar();
    
    Persistence.init();
    
    if (window.cordova && window.cordova.plugins.Keyboard) {
      console.log("keyboard setting...");
      cordova.plugins.Keyboard.hideKeyboardAccessoryBar(true);
      cordova.plugins.Keyboard.disableScroll(true);
      
      window.addEventListener('native.keyboardshow', hideStatusbar);
      window.addEventListener('native.keyboardhide', hideStatusbar);
    }
    
  });
  
});

