// inspired by https://github.com/bgoetzmann/ionic-persistence/blob/master/www/js/services.js

angular.module('yesno.database', [])

.factory('Persistence', function($q, $cordovaFileTransfer, $ionicPlatform, $rootScope) {
  
  var entities = {};
  
  entities.Option = persistence.define('Option', {
    name: "TEXT",
    text: "TEXT",
    color: "TEXT",
    image: "TEXT",
    sound: "TEXT",
    owner: "INT",
    shared: "BOOL"
  });
  
  entities.Pair = persistence.define('Pair', {});
  entities.Pair.hasOne('left', entities.Option);
  entities.Pair.hasOne('right', entities.Option);
  
  entities.List = persistence.define('List', {
    name: "TEXT",
    user: "INT",
    owner: "INT",
    shared: "BOOL"
  });
  entities.List.hasMany('pairs', entities.Pair, 'list');
  
  entities.Server = persistence.define('Server', {
    name: "TEXT",
    url: "TEXT",
    username: "TEXT",
    password: "TEXT",
    owner: "INT"
  });
  
  persistence.debug = true;
  
  return {
    initialized: false,
    init: function(){
      
      if (window && 'SQLitePlugin' in window) {
        console.log("database ok - SQLitePlugin present");
      } else if (window && window.openDatabase) {
        console.log("sqlitePlugin not supported, websql should be used");
      } else {
        console.log("no database supported!");
      }
      
      console.log("initiating database");
      persistence.store.cordovasql.config(persistence, 'YesNoAppDb3', '0.0.1', 'YesNoApp', 10 * 1024 * 1024, 0);
      persistence.schemaSync();
      console.log("database-ready");
      this.initialized = true;
      $rootScope.$broadcast('database-ready');
    },
    entities: entities,
    add: function(obj, callback) {
      persistence.add(obj);
      persistence.flush(callback);
    },
    remove: function(obj, callback) {
      persistence.remove(obj);
      persistence.flush(callback);
    },
    flush: function(callback) {
      persistence.flush(callback);
    },
    allOptions: function() {
      var defer = $q.defer();
      entities.Option.all().order('name', true).list(null, function(options) {
        defer.resolve(options);
      });
      return defer.promise;
    },
    privateOptions: function(ownerId) {
      var defer = $q.defer();
      entities.Option.all().filter('shared', '=', false).filter('owner', '=', ownerId ? ownerId : 0).order('name', true).list(null, function(options) {
        defer.resolve(options);
      });
      return defer.promise;
    },
    sharedOptions: function() {
      var defer = $q.defer();
      entities.Option.all().filter('shared', '=', true).order('name', true).list(null, function(options) {
        defer.resolve(options);
      });
      return defer.promise;
    },
    loadOption: function(id) {
      var defer = $q.defer();
      entities.Option.load(id, function(option) {
        defer.resolve(option);
      });
      return defer.promise;
    },
    allLists: function() {
      var defer = $q.defer();
      entities.List.all().order('name', true).list(null, function(lists) {
        defer.resolve(lists);
      });
      return defer.promise;
    },
    allListsOfUser: function(user) {
      var defer = $q.defer();
      entities.List.all().filter('user', '=', user ? user : 0).filter('shared', '=', 0).order('name', true).list(null, function(lists) {
        defer.resolve(lists);
      });
      return defer.promise;
    },
    allListsShared: function() {
      var defer = $q.defer();
      entities.List.all().filter('shared', '=', 1).order('name', true).list(null, function(lists) {
        defer.resolve(lists);
      });
      return defer.promise;
    },
    allServersOfUser: function(user) {
      var defer = $q.defer();
      console.log(entities.Server.all().filter('owner', '=', user ? user : 0));
      entities.Server.all().filter('owner', '=', user ? user : 0).order('name', true).list(null, function(servers) {
        defer.resolve(servers);
      });
      return defer.promise;
    },
    loadList: function(id) {
      var defer = $q.defer();
      entities.List.load(id, function(list) {
        defer.resolve(list);
      });
      return defer.promise;
    },
    loadServer: function(id) {
      var defer = $q.defer();
      entities.Server.load(id, function(server) {
        defer.resolve(server);
      });
      return defer.promise;
    },
    pairsOfList: function(list) {
      var defer = $q.defer();
      list.pairs.prefetch("left").prefetch("right").list(null, function(pairs) {
        defer.resolve(pairs);
      });
      return defer.promise;
    },
    loadPair: function(id) {
      var defer = $q.defer();
      entities.Pair.load(id, function(pair) {
        defer.resolve(pair);
      });
      return defer.promise;
    },
  };
  
});
