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
        $scope.entity.goodsDesc.introduction = editor.html();
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
	$scope.entity={goods:{},goodsDesc:{itemImages: [],specificationItems:[]}};//定义页面实体结构

	$scope.add_image_entity = function () {
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	};

    $scope.del_image_entity = function ($index) {
        $scope.entity.goodsDesc.itemImages.splice($index, 1);
    };

    //查询商品一级分类列表
    $scope.getItemCat1List=function () {
        itemCatService.findItemCatByParentId(0).success(function (response) {
            $scope.itemCat1List=response;
        });
    };

    //查询商品二级分类列表
    $scope.$watch('entity.goods.category1Id', function (newValue, oldValue) {
        itemCatService.findItemCatByParentId(newValue).success(function (response) {
            $scope.itemCat2List=response;
        });
    });

    //查询商品三级分类列表
    $scope.$watch('entity.goods.category2Id', function (newValue, oldValue) {
        itemCatService.findItemCatByParentId(newValue).success(function (response) {
            $scope.itemCat3List=response;
        });
    });

    //查询商品模板ID
    $scope.$watch('entity.goods.category3Id', function (newValue, oldValue) {
        itemCatService.findOne(newValue).success(function (response) {
            $scope.entity.goods.typeTemplateId=response.typeId;
        });
    });

	//查询品牌列表
	$scope.$watch('entity.goods.typeTemplateId', function (newValue, oldValue) {
		typeTemplateService.findOne(newValue).success(function (response) {
			$scope.typeTemplate=response;
            $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);
            $scope.entity.goodsDesc.customAttributeItems= JSON.parse($scope.typeTemplate.customAttributeItems);
		});
		//商品规格选项下拉列表
		typeTemplateService.selectSpecificationItems(newValue).success(function (response) {
			$scope.specificationItems=response;

		});
	});
	
	//更新选择的商品规格选项
    $scope.updateSpecificationItems = function ($event, name, value) {
        var map = $scope.findByKeyValue($scope.entity.goodsDesc.specificationItems, 'attributeName', name);
        //还没此规格
        if (map == null) {
            $scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
        }else{
            if($event.target.checked) {
                map.attributeValue.push(value);
            }else{
                map.attributeValue.splice(map.attributeValue.indexOf(value), 1);
                if(map.attributeValue.length==0){
                    $scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(map), 1);
                }
            }
        }
    };

    //生成商品规格列表
	$scope.createSpecificationItemList = function () {
		var items = $scope.entity.goodsDesc.specificationItems;
		$scope.entity.itemList=[{spec:{},price:'0',num:'9999',status:'0',isDefault:'0'}];
		for (var i = 0; i < items.length; i++) {
			$scope.entity.itemList=addSpec($scope.entity.itemList, items[i].attributeName, items[i].attributeValue);
		}
	};

	var addSpec = function (list,columnName,columnValues) {
		var newList = [];
		for (var i = 0; i < list.length; i++) {
			var oldRow = list[i];
			for (var j = 0; j < columnValues.length; j++) {
				var newRow = JSON.parse(JSON.stringify(oldRow));//深克隆
				newRow.spec[columnName]=columnValues[j];
				newList.push(newRow);
			}
		}
		return newList;
	}

});	
