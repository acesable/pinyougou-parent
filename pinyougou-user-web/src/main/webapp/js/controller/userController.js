 //控制层 
app.controller('userController' ,function($scope,$controller ,userService){

    $scope.reg = function (user) {
        if($scope.user.password!=$scope.password2){
            alert("两次密码输入不一致,请重新输入!");
            $scope.user.password = "";
            $scope.password2 = "";
            return;
        }

        userService.add(user).success(function (response) {
            alert(response.message);
        });
    };

    $scope.sendCode = function (phone) {
        if(phone==null || phone==''){
            alert("手机号不能为空");
            return;
        }
        userService.sendCode(phone).success(function (response) {
            alert(response.message);
        });
    };

});	
