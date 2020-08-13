app.service('contentService', function ($http) {
    this.selectContentListByCategoryId = function (categoryId) {
        return $http.get('content/selectContentListByCategoryId.do?categoryId='+categoryId);
    };
});