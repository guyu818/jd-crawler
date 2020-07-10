package cn.guyu.service;

import cn.guyu.dao.ItemDao;
import cn.guyu.pojo.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class  ItemServiceImpl implements ItemService {
    @Autowired
    private ItemDao itemDao;

    @Override
    @Transactional
    public void save(Item item) {
        itemDao.save(item);
    }

    @Override
    public List<Item> findAll(Item item) {
        //声明查询的条件
        Example<Item> example=Example.of(item);
        //根据条件查询数据
        List<Item> itemList = itemDao.findAll(example);

        return itemList;
    }
}
