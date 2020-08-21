app.controller("searchController", function ($scope, searchService) {

    $scope.searchMap={keywords:'', category:'', brand:'', spec: {}};

    $scope.search = function () {
        searchService.search($scope.searchMap).success(function (response) {
            $scope.resultMap = response;
        });
    };

    $scope.addSearchItem = function (key, value) {
        if(key=='category'||key=='brand'){//非规格
            $scope.searchMap[key]=value;
        }else{//规格
            $scope.searchMap.spec[key]=value;
        }
        $scope.search();
    };

    $scope.removeSearchItem = function (key, value) {
        if(key=='category'||key=='brand'){//非规格
            $scope.searchMap[key]='';
        }else{//规格
            delete $scope.searchMap.spec[key];
        }
        $scope.search();
    };

});