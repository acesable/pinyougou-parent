app.controller("searchController", function ($scope, searchService) {

    $scope.searchItemList = function () {
        searchService.searchItemList($scope.searchMap).success(function (response) {
            $scope.resultMap = response;
        });
    };

});