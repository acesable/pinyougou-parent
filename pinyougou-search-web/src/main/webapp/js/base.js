var app = angular.module('pinyougou', []);

app.filter('trustHtml', ['$sce', function ($sce) {
    return function (data) { //入参:需过滤内容
        return $sce.trustAsHtml(data);//返回:过滤后内容
    };

}]);