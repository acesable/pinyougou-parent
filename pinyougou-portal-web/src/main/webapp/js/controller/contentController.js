app.controller('contentController', function ($scope, contentService) {

    $scope.contentList = []; //广告列表

    $scope.selectContentListByCategoryId = function (categoryId) {
        contentService.selectContentListByCategoryId(categoryId).success(function (response) {
            $scope.contentList[categoryId] = response;
        });

    };

});