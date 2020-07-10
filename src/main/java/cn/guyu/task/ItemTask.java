package cn.guyu.task;

import cn.guyu.enums.StatusEnum;
import cn.guyu.pojo.Item;
import cn.guyu.service.ItemService;
import cn.guyu.utils.HttpUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @Des 定时爬取数据
 * @Author guyu
 * @Date 2020/7/10 11:57
 * @Param
 * @Return
 */
@Component
public class ItemTask {
    @Autowired
    private HttpUtils httpUtils;
    @Autowired
    private ItemService itemService;
    //用于解析json
    private static final ObjectMapper MAPPER = new ObjectMapper();

    //当下载任务完成后，间隔多长时间进行下次任务。
    @Scheduled(fixedDelay = 100 * 1000)
    public void itemTask() throws JsonProcessingException {
        //1.声明地址
        String url = "https://search.jd.com/Search?keyword=%E6%89%8B%E6%9C%BA&enc=utf-8&qrst=1&rt=1&stop=1&vt=2&suggest=1.def.0.V18--12s0%2C20s0%2C38s0%2C97s0&wq=shouji&s=122&click=0&page=";
        //2.遍历页面
        for (int i = 1; i < 10; i = i + 2) {
            String html = httpUtils.doGetHtml(url + i);
            //解析页面，获取商品数据并存储
            parse(html);
        }
    }

    /**
     * 解析页面，并存储数据到数据库
     *
     * @param html
     */
    private void parse(String html) throws JsonProcessingException {
        //1.解析html
        Document document = Jsoup.parse(html);
        //获取spu
        Elements elements = document.select("div#J_goodsList >ul >li");
//

        for (Element element : elements) {

            //获取商品的spu
            Long spu = StatusEnum.NOT_EXIST.getType();
            String spuStr = element.attr("data-spu");
            if (spuStr != "") {
                spu = Long.parseLong(spuStr);
            }

            //获取sku
            Elements elSkus = element.select("li.ps-item");
            for (Element skus : elSkus) {
                Long sku = Long.parseLong(skus.select("img").attr("data-sku"));

                //根据sku查询数据库中有没有这个商品有的话，跳过
                Item item = new Item();
                item.setSku(sku);
                List<Item> itemList = itemService.findAll(item);
                if (itemList.size()>0){
                    continue;
                }
                //设置商品的spu
                item.setSpu(spu);
                //设置商品的详情url
                String itemUrl= "https://item.jd.com/"+sku+".html";
                item.setUrl(itemUrl);
                //设置商品图片
                String imgUrl="https:"+skus.select("img").attr("data-lazy-img");
                String imgName=httpUtils.doGetImage(imgUrl);
                item.setPic(imgName);
                //设置商品价格
                String priceJson = httpUtils.doGetHtml("https://p.3.cn/prices/mgets?skuIds=J_" + sku);
                double price = MAPPER.readTree(priceJson).get(0).get("p").asDouble();
                item.setPrice(price);
                //设置商品的标题
                String itemInfo = httpUtils.doGetHtml(item.getUrl());
                String title = Jsoup.parse(itemInfo).select("div.sku-name").text();
                item.setTitle(title);

                //设置商品爬取时间
                item.setCreated(new Date());
                item.setUpdated(item.getCreated());

                itemService.save(item);

            }
        }
    }

}
