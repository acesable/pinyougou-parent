//购物车控制层
app.controller('cartController',function($scope,cartService){
    //查询购物车列表
    $scope.findCartList=function(){
        cartService.findCartList().success(
            function(response){
                $scope.cartList=response;
                $scope.totalValue=cartService.sum($scope.cartList);
            }
        );
    }

    //添加商品到购物车
    $scope.addGoodsToCartList=function(itemId,num){
        cartService.addGoodsToCartList(itemId,num).success(
            function(response){
                if(response.success){
                    $scope.findCartList();
                }else{
                    alert(response.message);
                }
            }
        );
    }

    //获取收件人信息
    $scope.getAddressList=function (){
        cartService.getAddressList().success(
            function(response){
                $scope.addressList = response;
                for (var i = 0; i < $scope.addressList.length; i++) {
                    if($scope.addressList[i].isDefault=='1'){
                        $scope.selectAddress($scope.addressList[i]);
                        break;
                    }
                }
            }
        );
    }

    //选中地址
    $scope.selectAddress = function (address) {
        $scope.address = address;
    };

    //是否是选择的地址
    $scope.isSelectedAddress = function (address){
        if ($scope.address == address) {
            return true;
        }else{
            return false;
        }
    }

    //支付类型
    $scope.order = {paymentType:'1'}

    $scope.selectPaymentType = function (type){
        $scope.order.paymentType=type;
    }

    //提交订单
    $scope.submitOrder = function (){
        $scope.order.receiverAreaName = $scope.address.address;
        $scope.order.receiverMobile = $scope.address.mobile;
        $scope.order.receiver = $scope.address.contact;

        cartService.submitOrder($scope.order).success(function (response) {
            // alert(response.message);
            if(response.success){
                if($scope.order.paymentType=='1'){
                    location.href='pay.html';
                }else{
                    location.href='paysuccess.html';
                }
            }else{
                alert(response.message);
            }
        });
    }
});
