app.service("loginService", function ($http) {

    this.loginUserInfo = function () {
        return $http.get("../login/userInfo.do");
    }

});