package util;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class TransforItem {

    @Autowired
    TbItemMapper tbItemMapper;

//    @Autowired
//    SolrTemplate solrTemplate;

    public void transfor() {
        TbItemExample tbItemExample = new TbItemExample();
        TbItemExample.Criteria criteria = tbItemExample.createCriteria();
        criteria.andStatusEqualTo("1");
        List<TbItem> tbItems = tbItemMapper.selectByExample(tbItemExample);
        System.out.println(tbItems.size());
        for (TbItem item : tbItems) {
            System.out.println(item.getId()+" | "+item.getTitle()+" | "+item.getPrice());
//            Map spec = JSON.parseObject(item.getSpec(), Map.class);
//            item.setSpecMap(spec);
        }
//        solrTemplate.saveBeans(tbItems);
//        solrTemplate.commit();

    }

    public static void main(String[] args) {
        ApplicationContext context= new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        TransforItem transforItem = (TransforItem) context.getBean("transforItem");
        System.out.println("1231231231234556");
        transforItem.transfor();

    }

}
