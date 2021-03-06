 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,uploadService,itemCatService,typeTemplateService){
	
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
	};
	
	//查询实体 
	$scope.findOne=function(){
        var id = $location.search()['id'];
        if (id == null) {
            return;
        }
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
                editor.html($scope.entity.goodsDesc.introduction);
                $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                $scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);
                for(var i=0;i<$scope.entity.itemList.length;i++){
                    $scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
                }
			}
		);				
	};

	//判断规格是否选中
    $scope.isChecked = function (name,value) {
        var valueList = $scope.findByKeyValue($scope.entity.goodsDesc.specificationItems, 'attributeName', name).attributeValue;
        if (valueList == null) {
            return false;
        }else{
            if (valueList.indexOf(value) >= 0) {
                return true;
            }
        }
    };
	
	//新增
	$scope.add=function(){
        $scope.entity.goodsDesc.introduction = editor.html();//获取富文本内容
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

    //保存
    $scope.save=function(){
        var serviceObject;//服务层对象
        $scope.entity.goodsDesc.introduction = editor.html();//获取富文本内容
        if($scope.entity.goods.id!=null){//如果有ID
            serviceObject=goodsService.update( $scope.entity ); //修改
        }else{
            serviceObject=goodsService.add( $scope.entity  );//增加
        }
        serviceObject.success(
            function(response){
                if(response.success){
                    alert('保存成功!');
                    window.location.href='goods.html';
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
            if($location.search()['id']==null){// 新增商品
                $scope.entity.goodsDesc.customAttributeItems= JSON.parse($scope.typeTemplate.customAttributeItems);
            }
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

    // $scope.auditStatusValue = {0:'未申请',1:'申请中',2:'审核通过',3:'已驳回 '};
    $scope.auditStatusValue = ['未审核','已审核','审核未通过','已关闭'];

    $scope.itemCatKeyValue = {};

	$scope.getItemCat = function () {
        itemCatService.findAll().success(function (response) {
            var itemCatList = response;
            for(var i=0;i<itemCatList.length;i++){
                $scope.itemCatKeyValue[itemCatList[i].id]=itemCatList[i].name;
            }

        });
    };

    $scope.isMarketableValues = ['已下架', '已上架'];

    $scope.updateIsMarketable = function (isMarketable) {
        goodsService.updateIsMarketable($scope.ids, isMarketable).success(function (response) {
            if(response.success){
                $scope.reloadList();
                $scope.ids = [];
            }else{
                alert(response.message);
            }
        });
    }
});	
