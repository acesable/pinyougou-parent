app.controller("indexController", function ($scope,loginService) {

    $scope.loginUser = {};

    $scope.loginUserInfo = function () {
        loginService.loginUserInfo().success(function (response) {
            $scope.loginUser.id=response.loginId;
            $scope.loginUser.name=response.loginName;
        });
    };

});