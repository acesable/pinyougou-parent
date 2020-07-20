app.controller('brandController',function ($scope, $http, $controller, brandService) {

    $controller('baseController',{$scope:$scope});

    //查询品牌列表
    $scope.findAll = function () {
        brandService.findAll().success(function (response) {
            $scope.list = response;
        });
    };

    $scope.findPage=function (page,size) {
        brandService.findPage(page,size).success(
            function (response) {
                $scope.list=response.rows;//显示当前页数据
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        );
    };

    $scope.save=function () {
        var object; //对象
        if ($scope.entity.id != null) { //因为点击新建的时候entity被清空了
            object=brandService.update($scope.entity);
        }else{
            object=brandService.add($scope.entity);
        }

        object.success(
            function (response) {
                if(response.success){
                    $scope.reloadList();
                }else{
                    alert(response.message);
                }
            });
    };

    $scope.findOne=function (id) {
        brandService.findOne(id).success(function (response) {
            $scope.entity=response;
        });
    };



    $scope.delete=function () {
        brandService.delete($scope.ids).success(
            function (response) {
                if(response.success){
                    $scope.ids=[];
                    $scope.reloadList();
                }else{
                    alert(response.message);
                }
            });
    };

    $scope.searchEntity = {}; //初始化

    $scope.search=function (page,size) {
        brandService.search(page,size,$scope.searchEntity).success(
            function (response) {
                $scope.list=response.rows;//显示当前页数据
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        );
    };
});