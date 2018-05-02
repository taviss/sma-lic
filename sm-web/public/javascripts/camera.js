app.service("CameraService", function ($http) {

    this.addCamera = function(camera) {
        return $http({
            method: "POST",
            url: "/cameras",
            headers: { 'contentType': "application/x-www-form-urlencoded" },
            data: {
                "address": camera.address,
                "user": camera.user,
                "password": camera.password,
                "cameraType": camera.type
            }
         });
    }

});

app.controller('cameraController', function ($scope, CameraService) {

    $scope.camera = {};

    $scope.camera.address = null;
    $scope.camera.user = null;
    $scope.camera.password = null;
    $scope.camera.type = null;
    $scope.errors = {};
    
    $scope.cameraTypes = ["RTMP", "OPENCV", "STATIC"];

    $scope.addCamera = function() {
        CameraService.addCamera($scope.camera)
            .success(function(){
                alert("Camera created!");
            })

            .error(function(error, status){
                $scope.errors = error;
            });
    }

});