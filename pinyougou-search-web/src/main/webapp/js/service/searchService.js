app.service("searchService", function ($http) {

    this.searchItemList=function (searchMap) {
        return $http.post("../searchController/searchItemList.do", searchMap);
    };

});