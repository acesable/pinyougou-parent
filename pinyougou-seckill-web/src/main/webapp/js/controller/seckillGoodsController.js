//控制层
app.controller('seckillGoodsController' ,function($scope,$location,$interval,seckillGoodsService){
    //读取列表数据绑定到表单中
    $scope.findList=function(){
        seckillGoodsService.findList().success(
            function(response){
                $scope.list=response;
            }
        );
    }

    //根据秒杀商品ID获取秒杀商品信息
    $scope.findOne=function(){
        var id = $location.search()['id'];
        seckillGoodsService.findOne(id).success(
            function(response){
                $scope.entity=response;

                var allSecond = Math.floor((new Date($scope.entity.endTime).getTime() - new Date().getTime()) / 1000);

                //倒计时定时器
                time = $interval(function () {
                    allSecond = allSecond-1;
                    $scope.timeString = converSecondToTimeString(allSecond);
                    if(allSecond<=0){
                        $interval.cancel(time);
                    }
                },1000);

            }
        );
    }

    //将秒转化为 xxx天xx:xx:xx
    converSecondToTimeString = function (allSecond) {

        var day = Math.floor(allSecond / (60 * 60 * 24));
        var hour = Math.floor((allSecond - (day * 60 * 60 * 24)) / (60 * 60));
        var minute = Math.floor((allSecond - (day * 60 * 60 * 24) - (hour * 60 * 60)) / 60);
        var second = (allSecond - (day * 60 * 60 * 24) - (hour * 60 * 60)) - minute * 60;

        var timeString = "";

        if(day>0){
            timeString = day + "天";
        }
        return timeString = timeString+hour+":"+minute+":"+second;
    };

    $scope.submitOrder = function () {
        seckillGoodsService.submitOrder($scope.entity.id).success(function (response) {
            if(response.success){
                alert("秒杀成功，请在5分钟之内完成付款");
                location.href = "pay.html";
            }else{
                alert(response.message);
            }
        });
    };
});
