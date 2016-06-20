
angular.module('yesno.color', [])

.factory('Color', function($rootScope, $ionicPopup, IonicClosePopupService) {
  
  return {
    showPicker: function(scope, value, callback) {
      var Color = this;
      
      scope.colorsList = ["#820AC3", "#DB007C", "#FF0000", "#FF7400", "#FFAA00", "#FFD300", "#FFFF00", "#A2F300", "#00DB00", "#00B7FF", "#1449C4", "#4117C7", "#AAAAAA", "#FFFFFF", "#000000"];
      
      var picker = $ionicPopup.show({
        title: 'Výběr barvy',
        scope: scope,
        cssClass: 'colorPicker',
        template: '<div class="colorPicker">' +
                  '<div class="color" style="background-color: {{c}}" ng-repeat="c in colorsList" ng-click="colorPicked(c)"></div>' +
                  '</div>'
      });
      IonicClosePopupService.register(picker);
      
      scope.colorPicked = function(color) {
        console.log(picker);
        picker.close();
        callback(color);
      };
    }
  };
  
});

