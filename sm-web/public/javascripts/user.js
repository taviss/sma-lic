app.service("AccountService", function ($http) {

    this.addUser = function (user) {
        return $http({
            method: "POST",
            url: "/users",
            headers: { 'contentType': "application/x-www-form-urlencoded" },
            data: {
                "userName": user.userName,
                "userMail": user.userMail,
                "userPass": user.userPass
            }
        });
    }

    this.changeUserPassword = function (pass) {
        return $http({
            method: "POST",
            url: "/users/password/change",
            headers: { 'contentType': "application/x-www-form-urlencoded" },
            data: {
                "oldPassword": pass.old,
                "newPassword": pass.new,
                "newPasswordRepeat": pass.repeat
            }
        });
    }
});

app.controller('userController', function ($scope, AccountService, UserService) {
    $scope.loggedIn = false;
    $scope.user;
    $scope.pass = {};
    $scope.pass.old = "";
    $scope.pass.new = "";
    $scope.pass.repeat = "";
    $scope.errors = {};
    
    $scope.isAuthenticated = function() {
           UserService.isAuthenticated(function (success) {
                if(success) {
                    window.location = '/';
                }
           });
    }
    
    $scope.isAuthenticated();

    $scope.addUser = function() {
        AccountService.addUser($scope.user)
            .error(function (error, response) {
                console.log(error)
                $scope.errors = error;
            })

            .success(function () {
                window.location = '/login';
            });
    };

    $scope.changeUserPassword = function() {
        AccountService.changeUserPassword($scope.pass)
            .error(function (response) {
                $scope.errors = response;
            })

            .success(function () {
                window.location = "/";
                alert("Password changed!");
            });
    };

});