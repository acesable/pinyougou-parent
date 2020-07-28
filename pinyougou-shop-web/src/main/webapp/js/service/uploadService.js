app.service("uploadService", function ($http) {

    this.uploadFile=function () {
        var formData = new FormData();
        formData.append('file',file.files[0]);
        $http({
            url:'../upload.do',
            method:'post',
            data:formData,
            header:{'Content-Type':undefined},
            transformRequest: angular.identity
        });
    }

});