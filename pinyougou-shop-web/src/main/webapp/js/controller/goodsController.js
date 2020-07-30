 //控制层 
app.controller('goodsController' ,function($scope,$controller,goodsService,uploadService,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//新增
	$scope.add=function(){
        $scope.entity.tbGoodsDesc.introduction = editor.html();
        goodsService.add( $scope.entity).success(
            function(response){
                if(response.success){
                    alert('新增成功!');
                    $scope.entity = {};
                    editor.html('');
                }else{
                    alert(response.message);
                }
            }
		);				
	};
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	//上传图片
	$scope.uploadFile=function () {
		uploadService.uploadFile().success(function (response) {
			if(response.success){
				// alert('上传成功!');
                $scope.image_entity.url=response.message;
			}else{
				alert(response.message);
			}
		}).error(function () {
			alert('上传发生错误');
		});
	};

	//添加上传图片列表
	$scope.entity={tbGoods:{},tbGoodsDesc:{itemImages: []}};//定义页面实体结构

	$scope.add_image_entity = function () {
		$scope.entity.tbGoodsDesc.itemImages.push($scope.image_entity);
	};

    $scope.del_image_entity = function ($index) {
        $scope.entity.tbGoodsDesc.itemImages.splice($index, 1);
    };

    //查询商品一级分类列表
    $scope.getItemCat1List=function () {
        itemCatService.findItemCatByParentId(0).success(function (response) {
            $scope.itemCat1List=response;
        });
    };

    //查询商品二级分类列表
    $scope.$watch('entity.tbGoods.category1Id', function (newValue, oldValue) {
        itemCatService.findItemCatByParentId(newValue).success(function (response) {
            $scope.itemCat2List=response;
        });
    });

    //查询商品三级分类列表
    $scope.$watch('entity.tbGoods.category2Id', function (newValue, oldValue) {
        itemCatService.findItemCatByParentId(newValue).success(function (response) {
            $scope.itemCat3List=response;
        });
    });

    //查询商品模板ID
    $scope.$watch('entity.tbGoods.category3Id', function (newValue, oldValue) {
        itemCatService.findOne(newValue).success(function (response) {
            $scope.entity.tbGoods.typeTemplateId=response.typeId;
        });
    });

	//查询品牌列表
	$scope.$watch('entity.tbGoods.typeTemplateId', function (newValue, oldValue) {
		typeTemplateService.findOne(newValue).success(function (response) {
			$scope.typeTemplate=response;
            $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);
            $scope.entity.tbGoodsDesc.customAttributeItems= JSON.parse($scope.typeTemplate.customAttributeItems);
		});
	});

});	
