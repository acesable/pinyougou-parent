app.controller('itemPageController',function ($scope) {

    // 改变购物数量
    $scope.addItemNum=function(num){
        $scope.itemNum=$scope.itemNum+num;
        if($scope.itemNum<1){
            $scope.itemNum=1;
        }
    };

    $scope.specificationMap={};

    // 记录选择的规格值
    $scope.selectSpecificationItem=function(attributeName,attributeValue){
        $scope.specificationMap[attributeName]=attributeValue;
        getSelectItem();//根据规格查询sku
    };

    // 判断规格是否被选中
    $scope.isSpecificationItemSelected=function(attributeName,attributeValue){
        if($scope.specificationMap[attributeName]==attributeValue){
            return true;
        }else{
            return false;
        }
    };

    //当前选择的SKU
    $scope.currentItem={};

    //加载默认SKU
    $scope.defaultItem=function(){
        $scope.currentItem=skuList[0];
        $scope.specificationMap=JSON.parse(JSON.stringify(skuList[0].spec));
    };

    //判断两个map对象是否相等
    isSpecificationTheSame=function(spec1,spec2){
        for(k in spec1){
            if(spec1[k]!=spec2[k]){
                return false;
            }
        }
        for(k in spec2){
            if(spec1[k]!=spec2[k]){
                return false;
            }
        }
        return true;
    };

    //根据规格查询sku
    getSelectItem=function(){
        for(var i=0;i<skuList.length;i++){
            if(isSpecificationTheSame(skuList[i].spec,$scope.specificationMap)){
                $scope.currentItem = skuList[i];
                return;
            }
        }
        //没有找到此sku时的默认值
        $scope.currentItem={id:0,title:'--没有该商品--',price:0};
    };

    //将商品加入购物车
    $scope.addToCar = function () {
        window.alert('商品ID: ' + $scope.currentItem.id);
    };
});