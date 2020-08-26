app.controller("searchController", function ($scope, $location, searchService) {

    $scope.searchMap={keywords:'', category:'', brand:'', spec: {}, price:'', pageNum:1, pageSize:15, sort:'', sortField:''};

    $scope.search = function () {
        searchService.search($scope.searchMap).success(function (response) {
            $scope.resultMap = response;
            initPageLabel($scope.searchMap.pageNum,$scope.resultMap.totalPageNum,5);
        });
    };

    $scope.addSearchItem = function (key, value) {
        if(key=='category'||key=='brand'||key=='price'){//非规格
            $scope.searchMap[key]=value;
        }else{//规格
            $scope.searchMap.spec[key]=value;
        }
        $scope.search();
    };

    $scope.removeSearchItem = function (key) {
        if(key=='category'||key=='brand'||key=='price'){//非规格
            $scope.searchMap[key]='';
        }else{//规格
            delete $scope.searchMap.spec[key];
        }
        $scope.search();
    };

    $scope.searchByPage = function (pageNum) {
        if($scope.resultMap!=null){
            if(pageNum<1 || pageNum>$scope.resultMap.totalPageNum){
                return;
            }
        }
        $scope.searchMap.pageNum = parseInt(pageNum);
        $scope.search();
    }

    var initPageLabel = function (pageNum,totalPageNum,labelSize) {
        var a = (labelSize-1)/2;
        $scope.pageLabel = [];
        if(totalPageNum>labelSize){
            //偏移:第一页小于0了, 最大位要+偏移
            var smallThanStartNum=pageNum-a<1?1-(pageNum-a):0;
            //偏移:最后一页大于最大页了, 第一页要-偏移
            var largeThanEndNum=pageNum+a>totalPageNum?pageNum+a-totalPageNum:0;
            var startNum = pageNum-a-largeThanEndNum<1?1:pageNum-a-largeThanEndNum;
            var endNum = pageNum+a+smallThanStartNum>totalPageNum?totalPageNum:pageNum+a+smallThanStartNum;
            for(var i=startNum;i<=endNum;i++){
                $scope.pageLabel.push(i);
            }
        }
    }

    $scope.searchByOrder=function (sort,sortField) {
        $scope.searchMap.sort=sort;
        $scope.searchMap.sortField = sortField;
        $scope.searchByPage(1);
    }

    $scope.isContentBrand=function () {
        var brandList = $scope.resultMap.brands;
        for(var i=0; i<brandList.length;i++){
            if($scope.searchMap.keywords.indexOf(brandList[i].text)>-1){
                return true;
            }
        }
        return false;
    }

    $scope.beRedirect=function () {
        $scope.searchMap.keywords= $location.search()['keywords'];
        $scope.searchByPage(1);
    }

});