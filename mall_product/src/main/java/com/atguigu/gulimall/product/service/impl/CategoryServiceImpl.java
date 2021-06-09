package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //查出所有分类
        List<CategoryEntity> entityList = baseMapper.selectList(null);
        //转账父子树形结构
        List<CategoryEntity> collect = entityList.stream().filter((categoryEntity) ->
            categoryEntity.getParentCid() == 0
        ).map((menu)->{
            menu.setChildren(getChildrens(menu,entityList));
            return menu;
        }).sorted((menu1,menu2)->{
            return menu1.getSort() - menu2.getSort();
        }).collect(Collectors.toList());

        return collect;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO   逻辑删除
        baseMapper.deleteBatchIds(asList);
    }


    @Override
    public Long[] findCatelogPath(Long categoryId) {
        List<Long> paths = new ArrayList<Long>();
        List<Long> parentPath = findParentPath(categoryId, paths);
        Collections.reverse(parentPath);
        return (Long[]) paths.toArray(new Long[parentPath.size()]);
    }

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;
    //级联更新数据
//    @Caching(evict = {
//            @CacheEvict(value = "category",key = "'getLevel1Categorys'"),
//            @CacheEvict(value = "category",key = "'getCatalogJson'")
//    })  //失效模式清除缓存
    @CacheEvict(value = "category",allEntries = true) //同上，指定删除改分区下所有缓存数据
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
    }

    @Cacheable(value = {"category"},key = "#root.method.name",sync = true)  //缓存  指定缓存的名字   ，sync异步加锁，获数据库数据，防止缓存击穿
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        long l = System.currentTimeMillis();
        List<CategoryEntity> entityList = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        System.out.println("消耗时间"+(System.currentTimeMillis()-l));
        return entityList;
    }

    @Cacheable(value = "category",key = "#root.methodName")
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson(){
        System.out.println("查询了数据库");
        //优化成1次查询
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        //查出所有1级分类
        List<CategoryEntity> level1Categorys = getParentCid(selectList,0L);
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<CategoryEntity> entityList = getParentCid(selectList,v.getCatId());
            List<Catelog2Vo> collect = null;
            if (entityList != null) {
                collect = entityList.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    List<CategoryEntity> level3catlog = getParentCid(selectList,l2.getCatId());
                    if(level3catlog != null){
                        List<Catelog2Vo.Catalog3Vo> l3catelog = level3catlog.stream().map(l3 -> {
                            Catelog2Vo.Catalog3Vo catalog3Vo = new Catelog2Vo.Catalog3Vo(l2.getCatId().toString(),l3.getCatId().toString(),l3.getName());
                            return catalog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(l3catelog);
                        //2.封装成指定格式

                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }

            return collect;
        }));

        return parent_cid;
    }


    public Map<String, List<Catelog2Vo>> getCatalogJson2(){

        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");

        if(StringUtils.isEmpty(catalogJSON)){
            System.out.println("没缓存");
            Map<String, List<Catelog2Vo>> catalogJsonFromDB = getCatalogJsonFromRedis();

            return catalogJsonFromDB;
        }
        System.out.println("有缓存");
        //转为我们指定对象
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON,new TypeReference<Map<String, List<Catelog2Vo>>>(){});
        return result;
    }

    @Autowired
    RedissonClient redisClient;

    public synchronized Map<String, List<Catelog2Vo>> getCatalogJsonFromRedisson() {

        //占锁，并设置过期时间
        RLock lock = redisClient.getLock("catalogJson-lock");
        lock.lock();

        Map<String, List<Catelog2Vo>> dataFromDB;
        try {
            dataFromDB = getCatalogJsonFromDB();
        } finally {
            lock.unlock();
        }
        return dataFromDB;

    }

    public synchronized Map<String, List<Catelog2Vo>> getCatalogJsonFromRedis() {

        //占锁，并设置过期时间
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid,300,TimeUnit.SECONDS);
        if(lock){
            System.out.println("获取分布式锁成功");
            Map<String, List<Catelog2Vo>> catalogJsonFromDB;
            try {
                catalogJsonFromDB = getCatalogJsonFromDB();
            }finally {
                String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                        "then\n" +
                        "    return redis.call(\"del\",KEYS[1])\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end";
                //脚本删除锁
                redisTemplate.execute(new DefaultRedisScript<Integer>(script,Integer.class),Arrays.asList("lock"),uuid);
            }
//            String lock1 = redisTemplate.opsForValue().get("lock");
//            if(uuid.equals(lock1)){
//                redisTemplate.delete("lock");
//            }
            return catalogJsonFromDB;
        }else{
            //加锁失败
            //休眠100ms重试
            System.out.println("重试");
            try{
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromRedis();   //自旋的方式
        }
        //加锁之后，缓存确定

    }

    public synchronized Map<String, List<Catelog2Vo>> getCatalogJsonFromDB() {

        //加锁之后，缓存确定
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if(!StringUtils.isEmpty(catalogJSON)){
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON,new TypeReference<Map<String, List<Catelog2Vo>>>(){});
            return result;
        }
        System.out.println("查询了数据库");
        //优化成1次查询
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        //查出所有1级分类
        List<CategoryEntity> level1Categorys = getParentCid(selectList,0L);
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<CategoryEntity> entityList = getParentCid(selectList,v.getCatId());
            List<Catelog2Vo> collect = null;
            if (entityList != null) {
                collect = entityList.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    List<CategoryEntity> level3catlog = getParentCid(selectList,l2.getCatId());
                    if(level3catlog != null){
                        List<Catelog2Vo.Catalog3Vo> l3catelog = level3catlog.stream().map(l3 -> {
                            Catelog2Vo.Catalog3Vo catalog3Vo = new Catelog2Vo.Catalog3Vo(l2.getCatId().toString(),l3.getCatId().toString(),l3.getName());
                            return catalog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(l3catelog);
                        //2.封装成指定格式

                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }

            return collect;
        }));

        //存JSON
        String s = JSON.toJSONString(parent_cid);
        //过期时间
        redisTemplate.opsForValue().set("catalogJSON",s,1, TimeUnit.DAYS);
        return parent_cid;
    }

    private List<CategoryEntity> getParentCid(List<CategoryEntity> selectList,Long parent_cid) {
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
        //        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
        return collect;
    }

    private List<Long> findParentPath(Long categoryId,List<Long> paths){
        //1、收集当前节点id
        paths.add(categoryId);
        CategoryEntity byId = this.getById(categoryId);
        if(byId.getParentCid() != 0){
            findParentPath(byId.getParentCid(),paths);
        }
        return paths;
    }

    //递归查找所有菜单的子菜单
    private  List<CategoryEntity> getChildrens(CategoryEntity root,List<CategoryEntity> all){
        List<CategoryEntity> collect = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map(categoryEntity -> {
            categoryEntity.setChildren(getChildrens(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()== null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return collect;
    }

}