app.service("UserService", function ($http) {

    this.loginUser = function(user) {
        return $http({
            method: "POST",
            url: "/login/submit",
            headers: { 'contentType': "application/x-www-form-urlencoded" },
            data: {
                "userName": user.name,
                "userPass": user.password
            }
         });
    }

    this.checkUser = function() {
        return $http({
            method: "GET",
            url: "/logged"
         });
    }

    this.logoutUser = function() {
        return $http({
            method: "GET",
            url: "/logout"
         });
    }

    this.isAuthenticated = function(callback) {
            this.checkUser()
             .error(function() {
                callback(false);
             })
             .success(function(data) {
                if(data != '') {
                    callback(true);
                } else {
                    callback(false);
                }
             });
    };

});

app.controller('loginController', function ($scope, UserService) {

    $scope.user = {};

    $scope.user.name = null;
    $scope.user.password = null;
    $scope.errors = {};

    $scope.isAuthenticated = function() {
       UserService.isAuthenticated(function (success) {
            if(success) {
                window.location = '/acp';
            }
       });
    }

    $scope.isAuthenticated();

    $scope.loginUser = function() {
        UserService.loginUser($scope.user)
            .success(function(){
                username = $scope.user.name;
                window.location = "/acp";
            })

            .error(function(error, status){
                $scope.errors = error;
            });
    }

    $scope.logoutUser = function() {
        UserService.logoutUser()
            .success(function() {
                window.location = "/";
            });
    }

});