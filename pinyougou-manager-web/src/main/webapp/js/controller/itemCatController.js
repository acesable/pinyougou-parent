 //控制层 
app.controller('itemCatController' ,function($scope,$controller,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		itemCatService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	};
	
	//分页
	$scope.findPage=function(page,rows){			
		itemCatService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	};
	
	//查询实体 
	$scope.findOne=function(id){				
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=itemCatService.update( $scope.entity ); //修改  
		}else{
            $scope.entity.parentId=$scope.parentId;
			serviceObject=itemCatService.add( $scope.entity);//增加
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
                    $scope.findItemCatByParentId($scope.parentId);//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	};
	
	 
	//批量删除 
	$scope.dele=function(){
		//获取选中的复选框
		itemCatService.dele( $scope.ids ).success(
			function(response){
				if(response.success){
                    $scope.ids = [];
                    $scope.findItemCatByParentId($scope.parentId);//刷新列表
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象
	
	//搜索
	$scope.search=function(page,rows){			
		itemCatService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	};

	//商品分类
    $scope.findItemCatByParentId=function (parentId) {
        $scope.parentId=parentId;
        itemCatService.findItemCatByParentId(parentId).success(function (response) {
            $scope.list=response;
        });
    };

    $scope.grade=1;

    $scope.setGrade = function (value) {
        $scope.grade=value;
    };

    $scope.selectItemCatId=function (grade,p_entity) {
        $scope.grade=grade;
        if(grade==1){
            $scope.entity_1=null;
            $scope.entity_2=null;
        }
        if (grade == 2) {
            $scope.entity_1=p_entity;
            $scope.entity_2=null;
        }
        if (grade == 3) {
            $scope.entity_2=p_entity;
        }

        $scope.findItemCatByParentId(p_entity.id);
    };

    $scope.typeTemplateList = {data:[]};

    $scope.selectTypeTemplateList = function () {
        typeTemplateService.selectTypeTemplateList().success(function (response) {
            $scope.typeTemplateList={data:response};
        });
    };
    
});
