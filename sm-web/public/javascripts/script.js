var app = angular.module('project_upload', ['ngAnimate', 'ngRoute']);

app.config(function ($routeProvider, $locationProvider) {
        // configure the routing rules here
        $routeProvider.when('/projects/view/:id', {
            controller: 'projectViewController'
        });

        // enable HTML5mode to disable hashbang urls
        $locationProvider.html5Mode(true);
});

app.directive('loading',   ['$http' ,function ($http) {
           return {
               restrict: 'A',
               link: function (scope, elm, attrs)
               {
                   scope.isLoading = function () {
                       return $http.pendingRequests.length > 0;
                   };

                   scope.$watch(scope.isLoading, function (v)
                   {
                       if(v){
                           elm.show();
                       }else{
                           elm.hide();
                       }
                   });
               }
           };

}]);

app.controller('mainController', function ($scope, UserService) {
    $scope.loggedIn = false;

    UserService.isAuthenticated(function (success) {
        if(success) {
            $scope.loggedIn = true;
        }
    });
});