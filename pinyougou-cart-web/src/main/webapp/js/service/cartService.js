//购物车服务层
app.service('cartService',function($http){
    //购物车列表
    this.findCartList=function(){
        // alert("123");
        // var addressList = $http.get('address/getAddressListByLoginUser.do');
        // console.log(JSON.stringify(addressList));
        // debugger;
        var cartList = $http.get('cart/getCartList.do');
        console.log(cartList);
        return cartList;
    }

    //添加商品到购物车
    this.addGoodsToCartList=function(itemId,num){
        return $http.get('cart/addGoodsToCartList.do?itemId='+itemId+'&num='+num);
    }

    //求合计数
    this.sum=function (cartList){
        var totalValue={totalNum:0, totalMoney:0}

        for(var i=0;i<cartList.length;i++){
            var cart = cartList[i];
            for (var j=0;j<cart.orderItemList.length;j++){
                totalValue.totalNum+=cart.orderItemList[j].num;
                totalValue.totalMoney+=cart.orderItemList[j].totalFee;
            }
        }
        return totalValue;
    }

    this.getAddressList=function (){
        return $http.get('address/getAddressListByLoginUser.do');
    }

    //提交订单
    this.submitOrder=function (order){
        return $http.post('order/add.do', order);
    }

});
