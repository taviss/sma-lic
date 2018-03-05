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

    UserService.isAuthenticated(function (success) {
        if(success) {
            $scope.loggedIn = true;
        }
    });

    $scope.addUser = function() {
        AccountService.addUser($scope.user)
            .error(function (response) {
                alert(response);
            })

            .success(function () {
                alert("User created!");
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