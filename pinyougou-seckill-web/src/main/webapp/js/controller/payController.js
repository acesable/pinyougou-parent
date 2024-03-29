app.controller('payController' ,function($scope, $location,payService){
    //本地生成二维码
    $scope.createNative=function(){
        payService.createNative().success(
            function(response){
                $scope.money=  (response.total_fee/100).toFixed(2) ;	//金额
                $scope.out_trade_no= response.out_trade_no;//订单号
                //二维码
                var qr = new QRious({
                    element:document.getElementById('qrious'),
                    size:250,
                    level:'H',
                    value:response.code_url
                });
                // 开始获取支付结果
                $scope.queryPayStatus();
            }
        );
    }

    //查询支付状态
    $scope.queryPayStatus = function () {
        payService.queryPayStatus($scope.out_trade_no).success(function (response) {
            if (response.success) {
                location.href='paysuccess.html#?money='+$scope.money;
            }else{
                if(response.message=='二维码超时'){
                    alert("秒杀抢购支付超时")
                }else{
                    location.href='payfail.html';
                }
            }
        });
    };

    $scope.payMoney = function () {
        return $location.search()['money'];
    };

});
