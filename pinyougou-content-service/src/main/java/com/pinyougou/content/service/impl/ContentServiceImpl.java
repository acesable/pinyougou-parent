package com.pinyougou.content.service.impl;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.pojo.TbContentExample;
import com.pinyougou.pojo.TbContentExample.Criteria;
import com.pinyougou.content.service.ContentService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		contentMapper.insert(content);
		//更新该广告类型的缓存
        redisTemplate.boundHashOps("contents").delete(content.getCategoryId());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
	    //更新该广告修改前的广告类型的缓存
        Long categoryId = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();
        redisTemplate.boundHashOps("contents").delete(categoryId);
        contentMapper.updateByPrimaryKey(content);
        //更新该广告修改后的广告类型的缓存
        redisTemplate.boundHashOps("contents").delete(content.getCategoryId());
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
		    //删除先更新该广告类型的缓存
            Long categoryId = contentMapper.selectByPrimaryKey(id).getCategoryId();
            redisTemplate.boundHashOps("contents").delete(categoryId);
            contentMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		
		if(content!=null){			
						if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}
			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}
			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}
			if(content.getContent()!=null && content.getContent().length()>0){
				criteria.andContentLike("%"+content.getContent()+"%");
			}
			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}
	
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<TbContent> selectContentListByCategoryId(Long categoryId) {
        //查看缓存中是否有此类型的广告
        List<TbContent> contentList = (List<TbContent>) redisTemplate.boundHashOps("contents").get(categoryId);
        if(contentList==null){
            //缓存没有则取数据库中数据
            System.out.println("取数据库中广告数据");
            TbContentExample tbContentExample = new TbContentExample();
            Criteria criteria = tbContentExample.createCriteria();
            criteria.andCategoryIdEqualTo(categoryId);//广告类别ID
            criteria.andStatusEqualTo("1");//启动标志
            tbContentExample.setOrderByClause("sort_order");
            contentList = contentMapper.selectByExample(tbContentExample);
            redisTemplate.boundHashOps("contents").put(categoryId, contentList);
        }else{
            System.out.println("取缓存中广告数据");
        }
        return contentList;
    }
}
