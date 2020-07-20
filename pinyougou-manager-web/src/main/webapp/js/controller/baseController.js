app.controller('baseController',function ($scope) {
    //分页控件配置 currentPage,totalItems,itemsPerPage,perPageOptions 这些都是初始化默认值 onChange 当页码变更时触发的方法
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function(){
            $scope.reloadList();
        }
    };

    //刷新列表
    $scope.reloadList = function () {
        $scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
    };

    $scope.ids = [];

    $scope.updateIds=function ($event,id) {
        if($event.target.checked){
            $scope.ids.push(id);
        }else{
            var idIndex = $scope.ids.indexOf(id);
            $scope.ids.splice(idIndex);
        }
    };

    $scope.jsonToString = function (jsonString, key) {
        var json = JSON.parse(jsonString);
        var value='';
        for(var i=0;i<json.length;i++){
            if (i > 0) {
                value+=','
            }
            value+=json[i][key];
        }
        return value;
    }
});